import { Document as LangDocument } from "langchain/document";
import { MemoryVectorStore } from "langchain/vectorstores/memory";
import { LRUCache } from "lru-cache";
import { OllamaEmbeddings } from "@langchain/ollama";
import { v3 as murmurV3 } from "murmurhash";

export interface MemoryVectorStoreCacheEntry {
  store: Promise<MemoryVectorStore>;
  documents: LangDocument[];
}
class MemoryVectorStoreCache {
  private static instance: MemoryVectorStoreCache;
  private readonly cache: LRUCache<number, MemoryVectorStoreCacheEntry>;

  private constructor() {
    this.cache = new LRUCache({
      max: 70,
      maxSize: 1200,
      sizeCalculation: (v, _) => v.documents.length || 1,
      ttl: 1000 * 60 * 5,
      allowStale: false,
      updateAgeOnGet: true,
      updateAgeOnHas: false,
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
