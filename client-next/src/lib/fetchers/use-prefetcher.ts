"use client";

import { useCallback, useEffect, useRef } from "react";
import { BaseError } from "@/types/responses";

import {
  UseFetchStreamProps,
  UseFetchStreamReturn,
} from "@/lib/fetchers/useFetchStream";
import { CustomAbortController } from "@/lib/fetchers/custom-abort-controller";
import { debounce } from "lodash-es";

const DUMMY_VALUE = "dummy_value" as const;

export type PrefetchedHas = (keyValue: [string, string]) => boolean;
export type FlattenPrefetchedHas = (key: string) => boolean;
export type PrefetchedMarked = (keyValue: [string, string]) => void;
export type FlattenPrefetchedMarked = (key: string) => void;
export type PrefetchGenerateKeyValue<T> = (messages: T[]) => [string, string];
export type FlattenPrefetchGenerateKeyValue<T> = (messages: T[]) => string;
export type PrefetchedPredicate<T> = (
  messages: T[],
  hasPrefetched: PrefetchedHas,
) => boolean;
export type FlattenPrefetchedPredicate<T> = (
  messages: T[],
  hasPrefetched: FlattenPrefetchedHas,
) => boolean;
export type PrefetchGenerateNewArgs<T> = (messages: T[]) => UseFetchStreamProps;
export type PrefetchGenerateMarkPrefetchedArgs<T> = (
  messages: T[],
  newArgs: UseFetchStreamProps,
) => [string, string];
export type FlattenPrefetchGenerateMarkPrefetchedArgs<T> = (
  messages: T[],
  newArgs: UseFetchStreamProps,
) => string;

export type UseFetchStreamPrefetcherReturn<
  T,
  E extends BaseError = BaseError,
> = Pick<
  UseFetchStreamReturn<T, E>,
  | "messages"
  | "error"
  | "manualFetcher"
  | "isAbsoluteFinished"
  | "isRefetchClosure"
>;

export type UseFetchStreamPrefetcherProps<
  T,
  E extends BaseError = BaseError,
> = {
  returned: UseFetchStreamPrefetcherReturn<T, E>;
  preloadNext?: boolean;
  generateKeyValue: PrefetchGenerateKeyValue<T>;
  nextPredicate: PrefetchedPredicate<T>;
  previousPredicate: PrefetchedPredicate<T>;
  generateNextArgs: PrefetchGenerateNewArgs<T>;
  generatePreviousArgs: PrefetchGenerateNewArgs<T>;
  generateMarkPrefetchedNextArgs: PrefetchGenerateMarkPrefetchedArgs<T>;
  generateMarkPrefetchedPreviousArgs: PrefetchGenerateMarkPrefetchedArgs<T>;
  additionalKey?: string;
  delayPrefetch?: number;
};

type PrefetchOptions<T, E extends BaseError = BaseError> = {
  isAbsoluteFinished: boolean;
  preloadNext: boolean;
  messages: T[];
  hasPrefetched: PrefetchedHas;
  predicate: PrefetchedPredicate<T>;
  generateArgs: PrefetchGenerateNewArgs<T>;
  generateMark: (
    messages: T[],
    newArgs: UseFetchStreamProps,
  ) => [string, string];
  basePrefetcher: (
    predicate: PrefetchedPredicate<T>,
    generateArgs: PrefetchGenerateNewArgs<T>,
    generateMark: PrefetchGenerateMarkPrefetchedArgs<T>,
    controller: CustomAbortController,
  ) => Promise<void>;
  delayMs: number;
};

