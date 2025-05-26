import { CacheHandler } from "@neshca/cache-handler";
import createLruHandler from "@neshca/cache-handler/local-lru";
import createRedisHandler from "@neshca/cache-handler/redis-strings";
import { createClient } from "redis";
import { PHASE_PRODUCTION_BUILD } from "next/constants.js";

CacheHandler.onCreation(async () => {
  let client;
  if (PHASE_PRODUCTION_BUILD !== process.env.NEXT_PHASE) {
    try {
      client = createClient({
        url: process.env.CACHE_HANDLER_REDIS_URL ?? "redis://localhost:6379",
        name: "next-cache-handler",
      });

      // Redis won't work without error handling. https://github.com/redis/node-redis?tab=readme-ov-file#events
      client.on("error", (error) => {
        if (typeof process.env.NEXT_PRIVATE_DEBUG_CACHE !== "undefined") {
          // Use logging with caution in production. Redis will flood your logs. Hide it behind a flag.
          console.error("Redis client error:", error);
        }
      });
    } catch (error) {
      console.warn("Failed to create Redis client:", error);
    }
  }

  if (client) {
    try {
      console.info("Connecting Redis client...");

      await Promise.race([
        client.connect().then(() => {
          console.info("Redis NextJS CacheHandler client connected.");
        }),

        new Promise((_, reject) =>
          setTimeout(
            () => reject(new Error("Redis connection timed out")),
            10000,
          ),
        ),
      ]);
    } catch (error) {
      console.warn("Failed to connect Redis client:", error);

      console.warn("Disconnecting the Redis client...");
      // Try to disconnect the client to stop it from reconnecting.
      client
        .disconnect()
        .then(() => {
          console.info("Redis client disconnected.");
        })
        .catch(() => {
          console.warn(
            "Failed to quit the Redis client after failing to connect.",
          );
        });
    }
  }

  /** @type {import("@neshca/cache-handler").Handler | null} */
  let handler;

  if (client?.isReady) {
    // Create the `redis-stack` Handler if the client is available and connected.
    handler = createRedisHandler({
      client,
      keyPrefix: "nextCacheHandler:",
      timeoutMs: 5000,
      keyExpirationStrategy: "EXAT",
      sharedTagsKey: "__sharedTags__",
      revalidateTagQuerySize: 150,
    });
  } else {
    // Fallback to LRU handler if Redis client is not available.
    // The application will still work, but the cache will be in memory only and not shared.
    handler = createLruHandler();
    console.error(
      "Falling back to LRU handler because Redis client is not available.",
    );
  }

  return {
    handlers: [handler],
  };
});

export default CacheHandler;
