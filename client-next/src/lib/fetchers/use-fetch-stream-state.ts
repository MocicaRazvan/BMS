"use client";

import {
  useCacheInvalidator,
  useFlattenCachedValue,
} from "@/providers/cache-provider";
import { useAdditionalFetchingReducer } from "@/lib/fetchers/additional-fetching-reducer";
import { useCallback } from "react";

interface UseFetchStreamStateArgs {
  cacheKey: string;
}

export default function useFetchStreamState<T, E>({
  cacheKey,
}: UseFetchStreamStateArgs) {
  const { value: messages, ...restCachedValue } =
    useFlattenCachedValue<T>(cacheKey);

  const { removeArrayFromCache, isKeyInCache } = useCacheInvalidator();
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
    setFinishes,
    resetAdditionalArgs,
    removeArrayFromCache,
    isKeyInCache,
  };
}
