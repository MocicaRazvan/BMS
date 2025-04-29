import { ChatPromptTemplate } from "@langchain/core/prompts";
import { AiIdeasField, TargetedFields } from "@/types/ai-ideas-types";
import { RecursiveCharacterTextSplitter } from "langchain/text_splitter";
import { Document as LangDocument } from "langchain/document";
import removeMd from "remove-markdown";
import { ChatOllama, OllamaEmbeddings } from "@langchain/ollama";
import { getMemoryVectorStoreCache } from "@/lib/langchain/memory-vector-store-cache";
import { createStuffDocumentsChain } from "langchain/chains/combine_documents";
import { getMultiQueryRetriever } from "@/lib/langchain/langhcain-multi-query-retriver";
import { ContextualCompressionRetriever } from "langchain/retrievers/contextual_compression";
import { createRetrievalChain } from "langchain/chains/retrieval";
import { EmbeddingsFilter } from "langchain/retrievers/document_compressors/embeddings_filter";
import { AppenderRetriever } from "@/lib/langchain/appender-retriever";

const titlePrompt = ChatPromptTemplate.fromMessages([
  [
    "system",
    `You are an advanced AI language model tasked with assisting users in generating highly engaging and accurate titles based on the provided context for the type of items: **{item}**. 
    You are on a nutritional site and the output must always be related to health, wellness, lifestyle, fitness, nutrition, mental health, self-care, well-being, or healthy living.
    Your goal is to produce a SINGLE title that is clear, concise, and relevant to the content, capturing the essence of the given information.
    The title must be attention-grabbing, no more than 15 words, and formatted as a single text line with no explanation or commentary. 

    **Strict rules for title generation:**
    1. Output a single title as a text with NO explanations, alternate options, or commentary.
    2. The title must be a single line, with no introductions such as "Based on the context" or "Here is the title."
    3. Do not include any formatting like quotation marks, bullet points, or extra text, or emojis.
    4. Make a single option and do not provide multiple titles.
    5. Make the title at MOST 15 words.
    6. Ensure the title is relevant for the item type: {item}. And make sure it is engaging and captures the essence of the content.
    7. Avoid using numbers or special characters in the title.

    **Final output format:**
    - A SINGLE line containing ONLY the title as text content and NOTHING else besides.
    - No explanations, commentary, or additional information, keep it concise, short and to the point.
    - Make sure the title is engaging, relevant, and captures the essence of the content.
    - Keep in mind the kind of item you are generating the title for item type: {item}.
    - Do not add any notes or disclaimers.
    `,
  ],
  [
    "user",
    "Here is the __context__: {context}.\n" +
      "Additionally, the user has provided this input: {input}. Based on this, output a single short title as a text with NO explanations, the item type: {item}.",
  ],
]);
const descriptionPrompt = ChatPromptTemplate.fromMessages([
  [
    "system",
    `You are a highly advanced AI language model tasked with generating detailed, comprehensive, and engaging descriptions based on the provided context for the type: **{item}**. 
    You are on a nutritional site and the output must always be related to health, wellness, lifestyle, fitness, nutrition, mental health, self-care, well-being, or healthy living.
    Your goal is to produce verbose and thorough descriptions that cover all relevant aspects of the topic, leaving no important detail unexplored. 
    Be as detailed and expansive as possible, using rich and vivid language to create a complete and immersive description. Make it as LARGE and VERBOSE as you can.
    Do not include anything other than the description.
    
    **Strict rules for description generation:**
    - Make sure you ONLY give the description and make it VERY LARGE and VERBOSE don't worry about time, but be sure to maintain quality and coherence.
    - The description will be displayed on a website, so make sure it is well-structured and easy to read.
    - The output MUST be structured in **HTML** format to enhance readability and organization. Don't add any styling to the HTML and don't use any markdown. The HTML will be used inside a div, so don't use html, head or body tags.
    - NEVER include links or emojis or any other format that is not HTML. 
    - Remember ALWAYS keep in mind that the description is for a {item} and make the description as LARGE and VERBOSE as you can.
    - Tailor the description to the specific nature of the type of the current item: **{item}**. Do not include anything besides the description.
    - Always ensure that the user **input** and provided **context** are reflected in the description.
    
    **Additional Instructions to Ensure Maximum Detail and Length:**
    1. Use **multiple subsections** for each topic, marked with headers, to ensure thorough exploration of every angle.
    2. For every paragraph, aim to **cover at least 2-3 points** in-depth to add length while maintaining quality and coherence.
    3. Use just HTML and not markdown or any other formatting. And do not include anything other than the description.
    4. Base your generated description on the provided **context** and **input** to ensure relevance and accuracy.
    5. Don't use the tags: html, head, body and don't add DOCTYPE. The description will be used inside a div.
    
    **Keep In Mind:** 
      The description should be detailed, engaging, and comprehensive, covering all relevant aspects of the topic. Make it as LARGE and VERBOSE as you can while maintaining coherence. And format it in simple HTML.
    `,
  ],
  [
    "user",
    "Here is the __context__: {context}.\n" +
      "Additionally, the user has provided this __input__: {input}. " +
      "**Based on the input and context**, generate a __verbose, very large and detailed__ description for an item of type: {item}.\n" +
      "Do not include anything else besides the description formatted in simple HTML.",
  ],
]);

