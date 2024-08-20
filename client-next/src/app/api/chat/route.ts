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

const modelName = process.env.OLLAMA_MODEL;
const ollamaBaseUrl = process.env.OLLAMA_BASE_URL;
const siteUrl = process.env.NEXTAUTH_URL;

if (!modelName || !ollamaBaseUrl) {
  throw new Error(
    "OLLAMA_MODEL and OLLAMA_BASE_URL must be set in the environment",
  );
}

export async function POST(req: NextRequest) {
  try {
    const [vectorStore, vectorFilter, body] = await Promise.all([
      vectorStoreInstance.getPGVectorStore(),
      vectorStoreInstance.getFilter(),
      req.json(),
    ]);

    if (!vectorStore || !vectorFilter || !body) {
      return NextResponse.json(
        { error: "Internal Server Error" },
        { status: 500 },
      );
    }

    // const body = await req.json();
    const messages = body.messages satisfies VercelMessage[];

    const chatHistory = messages
      .slice(-21, -1) // last 20 messages
      .map((m: VercelMessage) =>
        m.role === "user"
          ? new HumanMessage(m.content)
          : new AIMessage(m.content),
      );

    const currentMessageContent = messages[messages.length - 1].content;

    const { stream, handlers } = LangChainStream();

    // const chatModel = new ChatOllama({
    //   model: modelName,
    //   baseUrl: ollamaBaseUrl,
    //   streaming: true,
    //   // verbose: true,
    //   keepAlive: "-1m",
    //   callbacks: [handlers],
    //   cache: true,
    //   temperature: 0.8,
    // });

    // const rephrasingModel = new ChatOllama({
    //   model: modelName,
    //   baseUrl: ollamaBaseUrl,
    //   keepAlive: "-1m",
    //   temperature: 0.4,
    //   // verbose: true,
    // });
    //
    // const retriever = vectorStore.asRetriever({
    //   searchType: "mmr",
    //   searchKwargs: {
    //     fetchK: 35,
    //   },
    //   k: 25,
    // });
    //
    // const compressionRetriver = new ContextualCompressionRetriever({
    //   baseRetriever: retriever,
    //   baseCompressor: vectorFilter,
    //   verbose: true,
    // });
    //
    // const rephrasePrompt = ChatPromptTemplate.fromMessages([
    //   new MessagesPlaceholder("chat_history"),
    //   ["user", "{input}"],
    //   [
    //     "user",
    //     "Given the above conversation, generate a seach query to look up in order to get information relevant to the current question and context." +
    //       "Don't leave out any relevant keywords. Only return the query and no other text.",
    //   ],
    // ]);
    //
    // const historyAwareRetrieverChain = await createHistoryAwareRetriever({
    //   llm: rephrasingModel,
    //   // retriever,
    //   retriever: compressionRetriver,
    //   rephrasePrompt,
    // });

    // const historyAwareRetrieverChain = await createHistroyChain(
    //   vectorStore,
    //   vectorFilter,
    // );
    // const combineDocsChain = await createDocsChain(handlers);
    const [historyAwareRetrieverChain, combineDocsChain] = await Promise.all([
      createHistroyChain(vectorStore, vectorFilter),
      createDocsChain(handlers),
    ]);

    // const prompt = ChatPromptTemplate.fromMessages([
    //   [
    //     "system",
    //     "You are a chatbot for a nutritional website called Bro Meets Science, and your name is Shaormel. Your primary role is to assist users with information about the site’s purpose and nutrition. " +
    //       "Never speak about the site's code, development, or technical aspects. " +
    //       "You are a consumer-focused AI assistant dedicated to users' health and well-being. " +
    //       "Feel free to make light-hearted jokes when appropriate to create a friendly and engaging atmosphere. " +
    //       "Answer the user's questions based on the provided context. Guide users to relevant content on the website, such as nutrition posts, meal plans, or account features. " +
    //       "The site is available in two languages: Romanian (locale: 'ro') and English (locale: 'en'). " +
    //       "Your default language is english, but always respond in the language the user uses unless they specify otherwise. " +
    //       "If you detect a different language (e.g., Spanish, French), switch to English or Romanian depending on the user’s input language. " +
    //       "If you are unsure of the language or the user switches languages during the conversation, default to English but continue to prefer the user's initial language if possible. " +
    //       "The website includes: " +
    //       "- Posts about nutrition to help users make informed choices.\n" +
    //       "- Meal plans available for purchase, tailored to different dietary needs.\n" +
    //       "- Features for users to create an account, log in, or register using Google or GitHub.\n" +
    //       "- An orders page for users to view their orders.\n" +
    //       "- A purchased plans page where users can view and manage their bought meal plans.\n" +
    //       "- The contact info for the website is email: razvanmocica@gmail.com and the phone: 0764105200\n" +
    //       "Format your messages in markdown when possible to enhance readability and user experience. " +
    //       "**Remember**: Focus on user experience, health, and well-being. Keep the conversation helpful, fun, and engaging, and NEVER give html/js code to the user!\n\n" +
    //       "Context:\n{context}",
    //   ],
    //   new MessagesPlaceholder("chat_history"),
    //   ["user", "{input}"],
    // ]);
    //
    // // {
    // //           pageContent: pageContentTrimmed,
    // //           metadata: { scope },
    // //         };
    //
    // const combineDocsChain = await createStuffDocumentsChain({
    //   llm: chatModel,
    //   prompt,
    //   documentPrompt: PromptTemplate.fromTemplate(
    //     "{scope}\n\nPage content:\n{page_content}",
    //   ),
    //   documentSeparator: "\n----END OF DOCUMENT----\n",
    // });

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
    console.error(error);
    return NextResponse.json(
      { error: "Internal Server Error" },
      { status: 500 },
    );
  }
}

