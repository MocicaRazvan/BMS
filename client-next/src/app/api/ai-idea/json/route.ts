import { NextRequest, NextResponse } from "next/server";
import { getUserWithMinRole } from "@/lib/user";
import { vectorStoreInstance } from "@/lib/langchain/langchain";
import { isAIIdeaActionArgs } from "@/types/ai-ideas-types";
import { ChatOllama } from "@langchain/ollama";
import { cleanString, getBaseIdea } from "@/app/api/ai-idea/ai-idea-utils";
import { getOllamaArgs } from "@/lib/langchain/ollama-utils";

const { modelName, ollamaBaseUrl } = getOllamaArgs();
export async function POST(req: NextRequest) {
  await getUserWithMinRole("ROLE_TRAINER");
  const [embeddings, vectorFilter, body] = await Promise.all([
    vectorStoreInstance.getEmbeddings(),
    vectorStoreInstance.getFilter(),
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
    streaming: false,
  });
  if (!embeddings || !vectorFilter) {
    return NextResponse.json(
      {
        error: "Error getting vector store",
      },
      {
        status: 500,
      },
    );
  }
  const resp = await getBaseIdea(
    targetedField,
    fields,
    embeddings,
    llm,
    extraContext,
    input,
    vectorFilter,
    item,
  );

  const answer = cleanString(resp.answer, targetedField).trim();
  return NextResponse.json(
    {
      answer,
    },
    {
      status: 200,
    },
  );
}