export const placeholderInput =
  "health, wellness, lifestyle, fitness, nutrition, mental health, self-care, well-being, healthy living";
export const prompts: Record<TargetedFields, ChatPromptTemplate> = {
  title: titlePrompt,
  description: descriptionPrompt,
};
const memoryVectorStoreCache = getMemoryVectorStoreCache();

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
export function cleanString(str: string, targetedField: TargetedFields) {
  let newStr = str.replace(/```html|```|html/gi, "");
  if (targetedField === "title") {
    newStr = removeMd(newStr).replace(/["`*]/g, "");
  }
  return newStr;
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

export async function getBaseIdea(
  targetedField: "title" | "description",
  fields: AiIdeasField[],
  embeddings: OllamaEmbeddings,
  llm: ChatOllama,
  extraContext: number,
  input: string | undefined,
  item: string,
) {
  const prompt = prompts[targetedField];
  const filteredDocs = fields.filter((f) => f.content.trim().length > 0);
  const [htmlDocs, textDocs] = await Promise.all([
    Promise.all(filteredDocs.filter((f) => f.isHtml).map(getHtmlDocs)).then(
      (a) => a.flat(),
    ),
    Promise.all(filteredDocs.filter((f) => !f.isHtml).map(getTextDoc)).then(
      (a) => a.flat(),
    ),
  ]);

  const vectorDb = await memoryVectorStoreCache.getStoreOrCreate(
    htmlDocs,
    embeddings,
  );

  const combineDocsChain = await createStuffDocumentsChain({
    llm,
    prompt,
  });

  const envK = process.env.OLLAMA_GENERATE_K
    ? parseInt(process.env.OLLAMA_GENERATE_K)
    : 10;

  const vectorDbRetriever = vectorDb.asRetriever({
    searchType: "mmr",
    searchKwargs: {
      fetchK: 3 * envK,
      lambda: 0.6,
    },
    k: envK + extraContext,
  });

  const multiQueryRetriever = getMultiQueryRetriever({
    retriever: vectorDbRetriever,
    originalInput: input ?? placeholderInput,
    extraInput: placeholderInput,
    queryCount: input ? 2 : 3,
  });

  const baseCompressor = new EmbeddingsFilter({
    embeddings,
    // low similarly because the vectors are from html pages
    similarityThreshold: process.env.EMBEDDINGS_GENERATE_SIMILARITY_THRESHOLD
      ? parseFloat(process.env.EMBEDDINGS_GENERATE_SIMILARITY_THRESHOLD)
      : 0.5,
    k: undefined,
    // ip bc lower length of docs and embeddings are normalized
    similarityFn: (x, y) =>
      x.map((rowX) =>
        y.map((rowY) => rowX.reduce((sum, val, j) => sum + val * rowY[j], 0)),
      ),
  });

  const compressionRetriever = new ContextualCompressionRetriever({
    baseRetriever: multiQueryRetriever,
    baseCompressor,
  });

  const appenderRetriever = new AppenderRetriever(
    compressionRetriever,
    textDocs,
  );
  const retrievalChain = await createRetrievalChain({
    combineDocsChain,
    retriever: appenderRetriever,
  });

  return retrievalChain.invoke({
    input: input || placeholderInput,
    item,
    question: input || placeholderInput,
  });
}
