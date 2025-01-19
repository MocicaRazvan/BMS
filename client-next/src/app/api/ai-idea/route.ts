"use server";
import { ChatOllama } from "@langchain/ollama";
import { isAIIdeaActionArgs } from "@/types/ai-ideas-types";
import { vectorStoreInstance } from "@/lib/langchain/langchain";
import { NextRequest, NextResponse } from "next/server";
import { LangChainStream, StreamingTextResponse } from "ai";
import { getUserWithMinRole } from "@/lib/user";
import { cleanString, getBaseIdea } from "@/app/api/ai-idea/ai-idea-utils";
import { getOllamaArgs } from "@/lib/langchain/ollama-utils";

const { modelName, ollamaBaseUrl } = getOllamaArgs();

export async function POST(req: NextRequest) {
  await getUserWithMinRole("ROLE_TRAINER");
  const [embeddings, body] = await Promise.all([
    vectorStoreInstance.getEmbeddings(),
    req.json(),
  ]);
  if (!isAIIdeaActionArgs(body)) {
    return NextResponse.json(
      {
        error: "Invalid request body",
      },
      {
        status: 400,
      },
    );
  }
  const { fields, input, targetedField, item, extraContext = 5 } = body;
  const { stream, handlers } = LangChainStream();
  const newHandlers: ReturnType<typeof LangChainStream>["handlers"] = {
    ...handlers,
    handleLLMNewToken: (token) =>
      handlers.handleLLMNewToken(cleanString(token, targetedField)),
  };
  const llm = new ChatOllama({
    model: modelName,
    baseUrl: ollamaBaseUrl,
    keepAlive: "-1m",
    temperature: process.env.OLLAMA_GENERATE_TEMPERATURE
      ? parseFloat(process.env.OLLAMA_GENERATE_TEMPERATURE)
      : 0.7,
    cache: false,
    numCtx: process.env.OLLAMA_NUM_CTX
      ? parseInt(process.env.OLLAMA_NUM_CTX)
      : 2048,
    streaming: true,
    callbacks: [newHandlers],
  });

  if (!embeddings) {
    return NextResponse.json(
      {
        error: "Error getting vector store",
      },
      {
        status: 500,
      },
    );
  }

  getBaseIdea(
    targetedField,
    fields,
    embeddings,
    llm,
    extraContext,
    input,
    item,
  );

  return new StreamingTextResponse(stream);
}
