import { vectorStoreInstance } from "@/lib/langchain/langchain";
import { ChatOllama } from "@langchain/ollama";
import { getOllamaArgs } from "@/lib/langchain/ollama-utils";
import { NextResponse } from "next/server";
import { MemoryVectorStore } from "langchain/vectorstores/memory";
import { Document as LangDocument } from "langchain/document";

const { modelName, ollamaBaseUrl } = getOllamaArgs();

export const dynamic = "force-dynamic";

const documents = [
  new LangDocument({
    pageContent: "This is a test document for AI Seed initialization.",
  }),
];

export async function GET() {
  await vectorStoreInstance.initialize(false, false);
  const embeddingModel = await vectorStoreInstance.getEmbeddings();
  if (!embeddingModel) {
    return NextResponse.json(
      {
        error: "Error getting vector store embeddings",
      },
      {
        status: 500,
      },
    );
  }
  await MemoryVectorStore.fromDocuments(documents, embeddingModel);
  console.error("OLLAMA CTX", process.env.OLLAMA_NUM_CTX, new Date().getTime());
  const model = new ChatOllama({
    model: modelName,
    baseUrl: ollamaBaseUrl,
    keepAlive: "-1m",
    temperature: 0.2,
    cache: false,
    numCtx: process.env.OLLAMA_NUM_CTX
      ? parseInt(process.env.OLLAMA_NUM_CTX)
      : 2048,
    streaming: false,
  });

  const res = await model.invoke(
    "keep it very short and simple, no more than 10 words",
  );

  console.log("AI Seed initialized with response:", res);

  return NextResponse.json({
    message: "AI Seed initialized successfully",
  });
}
