import { NextRequest, NextResponse } from "next/server";
import { ChatOllama } from "@langchain/ollama";
import {
  LangChainStream,
  Message as VercelMessage,
  StreamingTextResponse,
  StreamData,
} from "ai";
import {
  ChatPromptTemplate,
  MessagesPlaceholder,
  PromptTemplate,
} from "@langchain/core/prompts";
import { createStuffDocumentsChain } from "langchain/chains/combine_documents";
import { createRetrievalChain } from "langchain/chains/retrieval";
import { AIMessage, HumanMessage, ToolMessage } from "@langchain/core/messages";
import { vectorStoreInstance } from "@/lib/langchain/langchain";
import { ContextualCompressionRetriever } from "langchain/retrievers/contextual_compression";
import { EmbeddingsFilter } from "langchain/retrievers/document_compressors/embeddings_filter";
import { VectorStore } from "@langchain/core/vectorstores";
import { cookies } from "next/headers";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import { ScoreThresholdRetriever } from "langchain/retrievers/score_threshold";
import { getMultiQueryRetriever } from "@/lib/langchain/langhcain-multi-query-retriver";
import { LLMChain } from "langchain/chains";
import { getToolsForInput } from "@/app/api/chat/tool-call-wrapper";
import { generateToolsForUser } from "@/app/api/chat/get-item-tool";
import { Locale } from "@/navigation";
import { PrivilegedRetriever } from "@/lib/langchain/privileged-documents-retriever";
import { emitInfo } from "@/logger";
import { getOllamaArgs } from "@/lib/langchain/ollama-utils";

const { modelName, ollamaBaseUrl } = getOllamaArgs();
const siteUrl = process.env.NEXTAUTH_URL;

if (!siteUrl) {
  throw new Error("NEXTAUTH_URL must be set in the environment");
}

export async function POST(req: NextRequest) {
  try {
    const session = await getServerSession(authOptions);

    const currentUserRole =
      !session?.user?.role || session?.user?.role === "ROLE_USER"
        ? "user"
        : session?.user?.role === "ROLE_TRAINER"
          ? "trainer"
          : "admin";
    const currentUserId = session?.user?.id;

    const [vectorStore, vectorFilter, body] = await Promise.all([
      vectorStoreInstance.getPGVectorStore(),
      vectorStoreInstance.getFilter(),
      req.json(),
    ]);

    if (!vectorStore || !vectorFilter || !body) {
      console.error("VectorStore", vectorStore);
      console.error("VectorFilter", vectorFilter);
      console.error("Body", body);
      return NextResponse.json(
        { error: "Internal Server Error" },
        { status: 500 },
      );
    }

    const messages = body.messages satisfies VercelMessage[];

    const currentMessageContent = messages[messages.length - 1].content;
    const token = session?.user?.token;
    const locale = (cookies().get("NEXT_LOCALE")?.value || "en") as Locale;

    const siteNoPort = siteUrl?.replace(/:\d+/, "") as string;

    const { stream, handlers } = LangChainStream();

    // retrievers and documents
    const [chatHistory, userChatHistory, previousToolCalls] =
      await getChatHistory(messages);

    // console.log("chatHistory", chatHistory);
    // console.log("userChatHistory", userChatHistory);
    // console.log("previousToolCalls", previousToolCalls);

    const toolMessages = await getToolsForInput({
      input: currentMessageContent,
      tools: token ? generateToolsForUser(token, siteNoPort, locale) : [],
      userChatHistory,
      previousToolCalls,
    });
    // console.log("toolMessages", toolMessages);

    const newHandlers: ReturnType<typeof LangChainStream>["handlers"] = {
      ...handlers,
      handleLLMEnd: (output, id) => {
        // console.error("LLMEND", JSON.stringify(output), id);
        return handlers.handleLLMEnd(output, id);
      },
    };

    const [historyAwareRetrieverChain, combineDocsChain] = await Promise.all([
      createHistoryChain(
        vectorStore,
        vectorFilter,
        userChatHistory,
        currentUserRole,
        currentMessageContent,
      ),
      createDocsChain(
        newHandlers,
        currentUserRole,
        currentUserId,
        toolMessages,
        siteNoPort,
        locale,
      ),
      getChatHistory(messages),
    ]);

    const retrievalChain = await createRetrievalChain({
      combineDocsChain,
      retriever: historyAwareRetrieverChain,
    });

    retrievalChain.invoke({
      input: currentMessageContent,
      chat_history: chatHistory,
      user_chat_history: userChatHistory,
      question: currentMessageContent, // bc multi query is fucked
    });
    const toolData = new StreamData();
    toolData.append(JSON.stringify(toolMessages));
    await toolData.close();

    return new StreamingTextResponse(stream, {}, toolData);
  } catch (error) {
    console.error("AIError", error);
    return NextResponse.json(
      { error: "Internal Server Error" },
      { status: 500 },
    );
  }
}

