"use client";
import { useCache } from "@/providers/cache-provider";
import { useCallback, useEffect, useRef, useState } from "react";
import { isDeepEqual } from "@/lib/utils";

export default function useCachedValue<T>(cacheKey: string, batchSize: number) {
  const {
    removeFromCache,
    getFromCache,
    isCacheKeyNotEmpty,
    isSameBatchInCache,
    replaceBatchInCacheWithCallback,
    replaceBatchInForAnyKey,
  } = useCache<T>(cacheKey);
  const [value, setValue] = useState<T[]>([]);
  const prevKeyRef = useRef<string | null>(null);

  useEffect(() => {
    if (prevKeyRef.current !== cacheKey) {
      const cacheData = getFromCache();
      setValue([...(cacheData ?? [])]);
      prevKeyRef.current = cacheKey;
    }
  }, [cacheKey, getFromCache]);

  const resetValueAndCache = useCallback(() => {
    setValue([]);
    removeFromCache();
    prevKeyRef.current = null;
  }, [removeFromCache]);

  const resetValue = useCallback(() => {
    setValue([]);
  }, []);

  const putCacheInValue = useCallback(() => {
    const cache = getFromCache();
    if (cache) {
      setValue((_) => [...cache]);
    }
  }, [getFromCache]);

  const replaceBatch = useCallback(
    (data: T[], batchIndex: number) => {
      replaceBatchInCacheWithCallback(data, batchIndex, () => {
        const updatedCache = getFromCache();
        // console.log(
        //   ` useCachedValue Updated cache after replaceBatch: ${cacheKey} at ${batchIndex}
        //   updatedCache: ${JSON.stringify(updatedCache) ?? "undefined"}
        //     data: ${JSON.stringify(data) ?? "undefined"}
        //   `,
        // );
        if (updatedCache) {
          setValue((_) => [...updatedCache]);
        }
      });
    },
    [replaceBatchInCacheWithCallback, getFromCache, cacheKey],
  );

  const handleBatchUpdate = useCallback(
    (data: T[], batchIndex: number) => {
      const isBatchTheSame = isSameBatchInCache(data, batchIndex, batchSize);
      if (isBatchTheSame) {
        // console.log(
        //   `useCachedValue Batch is the same, skipping update  ${cacheKey} at ${batchIndex} data: ${
        //     JSON.stringify(data) ?? "undefined"
        //   }
        //   `,
        // );
        return;
      }

      replaceBatch(data, batchIndex);
    },
    [batchSize, cacheKey, isSameBatchInCache, replaceBatch],
  );

  // to sync more requests on the same root, from key checking till cache value it can be not synced for all
  //todo some atomic lock would be nice
  const finalSyncValueWithCache = useCallback(() => {
    setValue((prev) => {
      const cachedValue = getFromCache();
      if (
        cachedValue &&
        cachedValue.length !== prev.length && // usually if it fails its here
        !isDeepEqual(cachedValue, prev)
      ) {
        return [...cachedValue];
      }
      return prev;
    });
  }, [getFromCache]);

  return {
    value,
    resetValueAndCache,
    putCacheInValue,
    replaceBatch,
    isCacheKeyNotEmpty,
    handleBatchUpdate,
    resetValue,
    getFromCache,
    finalSyncValueWithCache,
    removeFromCache,
    replaceBatchInForAnyKey,
  };
}
