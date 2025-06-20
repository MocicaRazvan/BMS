import { Redis } from "ioredis";
import { RedisCache } from "@langchain/community/caches/ioredis";
import { v3 as murmurV3 } from "murmurhash";
import { PHASE_PRODUCTION_BUILD } from "next/constants";

class CustomRedis extends Redis {
  static REDIS_TTL = process.env.LANGCHAIN_CACHE_REDIS_TTL
    ? parseInt(process.env.LANGCHAIN_CACHE_REDIS_TTL)
    : 300;
  setWithTTL(key: string, value: string, ttl?: number) {
    return this.set(key, value, "EX", ttl || CustomRedis.REDIS_TTL);
  }
}
declare const globalThis: {
  redisCache: RedisCache | undefined;
  redisInstance: CustomRedis | undefined;
} & typeof global;

export function generateHashKey(string: string, antet?: string) {
  return `${antet ? antet + ":" : ""}${murmurV3(string)}`;
}

function initRedis() {
  if (!globalThis.redisInstance) {
    const isBuildPhase = process.env.NEXT_PHASE === PHASE_PRODUCTION_BUILD;

    console.log("Creating a new Langhchain Redis connection...");
    globalThis.redisInstance = new CustomRedis({
      db: process.env.LANGCHAIN_CACHE_REDIS_DB
        ? parseInt(process.env.LANGCHAIN_CACHE_REDIS_DB)
        : 11,
      port: process.env.LANGCHAIN_CACHE_REDIS_PORT
        ? parseInt(process.env.LANGCHAIN_CACHE_REDIS_PORT)
        : 6379,
      host: process.env.LANGCHAIN_CACHE_REDIS_HOST || "localhost",
      name: "langchain-cache",
      lazyConnect: isBuildPhase,
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
      ttl: CustomRedis.REDIS_TTL,
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

export function getRedisInstance(): CustomRedis {
  if (!globalThis.redisInstance) {
    initRedis();
  }
  if (!globalThis.redisInstance) {
    throw new Error("Redis instance is not initialized");
  }
  return globalThis.redisInstance;
}
