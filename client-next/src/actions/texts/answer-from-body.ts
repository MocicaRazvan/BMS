"use server";
import { Document as LangDocument } from "langchain/document";
import { RecursiveCharacterTextSplitter } from "langchain/text_splitter";
import { vectorStoreInstance } from "@/lib/langchain/langchain";
import { generateHashKey, getRedisInstance } from "@/lib/langchain/redis-cache";
import { split } from "sentence-splitter";
import { removeHTML } from "@/lib/utils";
import { OllamaEmbeddings } from "@langchain/ollama";
import { getMemoryVectorStoreCache } from "@/lib/langchain/memory-vector-store-cache";

function generateAnsCacheKey(text: string, query: string, k: number) {
  text = text.toLowerCase();
  query = query.toLowerCase();
  return generateHashKey(`${text}${query}${k}`, "ans");
}

const parseString = (str: string) => str.replace(/\s/g, " ").trim();
const memoryVectorStoreCache = getMemoryVectorStoreCache();

interface AnsReturnType {
  content: string;
  score: number;
}

interface AggregatedResult {
  idx: number;
  content: string;
}
async function getHTMLAggregate(
  splits: string[],
  embeddings: OllamaEmbeddings,
  query: string,
  k: number,
) {
  if (k === 0) {
    k = 1;
  }
  const docs = splits.map(
    (chunk) =>
      new LangDocument({
        pageContent: chunk,
      }),
  );

  const vectorDb = await memoryVectorStoreCache.getStoreOrCreate(
    docs,
    embeddings,
  );

  return (
    await vectorDb.maxMarginalRelevanceSearch(query, {
      fetchK: (k + 1) * 4,
      lambda: 0.65,
      k: (k + 1) * 2,
    })
  ).reduce((acc, cur, idx) => {
    acc.push({
      idx,
      content: removeHTML(parseString(cur.pageContent)),
    });
    return acc;
  }, [] as AggregatedResult[]);
}

async function getSentenceResults(
  aggregatedResult: AggregatedResult[],
  embeddings: OllamaEmbeddings,
  query: string,
  k: number,
) {
  const sentenceDocs = aggregatedResult
    .map(({ idx, content }) =>
      [
        ...new Set(
          split(content)
            .filter((r) => r.raw.replace(/\s/g, "").length > 0)
            .map((r) => parseString(r.raw)),
        ),
      ].map(
        (r) =>
          new LangDocument({
            pageContent: parseString(r),
            metadata: { idx },
          }),
      ),
    )
    .flat();

  const sentenceVectorDb = await memoryVectorStoreCache.getStoreOrCreate(
    sentenceDocs,
    embeddings,
  );

  const retrievedSentences = (
    await sentenceVectorDb.similaritySearchWithScore(query, 2 * (k + 1))
  ).reduce(
    (acc, [doc, score]) => {
      const idx = doc.metadata.idx;
      if (!acc[idx]) {
        acc[idx] = { contents: [], cumScore: 0 };
      }
      acc[idx].contents.push(doc.pageContent.trim());
      acc[idx].cumScore += score;
      return acc;
    },
    {} as Record<number, { contents: string[]; cumScore: number }>,
  );

  return Object.values(retrievedSentences)
    .map(({ contents, cumScore }) => {
      const joinedContent = contents.join(" ");
      const lengthBoost = 1 + 0.025 * Math.log(1 + joinedContent.length);
      const boostedScore =
        Math.min((cumScore / contents.length) * lengthBoost, 1) - 0.1;
      // console.log(
      //   joinedContent.length,
      //   cumScore / contents.length,
      //   boostedScore + 0.1,
      // );

      return {
        content: joinedContent,
        score: boostedScore,
      };
    })
    .sort((a, b) => b.score - a.score)
    .slice(0, k);
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
      chunkOverlap: 150,
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
