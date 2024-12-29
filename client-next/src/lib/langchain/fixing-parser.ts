import { OutputFixingParser } from "langchain/output_parsers";
import { ChatOllama } from "@langchain/ollama";
import { z } from "zod";
import { StructuredOutputParser } from "@langchain/core/output_parsers";

const modelName = process.env.OLLAMA_MODEL;
const ollamaBaseUrl = process.env.OLLAMA_BASE_URL;

export function getFixingParser<T extends z.ZodTypeAny>(zodSchema: T) {
  return OutputFixingParser.fromLLM(
    new ChatOllama({
      model: modelName,
      baseUrl: ollamaBaseUrl,
      streaming: false,
      keepAlive: "-1m",
      temperature: 0.1,
      numCtx: process.env.OLLAMA_NUM_CTX
        ? parseInt(process.env.OLLAMA_NUM_CTX)
        : 2048,
    }),
    StructuredOutputParser.fromZodSchema(zodSchema),
  );
}
