"use server";
import { RecursiveCharacterTextSplitter } from "langchain/text_splitter";
import { Document as LangDocument } from "langchain/document";
import { ChatPromptTemplate } from "@langchain/core/prompts";
import { ChatOllama } from "@langchain/ollama";
import {
  AIIdeaActionArgs,
  AiIdeasField,
  TargetedFields,
} from "@/types/ai-ideas-types";
import { createStuffDocumentsChain } from "langchain/chains/combine_documents";
import { createRetrievalChain } from "langchain/chains/retrieval";
import { vectorStoreInstance } from "@/lib/langchain/langchain";
import { MemoryVectorStore } from "langchain/vectorstores/memory";
import { getMultiQueryRetriever } from "@/lib/langchain/langhcain-multi-query-retriver";

const modelName = process.env.OLLAMA_MODEL;
const ollamaBaseUrl = process.env.OLLAMA_BASE_URL;
if (!modelName || !ollamaBaseUrl) {
  throw new Error(
    "OLLAMA_MODEL and OLLAMA_BASE_URL must be set in the environment",
  );
}
const titlePrompt = ChatPromptTemplate.fromMessages([
  [
    "system",
    `You are an advanced AI language model tasked with assisting users in generating highly engaging and accurate titles based on the provided context for **{item}**. 
    You are on a nutritional site and the output must always be related to health, wellness, lifestyle, fitness, nutrition, mental health, self-care, well-being, or healthy living.
    Your goal is to produce a SINGLE title that is clear, concise, and relevant to the content, capturing the essence of the given information.
    The title must be attention-grabbing, no more than 15 words, and formatted as a single line with no explanation or commentary. 

    **Strict rules for title generation:**
    1. Output a single title with NO explanations, alternate options, or commentary.
    2. The title must be a single line, with no introductions such as "Based on the context" or "Here is the title."
    3. Do not include any formatting like quotation marks, bullet points, or extra text.
    4. Make a single option and do not provide multiple titles.
    5. Make the title at most 15 words.
    6. Ensure the title is relevant for the item type: {item}.

    **Final output format:**
    - A SINGLE line containing ONLY the title and NOTHING else besides the title.
    - No explanations, commentary, or additional information, keep it concise, short and to the point.
    - Keep in mind the kind of item you are generating the title for: {item}.
    `,
  ],
  [
    "user",
    "Here is the context: {context}. Additionally, the user has provided this input: {input}. Based on this, generate a suitable title for the item: {item}.",
  ],
]);
const descriptionPrompt = ChatPromptTemplate.fromMessages([
  [
    "system",
    `You are a highly advanced AI language model tasked with generating detailed, comprehensive, and engaging descriptions based on the provided context for **{item}**. 
    You are on a nutritional site and the output must always be related to health, wellness, lifestyle, fitness, nutrition, mental health, self-care, well-being, or healthy living.
    Your goal is to produce verbose and thorough descriptions that cover all relevant aspects of the topic, leaving no important detail unexplored. 
    Be as detailed and expansive as possible, using rich and vivid language to create a complete and immersive description.
    
    **Strict rules for description generation:**
    - Make sure you ONLY give the description and make it as LARGE and VERBOSE as possible, do not worry about the length or time.
    - The output MUST be structured in **HTML** format to enhance readability and organization.
    - NEVER include links or any other format that is not HTML. 
    - Remember ALWAYS keep in mind that the description is for a {item} and make the description as LARGE and VERBOSE as you can.
    - Tailor the description to the specific nature of the **{item}**.
    `,
  ],
  [
    "user",
    "Here is the context: {context}. Additionally, the user has provided this input: {input}. Based on this, generate a verbose and detailed description for the item: {item}.",
  ],
]);

const placeholderInput =
  "health, wellness, lifestyle, fitness, nutrition, mental health, self-care, well-being, healthy living";
const prompts: Record<TargetedFields, ChatPromptTemplate> = {
  title: titlePrompt,
  description: descriptionPrompt,
};

export async function getHtmlDocs(field: AiIdeasField) {
  return (
    await RecursiveCharacterTextSplitter.fromLanguage("html", {
      chunkSize: 512,
      chunkOverlap: 100,
    }).splitText(field.content.trim())
  ).map(
    (chunk) =>
      new LangDocument({
        pageContent: `
        Source: Form field for ${field.name.trim()}
        Role: ${field.role.trim()}
        Content:
        ${chunk}
        `,
        metadata: {
          source: "Form field for " + field.name.trim(),
          type: "html",
          role: field.role.trim(),
        },
      }),
  );
}

export async function getTextDoc(field: AiIdeasField) {
  return [
    new LangDocument({
      pageContent: `
        Source: Form field for ${field.name.trim()}
        Role: ${field.role.trim()}
        Content:
        ${field.content.trim()}
      `,
      metadata: {
        source: "Form field for " + field.name.trim(),
        type: "string",
        role: field.role.trim(),
      },
    }),
  ];
}

export async function aiIdea({
  targetedField,
  fields,
  input,
  item,
  extraContext = 0,
}: AIIdeaActionArgs) {
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
  });
  const embeddings = await vectorStoreInstance.getEmbeddings();

  if (!embeddings) {
    return {
      error: "Error getting embeddings",
    };
  }
  const prompt = prompts[targetedField];
  const docs = (
    await Promise.all(
      fields
        .filter((f) => f.content.trim().length > 0)
        .map((f) => (f.isHtml ? getHtmlDocs(f) : getTextDoc(f))),
    )
  ).flat();
  let vectorDb = await MemoryVectorStore.fromDocuments(docs, embeddings);
  const combineDocsChain = await createStuffDocumentsChain({
    llm,
    prompt,
  });
  const vectorDbRetriever = vectorDb.asRetriever({
    searchType: "mmr",
    searchKwargs: {
      fetchK: 30,
    },
    k:
      (process.env.OLLAMA_GENERATE_K
        ? parseInt(process.env.OLLAMA_GENERATE_K)
        : 10) + extraContext,
  });
  const multiQueryRetriever = getMultiQueryRetriever({
    retriever: vectorDbRetriever,
  });
  const retrievalChain = await createRetrievalChain({
    combineDocsChain,
    retriever: multiQueryRetriever,
  });

  const resp = await retrievalChain.invoke({
    input: input || placeholderInput,
    item,
    question: input || placeholderInput,
  });

  // eslint-disable-next-line @typescript-eslint/ban-ts-comment
  // @ts-ignore
  vectorDb = null;
  return {
    answer: resp.answer,
  };
}