const NOOP_VALUE = 0;
const MODE_VALUE = 2 ** 32;
function useDebouncedIdlePrefetch<T, E extends BaseError = BaseError>({
  basePrefetcher,
  isAbsoluteFinished,
  preloadNext,
  predicate,
  messages,
  hasPrefetched,
  delayMs,
  generateArgs,
  generateMark,
}: PrefetchOptions<T, E>) {
  const instanceIdRef = useRef(NOOP_VALUE);

  useEffect(() => {
    if (
      !isAbsoluteFinished ||
      !preloadNext ||
      !predicate(messages, hasPrefetched)
    ) {
      return;
    }

    instanceIdRef.current = (instanceIdRef.current + 1) % MODE_VALUE;
    const instanceId = instanceIdRef.current;
    const abortController = new CustomAbortController();

    let mounted = true;

    const debouncedFetch = debounce(
      () => {
        if (
          abortController.signal.aborted ||
          !mounted ||
          instanceIdRef.current !== instanceId
        ) {
          return;
        }

        basePrefetcher(
          predicate,
          generateArgs,
          generateMark,
          abortController,
        ).catch((e) => {
          console.log("useDebouncedIdlePrefetch error:", e);
        });
      },
      delayMs,
      {
        leading: false,
        trailing: true,
      },
    );

    const cbId = requestIdleCallback(() => {
      if (
        abortController.signal.aborted ||
        !mounted ||
        instanceIdRef.current !== instanceId
      ) {
        return;
      }

      debouncedFetch();
    });

    return () => {
      mounted = false;
      cancelIdleCallback(cbId);
      debouncedFetch.cancel();
      if (!abortController.signal.aborted) {
        abortController.abort();
      }
    };
  }, [
    basePrefetcher,
    delayMs,
    generateArgs,
    generateMark,
    hasPrefetched,
    isAbsoluteFinished,
    messages,
    predicate,
    preloadNext,
  ]);
}

export type FlattedPrefetcherProps<T, E extends BaseError = BaseError> = Pick<
  UseFetchStreamPrefetcherProps<T, E>,
  | "returned"
  | "preloadNext"
  | "generateNextArgs"
  | "generatePreviousArgs"
  | "additionalKey"
> & {
  generateKeyValue: FlattenPrefetchGenerateKeyValue<T>;
  generateMarkPrefetchedNextArgs: FlattenPrefetchGenerateMarkPrefetchedArgs<T>;
  generateMarkPrefetchedPreviousArgs: FlattenPrefetchGenerateMarkPrefetchedArgs<T>;
  nextPredicate: FlattenPrefetchedPredicate<T>;
  previousPredicate: FlattenPrefetchedPredicate<T>;
};

const DEFAULT_DELAY = 750 as const;

export default function usePrefetcher<T, E extends BaseError = BaseError>({
  returned: {
    messages,
    error,
    manualFetcher,
    isAbsoluteFinished,
    isRefetchClosure,
  },
  preloadNext = true,
  generateKeyValue,
  nextPredicate,
  previousPredicate,
  generateNextArgs,
  generatePreviousArgs,
  generateMarkPrefetchedNextArgs,
  generateMarkPrefetchedPreviousArgs,
  additionalKey = "",
  delayPrefetch = DEFAULT_DELAY,
}: UseFetchStreamPrefetcherProps<T, E>) {
  const prefetchedMap = useRef<Map<string, Set<string>>>(new Map());

  const createWithAdditionalKey = useCallback(
    (key: string) => `${key}${additionalKey}`,
    [additionalKey],
  );

  const hasPrefetched: PrefetchedHas = useCallback(
    ([key, value]) => {
      const set = prefetchedMap.current.get(createWithAdditionalKey(key));
      return set ? set.has(value) : false;
    },
    [createWithAdditionalKey],
  );
  const markPrefetched: PrefetchedMarked = useCallback(
    ([key, value]) => {
      const setKey = createWithAdditionalKey(key);
      let set = prefetchedMap.current.get(setKey);
      if (!set) {
        set = new Set<string>();
        prefetchedMap.current.set(setKey, set);
      }
      set.add(value);
    },
    [createWithAdditionalKey],
  );

  const unmarkPrefetched: PrefetchedMarked = useCallback(
    ([key, value]) => {
      const setKey = createWithAdditionalKey(key);
      const set = prefetchedMap.current.get(setKey);
      if (set) {
        set.delete(value);
        if (set.size === 0) {
          prefetchedMap.current.delete(setKey);
        }
      }
    },
    [createWithAdditionalKey],
  );

  const cleanUpPrefetched = useCallback(() => {
    prefetchedMap.current.clear();
  }, []);

  useEffect(() => {
    if (isAbsoluteFinished && messages?.length) {
      markPrefetched(generateKeyValue(messages));
    }
  }, [generateKeyValue, isAbsoluteFinished, markPrefetched, messages]);

  useEffect(() => {
    if (isRefetchClosure) {
      cleanUpPrefetched();
    }
  }, [cleanUpPrefetched, isRefetchClosure]);

  const basePrefetcher = useCallback(
    async (
      predicate: PrefetchedPredicate<T>,
      generateArgs: PrefetchGenerateNewArgs<T>,
      generateMarkPrefetched: PrefetchGenerateMarkPrefetchedArgs<T>,
      abortController: CustomAbortController,
    ) => {
      if (
        isAbsoluteFinished &&
        preloadNext &&
        messages &&
        !abortController.signal.aborted &&
        !error &&
        predicate(messages, hasPrefetched)
      ) {
        const newArgs = generateArgs(messages);
        const prefetchedKey = generateMarkPrefetched(messages, newArgs);
        try {
          if (abortController.signal.aborted) {
            return;
          }
          await manualFetcher({
            fetchProps: newArgs,
            aboveController: abortController,
            localAuthToken: true,
          });

          if (!abortController.signal.aborted) {
            markPrefetched(prefetchedKey);
          }
        } catch (e: any) {
          if (e?.name !== "AbortError") {
            console.log("basePrefetcher  error:", e);
            unmarkPrefetched(prefetchedKey);
          }
        }
      }
    },
    [
      error,
      hasPrefetched,
      isAbsoluteFinished,
      manualFetcher,
      markPrefetched,
      messages,
      preloadNext,
      unmarkPrefetched,
    ],
  );

  useDebouncedIdlePrefetch<T, E>({
    generateMark: generateMarkPrefetchedNextArgs,
    generateArgs: generateNextArgs,
    basePrefetcher,
    isAbsoluteFinished,
    preloadNext,
    predicate: nextPredicate,
    messages,
    hasPrefetched,
    delayMs: delayPrefetch,
  });

  useDebouncedIdlePrefetch<T, E>({
    generateMark: generateMarkPrefetchedPreviousArgs,
    generateArgs: generatePreviousArgs,
    basePrefetcher,
    isAbsoluteFinished,
    preloadNext,
    predicate: previousPredicate,
    messages,
    hasPrefetched,
    delayMs: delayPrefetch,
  });

  return {
    prefetchedMap: prefetchedMap.current,
    cleanUpPrefetched,
    hasPrefetched,
    markPrefetched,
    unmarkPrefetched,
  };
}

