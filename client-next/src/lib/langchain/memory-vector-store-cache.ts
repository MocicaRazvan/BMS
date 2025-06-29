import { Document as LangDocument } from "langchain/document";
import { MemoryVectorStore } from "langchain/vectorstores/memory";
import { OllamaEmbeddings } from "@langchain/ollama";
import { v3 as murmurV3 } from "murmurhash";
import TTLCache from "@isaacs/ttlcache";

export interface MemoryVectorStoreCacheEntry {
  store: Promise<MemoryVectorStore>;
  documents: LangDocument[];
}
class MemoryVectorStoreCache {
  private static instance: MemoryVectorStoreCache;
  private readonly cache: TTLCache<number, MemoryVectorStoreCacheEntry>;

  private constructor() {
    this.cache = new TTLCache({
      max: 250,
      ttl: 1000 * 60 * 5,
      updateAgeOnGet: true,
    });
  }

  static getInstance(): MemoryVectorStoreCache {
    if (!MemoryVectorStoreCache.instance) {
      MemoryVectorStoreCache.instance = new MemoryVectorStoreCache();
    }
    return MemoryVectorStoreCache.instance;
  }

  public getOrCreate(
    documents: LangDocument[],
    embeddings: OllamaEmbeddings,
  ): MemoryVectorStoreCacheEntry {
    const cacheKey = this.generateCacheKey(documents);
    const cachedEntry = this.cache.get(cacheKey);
    if (cachedEntry) {
      return cachedEntry;
    }
    const store = MemoryVectorStore.fromDocuments(documents, embeddings);
    const entry = {
      store,
      documents,
    };
    this.cache.set(cacheKey, entry);
    return entry;
  }

  public getStoreOrCreate(
    documents: LangDocument[],
    embeddings: OllamaEmbeddings,
  ) {
    return this.getOrCreate(documents, embeddings).store;
  }

  public remove(documents: LangDocument[]) {
    this.cache.delete(this.generateCacheKey(documents));
  }

  private generateCacheKey(documents: LangDocument[]) {
    return murmurV3(documents.map((doc) => doc.pageContent).join());
  }
}
declare const globalThis: {
  memoryVectorStoreCache: MemoryVectorStoreCache | undefined;
} & typeof global;

export function getMemoryVectorStoreCache(): MemoryVectorStoreCache {
  if (!globalThis.memoryVectorStoreCache) {
    globalThis.memoryVectorStoreCache = MemoryVectorStoreCache.getInstance();
  }
  if (!globalThis.memoryVectorStoreCache) {
    throw new Error("Memory vector store cache is not initialized");
  }
  return globalThis.memoryVectorStoreCache;
}
