import { Redis } from "ioredis";
import { RedisCache } from "@langchain/community/caches/ioredis";

declare const globalThis: {
  redisCache: RedisCache | undefined;
} & typeof global;

const redisDb = process.env.LANGCHAIN_CACHE_REDIS_DB
  ? parseInt(process.env.LANGCHAIN_CACHE_REDIS_DB)
  : 11;
const redisPort = process.env.LANGCHAIN_CACHE_REDIS_PORT
  ? parseInt(process.env.LANGCHAIN_CACHE_REDIS_PORT)
  : 6379;
const redisHost = process.env.LANGCHAIN_CACHE_REDIS_HOST
  ? process.env.LANGCHAIN_CACHE_REDIS_HOST
  : "localhost";
const redisTTL = process.env.LANGCHAIN_CACHE_REDIS_TTL
  ? parseInt(process.env.LANGCHAIN_CACHE_REDIS_TTL)
  : 300;

export function getRedisCache(): RedisCache {
  if (!globalThis.redisCache) {
    const redisCon = new Redis({
      db: redisDb,
      port: redisPort,
      host: redisHost,
    });
    globalThis.redisCache = new RedisCache(redisCon, {
      ttl: redisTTL,
    });
  }
  return globalThis.redisCache;
}