async function getChatHistory(messages: VercelMessage[]) {
  const chatCount = process.env.OLLAMA_CHAT_COUNT
    ? -(parseInt(process.env.OLLAMA_CHAT_COUNT) + 1)
    : -11;
  return (async () => messages.slice(chatCount, -1))().then((slicedMessages) =>
    Promise.all([
      (async () =>
        slicedMessages.map((m: VercelMessage) => {
          if (m.role === "user") {
            return new HumanMessage(m.content.replace(/\s+/g, " ").trim());
          } else if (m.role === "assistant") {
            return new AIMessage(m.content.replace(/\s+/g, " ").trim());
          } else if (m.role === "tool") {
            return new AIMessage(
              `[Previous Tool Response For Reference]: Tool Name - ${m.tool_call_id} \t Tool Content - ${m.content} \t Input - ${m.function_call || "unknown"}`,
            );
          } else {
            return new AIMessage(m.content.replace(/\s+/g, " ").trim());
          }
        }))(),
      (async () =>
        slicedMessages
          .filter((m) => m.role === "user")
          .map(
            (m: VercelMessage) =>
              new HumanMessage(m.content.replace(/\s+/g, " ").trim()),
          ))(),
      (async () =>
        slicedMessages
          .filter((m) => m.role === "tool")
          .map((me) => ({
            tool_call_id: me.tool_call_id,
            content: me.content,
            input: `${me.function_call}`,
          })))(),
    ]),
  );
}

const systemRephrasePrompt = `You are a highly skilled **query rephrasing assistant** for Bro Meets Science, a website dedicated to nutrition, meal plans, and promoting healthy lifestyles. 
Your role is to rephrase user queries to maximize the accuracy and relevance of document retrieval from the site's vector database. 
Your rephrased queries must align with the site's core focus areas, which include:
- **Meal plans** available for buying
- Nutrition and dietary advice
- Caloric intake and calculators
- User health and well-being
- **Posts** that users can browse
- Previously bought meal plans

### Guidelines for Rephrasing Queries
1. **Preserve Original Intent**: Ensure the rephrased query captures the user's intent without losing meaning.
2. **Incorporate Essential Keywords**: Include all critical keywords to ensure accurate and comprehensive document retrieval.
3. **Be Concise and Specific**: Avoid unnecessary verbosity while maintaining clarity.
4. **Align with the Site's Purpose**: Focus on topics relevant to Bro Meets Science, and avoid straying from its core themes.
5. **Limit the Number of Rephrased Queries**: Generate exactly **{queryCount}** rephrased queries and no more.

### Formatting Instructions
Your output **must strictly follow the XML format**. Provide the rephrased queries in the following structure:

<inputs>
Input 1
Input 2
Input 3
</inputs>

### Important Notes
- Do not include any additional commentary, explanations, or unnecessary information.
- Ensure all rephrased queries align with the website's focus areas and maintain their original intent.`;

const userRephrasePrompt = `##Original input##: {question}
    
      Keep the intent of the original input.
      
      Based on the **original input**, **previous user inputs**, and the **website's focus areas**.
      
      Generate exactly **{queryCount}** rephrased queries.
      
      Ensure the original query is included as the last entry in the output.
      
      Provide the output in the required XML format as shown below:
      
      <inputs>
      Input 1
      Input 2
      Input 3
      </inputs>
      
      Do not include anything else.`;

async function createHistoryChain(
  vectorStore: VectorStore,
  vectorFilter: EmbeddingsFilter,
  userChatHistory: HumanMessage[],
  currentUserRole: string,
  input: string,
) {
  const rephrasingModel = new ChatOllama({
    model: modelName,
    baseUrl: ollamaBaseUrl,
    keepAlive: "-1m",
    temperature: 0.2,
    cache: false,
    numCtx: process.env.OLLAMA_NUM_CTX
      ? parseInt(process.env.OLLAMA_NUM_CTX)
      : 2048,
    // verbose: true,
  });
  const rephrasePrompt = ChatPromptTemplate.fromMessages([
    ["system", systemRephrasePrompt],
    ...userChatHistory,
    ["user", userRephrasePrompt],
  ]);

  const thresholdRetriever = ScoreThresholdRetriever.fromVectorStore(
    vectorStore,
    {
      searchType: "similarity",
      // low similarly because the vectors are from html pages
      // lower from embeddings model to get more results (pg store is not as good as the embeddings model)
      minSimilarityScore: process.env.OLLAMA_MIN_SIMILARITY_SCORE
        ? parseInt(process.env.OLLAMA_MIN_SIMILARITY_SCORE)
        : 0.4,
      maxK: process.env.OLLAMA_MAX_K_CHAT
        ? parseInt(process.env.OLLAMA_MAX_K_CHAT)
        : 5, // max number of results
      kIncrement: 2, // increment by 2
      // verbose: true,
    },
  );

  const privilegedRetriever = new PrivilegedRetriever(
    thresholdRetriever,
    currentUserRole,
  );

  const multiQueryRetriever = getMultiQueryRetriever({
    retriever: privilegedRetriever,
    originalInput: input,
    llmChain: new LLMChain({
      llm: rephrasingModel,
      prompt: rephrasePrompt,
    }),
  });

  return new ContextualCompressionRetriever({
    baseRetriever: multiQueryRetriever,
    baseCompressor: vectorFilter,
    // verbose: true,
  });
}

