import { NextRequest, NextResponse } from "next/server";
import { ChatOllama } from "@langchain/ollama";
import {
  LangChainStream,
  Message as VercelMessage,
  StreamingTextResponse,
} from "ai";
import {
  ChatPromptTemplate,
  MessagesPlaceholder,
  PromptTemplate,
} from "@langchain/core/prompts";
import { createStuffDocumentsChain } from "langchain/chains/combine_documents";
import { createHistoryAwareRetriever } from "langchain/chains/history_aware_retriever";
import { createRetrievalChain } from "langchain/chains/retrieval";
import { AIMessage, HumanMessage } from "@langchain/core/messages";
import { vectorStoreInstance } from "@/lib/langchain";
import { ContextualCompressionRetriever } from "langchain/retrievers/contextual_compression";
import { EmbeddingsFilter } from "langchain/retrievers/document_compressors/embeddings_filter";
import { VectorStore } from "@langchain/core/vectorstores";
import { cookies } from "next/headers";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import { ScoreThresholdRetriever } from "langchain/retrievers/score_threshold";

const modelName = process.env.OLLAMA_MODEL;
const ollamaBaseUrl = process.env.OLLAMA_BASE_URL;
const siteUrl = process.env.NEXTAUTH_URL;

if (!modelName || !ollamaBaseUrl) {
  throw new Error(
    "OLLAMA_MODEL and OLLAMA_BASE_URL must be set in the environment",
  );
}

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

    const chatCount = process.env.OLLAMA_CHAT_COUNT
      ? -(parseInt(process.env.OLLAMA_CHAT_COUNT) + 1)
      : -21;
    const chatHistory = messages
      .slice(chatCount, -1) // last 20 messages
      .map((m: VercelMessage) =>
        m.role === "user"
          ? new HumanMessage(m.content)
          : new AIMessage(m.content),
      );

    const currentMessageContent = messages[messages.length - 1].content;

    const { stream, handlers } = LangChainStream();
    const newHandlers: ReturnType<typeof LangChainStream>["handlers"] = {
      ...handlers,
      handleLLMEnd: (output, id) => {
        console.error("LLMEND", JSON.stringify(output), id);
        return handlers.handleLLMEnd(output, id);
      },
    };
    // retrievers and documents
    const [historyAwareRetrieverChain, combineDocsChain] = await Promise.all([
      createHistoryChain(vectorStore, vectorFilter),
      createDocsChain(newHandlers, currentUserRole, currentUserId),
    ]);

    console.log("RETRIVER DONE");

    const retrievalChain = await createRetrievalChain({
      combineDocsChain,
      retriever: historyAwareRetrieverChain,
    });

    retrievalChain.invoke({
      input: currentMessageContent,
      chat_history: chatHistory,
    });

    return new StreamingTextResponse(stream);
  } catch (error) {
    console.error("AIError", error);
    return NextResponse.json(
      { error: "Internal Server Error" },
      { status: 500 },
    );
  }
}

async function createHistoryChain(
  vectorStore: VectorStore,
  vectorFilter: EmbeddingsFilter,
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
    [
      "system",
      "You are a query **rephrasing assistant** for Bro Meets Science, a website focused on nutrition, meal plans, and promoting healthy lifestyles. " +
        "Your primary goal is to rephrase user queries to maximize the accuracy of document retrieval from the site's vector database. " +
        "Ensure the rephrased queries are highly relevant to the site's core focus areas, which include:\n" +
        "- **Meal plans** available for purchase\n" +
        "- Nutrition and dietary advice\n" +
        "- Caloric intake and calculators\n" +
        "- User health and well-being\n" +
        "- **Posts** which user can browse\n" +
        "- Already purchased meal plans\n\n" +
        "Rephrase the query **while preserving its original intent**. Include all essential keywords to ensure the rephrased query retrieves the most accurate and comprehensive results from the database. " +
        "Be concise, specific, and ensure the rephrased query aligns with the site's purpose and content. " +
        "**Never omit relevant keywords** and always rephrase with the site's purpose in mind. " +
        "**ONLY return the rephrased query without any additional text.**\n" +
        "\n" +
        "Below is the chat history for context.\n\n",
    ],
    new MessagesPlaceholder("chat_history"),
    [
      "user",
      "Original query: {input}\n\n" +
        "Based on the original query and conversation context, rephrase the query to improve document retrieval. " +
        "**ONLY return the rephrased query with no extra text or explanation.**",
    ],
  ]);

  const thresholdRetriever = ScoreThresholdRetriever.fromVectorStore(
    vectorStore,
    {
      searchType: "mmr",
      // low similarly because the vectors are from html pages
      // lower from embeddings model to get more results (pg store is not as good as the embeddings model)
      minSimilarityScore: 0.3, // similarity threshold
      maxK: 20, // at most 20 results
      kIncrement: 2, // increment by 2
      verbose: true,
    },
  );

  const compressionRetriever = new ContextualCompressionRetriever({
    baseRetriever: thresholdRetriever,
    baseCompressor: vectorFilter,
    verbose: true,
  });

  return await createHistoryAwareRetriever({
    llm: rephrasingModel,
    retriever: compressionRetriever,
    rephrasePrompt,
  });
}