async function createHistroyChain(
  vectorStore: VectorStore,
  vectorFilter: EmbeddingsFilter,
) {
  const rephrasingModel = new ChatOllama({
    model: modelName,
    baseUrl: ollamaBaseUrl,
    keepAlive: "-1m",
    temperature: 0.4,
    // verbose: true,
  });

  const retriever = vectorStore.asRetriever({
    searchType: "similarity",
    k: 40,
  });

  const compressionRetriever = new ContextualCompressionRetriever({
    baseRetriever: retriever,
    baseCompressor: vectorFilter,
    verbose: true,
  });

  const rephrasePrompt = ChatPromptTemplate.fromMessages([
    new MessagesPlaceholder("chat_history"),
    ["user", "{input}"],
    [
      "user",
      "Given the above conversation, generate a search query to look up in order to get information relevant to the current question and context." +
        "Don't leave out any relevant keywords. Only return the query and no other text.",
    ],
  ]);

  return await createHistoryAwareRetriever({
    llm: rephrasingModel,
    // retriever,
    retriever: compressionRetriever,
    rephrasePrompt,
  });
}

async function createDocsChain(
  handlers: ReturnType<typeof LangChainStream>["handlers"],
) {
  const chatModel = new ChatOllama({
    model: modelName,
    baseUrl: ollamaBaseUrl,
    streaming: true,
    // verbose: true,
    keepAlive: "-1m",
    callbacks: [handlers],
    cache: true,
    temperature: 0.7,
  });

  const locale = cookies().get("NEXT_LOCALE")?.value || "en";

  const prompt = ChatPromptTemplate.fromMessages([
    [
      "system",
      "You are Shaormel, the friendly and helpful chatbot for Bro Meets Science, a website focused on nutrition. " +
        "Your primary role is to assist users by providing information related to nutrition, meal plans, and the site’s features. " +
        "Do not discuss any technical aspects, including the site's code or development. " +
        "Your responses should prioritize user health, well-being, and engagement, delivered in a friendly and approachable manner. " +
        "Feel free to use light-hearted humor when appropriate to create a welcoming atmosphere.\n\n" +
        "Always ensure that any URLs you provide are localized according to the user's current language preference. " +
        "The site supports English (locale: 'en') and Romanian (locale: 'ro'). The site base URL is" +
        siteUrl +
        " always add it to internal links. " +
        "You are currently using the '" +
        locale +
        "' locale.\n\n" +
        "Here are the key sections of the site with the appropriate localized URLs:\n" +
        "- Nutrition posts: /" +
        locale +
        "/posts/approved\n" +
        "- Meal plans: /" +
        locale +
        "/plans/approved\n" +
        "- Account login/registration: /" +
        locale +
        "/auth/signin\n" +
        "- View orders: /" +
        locale +
        "/orders\n" +
        "- Manage purchased meal plans: /" +
        locale +
        "/subscriptions\n\n" +
        "If the user communicates in a language other than English or Romanian, respond in English unless the user specifies otherwise. " +
        "If the user's language preference is unclear, default to English but try to maintain the user's initial language if possible. " +
        "Always format your messages in markdown to improve readability and user experience.\n\n" +
        "**Key guidelines**:\n" +
        "1. Focus on user experience, health, and well-being.\n" +
        "2. Keep the conversation engaging, informative, and fun.\n" +
        "3. Never provide HTML/JS code or discuss technical details with the user.\n\n" +
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
      "{scope}\n\nPage content:\n{page_content}",
    ),
    documentSeparator: "\n----END OF DOCUMENT----\n",
  });
}
