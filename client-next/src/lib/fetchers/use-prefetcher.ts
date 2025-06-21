"use client";

import { useCallback, useEffect, useRef, useState } from "react";
import { BaseError } from "@/types/responses";

import {
  UseFetchStreamProps,
  UseFetchStreamReturn,
} from "@/lib/fetchers/useFetchStream";
import { CustomAbortController } from "@/lib/fetchers/custom-abort-controller";
import { useMountedState } from "react-use";

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
};

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
}: UseFetchStreamPrefetcherProps<T, E>) {
  const [nextMessages, setNextMessages] = useState<T[] | null>(null);
  const [previousMessages, setPreviousMessages] = useState<T[] | null>(null);
  const prefetchedMap = useRef<Map<string, Set<string>>>(new Map());
  const isMounted = useMountedState();

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
    (
      predicate: PrefetchedPredicate<T>,
      generateArgs: PrefetchGenerateNewArgs<T>,
      generateMarkPrefetched: PrefetchGenerateMarkPrefetchedArgs<T>,
      abortController: CustomAbortController,
      batchCallback: (data: T[]) => void,
    ) => {
      if (
        isAbsoluteFinished &&
        preloadNext &&
        messages &&
        isMounted() &&
        !error &&
        predicate(messages, hasPrefetched)
      ) {
        const newArgs = generateArgs(messages);
        const prefetchedKey = generateMarkPrefetched(messages, newArgs);
        markPrefetched(prefetchedKey);
        Promise.resolve()
          .then(() => {
            if (!isMounted()) {
              unmarkPrefetched(prefetchedKey);
              return;
            }
            return manualFetcher({
              fetchProps: newArgs,
              aboveController: abortController,
              localAuthToken: true,
              batchCallback: (data) => {
                if (data.length > 0) {
                  batchCallback(data);
                }
              },
              errorCallback: () => {
                setNextMessages(null);
              },
            });
          })
          .catch((e) => {
            unmarkPrefetched(prefetchedKey);
            console.log("manualFetcher Error fetching", e);
          });
      }
    },
    [
      error,
      hasPrefetched,
      isAbsoluteFinished,
      isMounted,
      manualFetcher,
      markPrefetched,
      messages,
      preloadNext,
      unmarkPrefetched,
    ],
  );

  // fine bc dedup handles it
  // preload next page
  useEffect(() => {
    const abortController = new CustomAbortController();

    basePrefetcher(
      nextPredicate,
      generateNextArgs,
      generateMarkPrefetchedNextArgs,
      abortController,
      (data) => setNextMessages((prev) => [...(prev || []), ...data]),
    );

    return () => {
      if (abortController && !abortController?.signal?.aborted) {
        abortController?.abort();
      }
    };
  }, [
    basePrefetcher,
    generateMarkPrefetchedNextArgs,
    generateNextArgs,
    nextPredicate,
  ]);

  // preload previous page
  useEffect(() => {
    const abortController = new CustomAbortController();

    basePrefetcher(
      previousPredicate,
      generatePreviousArgs,
      generateMarkPrefetchedPreviousArgs,
      abortController,
      (data) => setPreviousMessages((prev) => [...data, ...(prev || [])]),
    );

    return () => {
      if (abortController && !abortController?.signal?.aborted) {
        abortController?.abort();
      }
    };
  }, [
    basePrefetcher,
    generateMarkPrefetchedPreviousArgs,
    generatePreviousArgs,
    previousPredicate,
  ]);

  return {
    nextMessages,
    previousMessages,
    prefetchedMap: prefetchedMap.current,
    cleanUpPrefetched,
    hasPrefetched,
    markPrefetched,
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