export function useFlattenPrefetcher<T, E extends BaseError = BaseError>({
  generateKeyValue,
  nextPredicate,
  previousPredicate,
  generateMarkPrefetchedNextArgs,
  generateMarkPrefetchedPreviousArgs,
  ...rest
}: FlattedPrefetcherProps<T, E>) {
  const wrappedGenerateKeyValue: PrefetchGenerateKeyValue<T> = useCallback(
    (messages) => [generateKeyValue(messages), DUMMY_VALUE],
    [generateKeyValue],
  );

  const wrappedGenerateMarkPrefetchedNextArgs: PrefetchGenerateMarkPrefetchedArgs<T> =
    useCallback(
      (messages, newArgs) => [
        generateMarkPrefetchedNextArgs(messages, newArgs),
        DUMMY_VALUE,
      ],
      [generateMarkPrefetchedNextArgs],
    );
  const wrappedGenerateMarkPrefetchedPreviousArgs: PrefetchGenerateMarkPrefetchedArgs<T> =
    useCallback(
      (messages, newArgs) => [
        generateMarkPrefetchedPreviousArgs(messages, newArgs),
        DUMMY_VALUE,
      ],
      [generateMarkPrefetchedPreviousArgs],
    );
  const wrappedNextPredicate: PrefetchedPredicate<T> = useCallback(
    (messages, hasPrefetched) =>
      nextPredicate(messages, (key) => hasPrefetched([key, DUMMY_VALUE])),
    [nextPredicate],
  );
  const wrappedPreviousPredicate: PrefetchedPredicate<T> = useCallback(
    (messages, hasPrefetched) =>
      previousPredicate(messages, (key) => hasPrefetched([key, DUMMY_VALUE])),
    [previousPredicate],
  );

  return usePrefetcher<T, E>({
    generateKeyValue: wrappedGenerateKeyValue,
    generateMarkPrefetchedNextArgs: wrappedGenerateMarkPrefetchedNextArgs,
    generateMarkPrefetchedPreviousArgs:
      wrappedGenerateMarkPrefetchedPreviousArgs,
    nextPredicate: wrappedNextPredicate,
    previousPredicate: wrappedPreviousPredicate,
    ...rest,
  });
}
