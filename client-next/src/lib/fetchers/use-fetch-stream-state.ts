"use client";

import useCachedValue from "@/hoooks/use-cached-value";
import { useCacheInvalidator } from "@/providers/cache-provider";
import { useAdditionalFetchingReducer } from "@/lib/fetchers/additional-fetching-reducer";
import { useCallback } from "react";

interface UseFetchStreamStateArgs {
  cacheKey: string;
  batchSize: number;
}

export default function useFetchStreamState<T, E>({
  cacheKey,
  batchSize,
}: UseFetchStreamStateArgs) {
  const { value: messages, ...restCachedValue } = useCachedValue<T>(
    cacheKey,
    batchSize,
  );

  const { removeArrayFromCache } = useCacheInvalidator();
  const {
    setFinishes,
    reset: resetAdditionalArgs,
    ...restAdditional
  } = useAdditionalFetchingReducer<E>();

  const resetFinishes = useCallback(() => {
    setFinishes({ isFinished: false, isAbsoluteFinished: false });
  }, [setFinishes]);

  return {
    messages,
    ...restCachedValue,
    ...restAdditional,
    resetFinishes,
    removeArrayFromCache,
    setFinishes,
    resetAdditionalArgs,
  };
}
