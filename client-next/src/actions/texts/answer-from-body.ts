"use server";
import { Document as LangDocument } from "langchain/document";
import { RecursiveCharacterTextSplitter } from "langchain/text_splitter";
import { MemoryVectorStore } from "langchain/vectorstores/memory";
import { vectorStoreInstance } from "@/lib/langchain/langchain";
import { generateHashKey, getRedisInstance } from "@/lib/langchain/redis-cache";
import { split } from "sentence-splitter";
import { removeHTML } from "@/lib/utils";
import { OllamaEmbeddings } from "@langchain/ollama";

function generateAnsCacheKey(text: string, query: string, k: number) {
  text = text.toLowerCase();
  query = query.toLowerCase();
  return generateHashKey(`${text}${query}${k}`, "ans");
}

interface AnsReturnType {
  content: string;
  score: number;
}

async function getHTMLAggregate(
  splits: string[],
  embeddings: OllamaEmbeddings,
  query: string,
  k: number,
) {
  const docs = splits.map(
    (chunk) =>
      new LangDocument({
        pageContent: chunk,
      }),
  );

  const vectorDb = await MemoryVectorStore.fromDocuments(docs, embeddings);

  return removeHTML(
    (
      await vectorDb.maxMarginalRelevanceSearch(query, {
        fetchK: k * 4,
        lambda: 0.6,
        k: k * 2,
      })
    ).reduce(
      (acc, cur) => {
        acc += cur.pageContent;
        return acc;
      },

      "",
    ),
  );
}

async function getSentenceResults(
  aggregatedResult: string,
  embeddings: OllamaEmbeddings,
  query: string,
  k: number,
) {
  const sentenceDocs = split(aggregatedResult)
    .filter((r) => r.raw.replace(/\s/g, "").length > 0)
    .reduce(
      (acc, _, i, arr) => {
        if (i % 2 === 0) {
          const nextItem = arr.at(i + 1) ? arr.at(i + 1)?.raw : "";
          acc.push({ raw: arr[i].raw + " " + nextItem });
        }
        return acc;
      },
      [] as { raw: string }[],
    )
    .map(
      (r) =>
        new LangDocument({
          pageContent: r.raw,
        }),
    );

  const sentenceVectorDb = await MemoryVectorStore.fromDocuments(
    sentenceDocs,
    embeddings,
  );

  return (await sentenceVectorDb.similaritySearchWithScore(query, k)).map(
    (d) => ({
      content: d[0].pageContent.trim(),
      score: d[1],
    }),
  );
}

export async function getAnswerFromBody(
  body: string,
  query: string,
  k = 3,
): Promise<AnsReturnType[] | undefined> {
  body = body.trim();
  query = query.trim();
  const cache = getRedisInstance();
  const cacheKey = generateAnsCacheKey(body, query, k);

  const cached = await cache.get(cacheKey);

  if (cached) {
    return JSON.parse(cached) as AnsReturnType[];
  }

  const [splits, embeddings] = await Promise.all([
    RecursiveCharacterTextSplitter.fromLanguage("html", {
      chunkSize: 600,
      chunkOverlap: 100,
    }).splitText(body),
    vectorStoreInstance.getEmbeddings(),
  ]);

  if (!embeddings) {
    return;
  }
  const aggregatedResult = await getHTMLAggregate(splits, embeddings, query, k);

  const results = await getSentenceResults(
    aggregatedResult,
    embeddings,
    query,
    k,
  );

  await cache.setWithTTL(cacheKey, JSON.stringify(results));

  return results;
}
