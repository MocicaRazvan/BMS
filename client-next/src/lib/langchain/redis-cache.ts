import { Redis } from "ioredis";
import { RedisCache } from "@langchain/community/caches/ioredis";

declare const globalThis: {
  redisCache: RedisCache | undefined;
  redisInstance: Redis | undefined;
} & typeof global;

function initRedis() {
  if (!globalThis.redisInstance) {
    console.log("Creating a new Redis connection...");
    globalThis.redisInstance = new Redis({
      db: process.env.LANGCHAIN_CACHE_REDIS_DB
        ? parseInt(process.env.LANGCHAIN_CACHE_REDIS_DB)
        : 11,
      port: process.env.LANGCHAIN_CACHE_REDIS_PORT
        ? parseInt(process.env.LANGCHAIN_CACHE_REDIS_PORT)
        : 6379,
      host: process.env.LANGCHAIN_CACHE_REDIS_HOST || "localhost",
    });

    globalThis.redisInstance.on("connect", () =>
      console.log("Redis connected"),
    );
    globalThis.redisInstance.on("error", (err) =>
      console.error("Redis Error:", err),
    );
  }

  if (!globalThis.redisCache) {
    globalThis.redisCache = new RedisCache(globalThis.redisInstance, {
      ttl: process.env.LANGCHAIN_CACHE_REDIS_TTL
        ? parseInt(process.env.LANGCHAIN_CACHE_REDIS_TTL)
        : 300,
    });
  }
}

export function getRedisCache(): RedisCache {
  if (!globalThis.redisCache) {
    initRedis();
  }
  if (!globalThis.redisCache) {
    throw new Error("Redis cache is not initialized");
  }
  return globalThis.redisCache;
}
