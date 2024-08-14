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

const modelName = process.env.OLLAMA_MODEL;
const ollamaBaseUrl = process.env.OLLAMA_BASE_URL;

if (!modelName || !ollamaBaseUrl) {
  throw new Error(
    "OLLAMA_MODEL and OLLAMA_BASE_URL must be set in the environment",
  );
}

export async function POST(req: NextRequest) {
  try {
    const [vectorStore, vectorFilter] = await Promise.all([
      vectorStoreInstance.getVectorStore(),
      vectorStoreInstance.getFilter(),
    ]);

    if (!vectorStore || !vectorFilter) {
      return NextResponse.json(
        { error: "Internal Server Error" },
        { status: 500 },
      );
    }

    const body = await req.json();
    const messages = body.messages;

    const chatHistory = messages
      .slice(-21, -1) // last 20 messages
      .map((m: VercelMessage) =>
        m.role === "user"
          ? new HumanMessage(m.content)
          : new AIMessage(m.content),
      );

    const currentMessageContent = messages[messages.length - 1].content;

    const { stream, handlers } = LangChainStream();

    const chatModel = new ChatOllama({
      model: modelName,
      baseUrl: ollamaBaseUrl,
      streaming: true,
      // verbose: true,
      keepAlive: "-1m",
      callbacks: [handlers],
      cache: true,
    });

    const rephrasingModel = new ChatOllama({
      model: modelName,
      baseUrl: ollamaBaseUrl,
      keepAlive: "-1m",
      // verbose: true,
    });

    const retriever = vectorStore.asRetriever({
      searchType: "mmr",
      searchKwargs: {
        fetchK: 35,
      },
      k: 25,
    });

    const compressionRetriver = new ContextualCompressionRetriever({
      baseRetriever: retriever,
      baseCompressor: vectorFilter,
      verbose: true,
    });

    const rephrasePrompt = ChatPromptTemplate.fromMessages([
      new MessagesPlaceholder("chat_history"),
      ["user", "{input}"],
      [
        "user",
        "Given the above conversation, generate a seach query to look up in order to get information relevant to the current question and context." +
          "Don't leave out any relevant keywords. Only return the query and no other text.",
      ],
    ]);

    const historyAwareRetrieverChain = await createHistoryAwareRetriever({
      llm: rephrasingModel,
      // retriever,
      retriever: compressionRetriver,
      rephrasePrompt,
    });

    const prompt = ChatPromptTemplate.fromMessages([
      [
        "system",
        "You are a chatbot for a nutritional website called Bro Meets Science, and your name is Shaormel. Your primary role is to assist users with information about the site’s purpose and nutrition. " +
          "Never speak about the site's code, development, or technical aspects. " +
          "You are a consumer-focused AI assistant dedicated to users' health and well-being. " +
          "Feel free to make light-hearted jokes when appropriate to create a friendly and engaging atmosphere. " +
          "Answer the user's questions based on the provided context. Guide users to relevant content on the website, such as nutrition posts, meal plans, or account features. " +
          "The site is available in two languages: Romanian (locale: 'ro') and English (locale: 'en'). " +
          "Your default language is english, but always respond in the language the user uses unless they specify otherwise. " +
          "If you detect a different language (e.g., Spanish, French), switch to English or Romanian depending on the user’s input language. " +
          "If you are unsure of the language or the user switches languages during the conversation, default to English but continue to prefer the user's initial language if possible. " +
          "The website includes: " +
          "- Posts about nutrition to help users make informed choices.\n" +
          "- Meal plans available for purchase, tailored to different dietary needs.\n" +
          "- Features for users to create an account, log in, or register using Google or GitHub.\n" +
          "- An orders page for users to view their orders.\n" +
          "- A purchased plans page where users can view and manage their bought meal plans.\n" +
          "- The contact info for the website is email: razvanmocica@gmail.com and the phone: 0764105200\n" +
          "Format your messages in markdown when possible to enhance readability and user experience. " +
          "**Remember**: Focus on user experience, health, and well-being. Keep the conversation helpful, fun, and engaging, and NEVER give html/js code to the user!\n\n" +
          "Context:\n{context}",
      ],
      new MessagesPlaceholder("chat_history"),
      ["user", "{input}"],
    ]);

    // {
    //           pageContent: pageContentTrimmed,
    //           metadata: { scope },
    //         };

    const combineDocsChain = await createStuffDocumentsChain({
      llm: chatModel,
      prompt,
      documentPrompt: PromptTemplate.fromTemplate(
        "{scope}\n\nPage content:\n{page_content}",
      ),
      documentSeparator: "\n----END OF DOCUMENT----\n",
    });

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