async function createDocsChain(
  handlers: ReturnType<typeof LangChainStream>["handlers"],
  currentUserRole: string,
  currentUserId: string | undefined,
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

  const locale = cookies().get("NEXT_LOCALE")?.value || "en";

  console.log("CurrentUserId", currentUserId);
  const siteNoPort = siteUrl?.replace(/:\d+/, "");
  const prompt = ChatPromptTemplate.fromMessages([
    [
      "system",
      "You are Shaormel, the friendly and helpful chatbot for Bro Meets Science, a website focused on nutrition. " +
        "Your primary role is to assist users by providing information related to nutrition, meal plans, and the site’s features. " +
        "Do not discuss any technical aspects, including the site's code or development. " +
        "Your responses should prioritize user health, well-being, and engagement, delivered in a friendly and approachable manner, but keep the responses short and concise. " +
        "Feel free to use light-hearted humor when appropriate to create a welcoming atmosphere.\n\n" +
        "When constructing URLs, **ALWAYS** replace the placeholder [userId] with the: " +
        (currentUserId || "") +
        ".\n\n" +
        "Always ensure that any URLs you provide are localized according to the user's current language preference. The localization means that links start with the user current locale, the rest of the URL it's not localized! " +
        "The site supports English (locale: 'en') and Romanian (locale: 'ro'). The site base URL is " +
        siteNoPort +
        " always add it to internal links and ** NEVER ** add a specific port to it. " +
        "You are currently using the '" +
        locale +
        "' locale.\n\n" +
        "Here are the key sections of the site with the appropriate localized URLs:\n" +
        "- Caloric intake calculator: /" +
        locale +
        "/calculator\n" +
        "- Nutrition posts where user can browse all the posts: /" +
        locale +
        "/posts/approved\n" +
        "- Meal plans page where the user can BUY new plans and browse all plans: /" +
        locale +
        "/plans/approved\n" +
        "- Account login/registration: /" +
        locale +
        "/auth/signin\n" +
        "- View orders: /" +
        locale +
        "/orders\n" +
        "- Manage ALREADY purchased meal plans: /" +
        locale +
        "/subscriptions\n" +
        "- Terms of service: /" +
        locale +
        '/termsOfService\n\n"' +
        "If the user communicates in a language other than English or Romanian, respond in English unless the user specifies otherwise. " +
        "If the user's language preference is unclear, default to English but try to maintain the user's initial language if possible. " +
        "**Always format your messages in markdown** to improve readability and user experience.\n\n" +
        "**Key guidelines based on user roles**:\n" +
        (currentUserRole === "user"
          ? "As a 'user', only provide information relevant to general users. Do not share details related to trainer or admin features.\n"
          : currentUserRole === "trainer"
            ? "As a 'trainer', provide information relevant to both general users and trainers. However, do not share details related to admin features.\n"
            : "As an 'admin', provide information relevant to users, trainers, and admins as necessary.") +
        "\n\n" +
        +"**Key guidelines for interaction**:\n" +
        "1. Focus on user experience, health, and well-being.\n" +
        "2. Keep the conversation engaging, informative, and fun.\n" +
        "3. Never provide HTML/JS code or discuss technical details with the user.\n" +
        "4. Never send images to the user, you are a text based chat, but you can send emojis. \n" +
        "5. Never mention other sites and always focus on the site you are assisting with.\n" +
        "6. Always include the site's base URL : " +
        siteNoPort +
        " in any links you provide.\n" +
        "Context:\n{context}",
    ],
    new MessagesPlaceholder("chat_history"),
    ["user", "{input}"],
  ]);

  // {
  //           pageContent: pageContentTrimmed,
  //           metadata: { scope },
  //         };

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