function getChatSystemPrompt(
  currentUserId: string | undefined,
  siteNoPort: string,
  locale: Locale,
) {
  return `You are Shaormel, the friendly and helpful chatbot for Bro Meets Science, a website focused on nutrition. The site sells meal plans (also known as just plans), provides nutrition through posts and many other features.
  Your primary role is to assist users by providing information related to nutrition, meal plans, and the site’s features. 
  Do not discuss any technical aspects, including the site's code or development. 
  Your responses should prioritize user health, well-being, and engagement, delivered in a friendly and approachable manner, but keep the responses short and concise. Feel free to use light-hearted humor when appropriate to create a welcoming atmosphere.
  When constructing URLs, **ALWAYS** replace the placeholder [userId] with the: ${currentUserId || ""}.
  Always steer the conversation back to the site's content and features, and avoid discussing external topics or sites.
  Always ensure that any URLs you provide are localized according to the user's current language preference. The localization means that links start with the user current locale, the rest of the URL it's not localized! The site supports English (locale: 'en') and Romanian (locale: 'ro'). The site base URL is ${siteNoPort} always add it to internal links and ** NEVER ** add a specific port to it. You are currently using the '${locale}' locale.

**Here are the key sections of the site with the appropriate localized URLs**:
- Caloric intake calculator: /${locale}/calculator
- Nutrition posts where user can browse all the posts: /${locale}/posts/approved
- Meal plans page where the user can BUY new plans and browse all plans: /${locale}/plans/approved
- Account login/registration: /${locale}/auth/signin
- View orders: /${locale}/orders
- Manage ALREADY purchased meal plans: /${locale}/subscriptions
- Terms of service: /${locale}/termsOfService
- User profile: /${locale}/users/single/${currentUserId}

**Key guidelines for interaction**:
1. Focus on user experience, health, and well-being.
2. Keep the conversation engaging, informative, and fun.
3. Never provide HTML/JS code or discuss technical details with the user.
4. Never send images to the user, you are a text based chat, but you can send emojis. 
5. Always format your messages in markdown.
6. Never mention other sites and always focus on the site you are assisting with.
7. Include the tools output in your response, if they are present. Don't explicitly say that you are using a tool, just include the output in your response.
8. Always include the site's base URL : ${siteNoPort} in any links you provide.

**Context and Tool Integration**:
  - If the user communicates in a language other than English or Romanian, respond in English unless the user specifies otherwise. If the user's language preference is unclear, default to English but try to maintain the user's initial language if possible. 
  - When tools are used to assist in answering a query, include their in your response naturally and appropriately. Ensure that tool outputs enhance clarity and add value. Never make follow-up queries about the tools results, always provide the information in the same response.

**Important Notes**:
- Always format your messages in markdown to improve readability and user experience.
- Always steer the conversation back to the current site's content and features.
- NEVER invent information or make up details/plans/posts that do not exist on the site. It is crucial to provide accurate and relevant information to users.
- Include tools output, but do not explicitly mention that you are using a tool. Integrate the tool output naturally into your response.

The context of the current conversation is as follows:
{context}`;
}

async function createDocsChain(
  handlers: ReturnType<typeof LangChainStream>["handlers"],
  currentUserRole: string,
  currentUserId: string | undefined,
  toolsMessages: ToolMessage[],
  siteNoPort: string,
  locale: Locale,
) {
  const chatModel = new ChatOllama({
    model: modelName,
    baseUrl: ollamaBaseUrl,
    streaming: true,
    // verbose: true,
    keepAlive: "-1m",
    callbacks: [handlers],
    cache: false,
    temperature: process.env.OLLAMA_TEMPERATURE
      ? parseFloat(process.env.OLLAMA_TEMPERATURE)
      : 0.7,
    numCtx: process.env.OLLAMA_NUM_CTX
      ? parseInt(process.env.OLLAMA_NUM_CTX)
      : 2048,
  });
  toolsMessages = toolsMessages.filter((t) => t.content.length > 0);
  emitInfo(`Tool messages: ${JSON.stringify(toolsMessages, null, 2)}`);

  const prompt = ChatPromptTemplate.fromMessages([
    ["system", getChatSystemPrompt(currentUserId, siteNoPort, locale)],
    new MessagesPlaceholder("chat_history"),
    ...toolsMessages,
    ["user", "{input}"],
  ]);

  return await createStuffDocumentsChain({
    llm: chatModel,
    prompt,
    documentPrompt: PromptTemplate.fromTemplate(
      "URL: " +
        "{url}\n" +
        "Title: " +
        "{title}\n" +
        "Description: " +
        "{description}\n\n" +
        "Page Content:\n" +
        "{page_content}",
      // .slice(0, 501),
    ),
    documentSeparator: "\n----END OF DOCUMENT----\n",
  });
}
