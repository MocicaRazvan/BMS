"use client";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useSession } from "next-auth/react";
import { BaseError, isBaseError } from "@/types/responses";
import { AcceptHeader } from "@/types/fetch-utils";
import { isDeepEqual, stableStringify, wrapItemToString } from "@/lib/utils";
import murmur from "murmurhash";
import useCachedValue from "@/hoooks/use-cached-value";
import { deduplicateFetchStream } from "@/lib/fetchers/deduplicateFetchStream";
import { FetchStreamProps } from "@/lib/fetchers/fetchStream";
import { useCacheInvalidator } from "@/providers/cache-provider";

export interface UseFetchStreamProps {
  path: string;
  method?: "GET" | "POST" | "PUT" | "DELETE" | "HEAD" | "PATCH";
  body?: object | null;
  authToken?: boolean;
  customHeaders?: HeadersInit;
  queryParams?: Record<string, string>;
  arrayQueryParam?: Record<string, string[]>;
  cache?: RequestCache;
  acceptHeader?: AcceptHeader;
  useAbortController?: boolean;
  batchSize?: number;
  refetchOnFocus?: boolean;
  focusDelay?: number;
  trigger?: boolean;
}
type ManualFetcher<T> = (args: {
  fetchProps: Omit<
    FetchStreamProps<T>,
    "token" | "successArrayCallback" | "successCallback" | "errorCallback"
  >;
  localAuthToken?: boolean;
  batchCallback?: (data: T[], batchIndex: number) => void;
  aboveController?: AbortController;
  errorCallback?: (error: unknown) => void;
}) => Promise<void>;
export interface UseFetchStreamReturn<T, E> {
  messages: T[];
  error: E | null;
  isFinished: boolean;
  refetch: () => void;
  cacheKey: string;
  resetValueAndCache: () => void;
  removeFromCache: () => void;
  refetchState: boolean;
  isAbsoluteFinished: boolean;
  manualFetcher: ManualFetcher<T>;
  resetFinishes: () => void;
}

function generateKey(
  path: string,
  stableStringifyQueryParams: string,
  stableStringifyArrayQueryParam: string,
  stableStringifyBody: string,
  stableStringifyCustomHeaders: string,
  batchSize: number | undefined,
  method: "GET" | "POST" | "PUT" | "DELETE" | "HEAD" | "PATCH" | undefined,
  acceptHeader: "application/x-ndjson" | "application/json" | undefined,
) {
  return wrapItemToString(
    murmur.v3(
      `${path}-${stableStringifyQueryParams}-${stableStringifyArrayQueryParam}-${stableStringifyBody}-${stableStringifyCustomHeaders}-${batchSize}-${method}-${acceptHeader}`,
    ),
  );
}

export function useFetchStream<T = unknown, E extends BaseError = BaseError>({
  path,
  method = "GET",
  body = null,
  authToken = false,
  customHeaders = {},
  queryParams = {},
  cache = "no-cache",
  arrayQueryParam = {},
  acceptHeader = "application/x-ndjson",
  useAbortController = true,
  batchSize = 6,
  refetchOnFocus = true,
  focusDelay = 300,
  trigger = true,
}: UseFetchStreamProps): UseFetchStreamReturn<T, E> {
  const stableStringifyQueryParams = stableStringify(queryParams);
  const stableStringifyArrayQueryParam = stableStringify(arrayQueryParam);
  const stableStringifyBody = stableStringify(body);
  const stableStringifyCustomHeaders = stableStringify(customHeaders);

  const cacheKey = useMemo(
    () =>
      generateKey(
        path,
        stableStringifyQueryParams,
        stableStringifyArrayQueryParam,
        stableStringifyBody,
        stableStringifyCustomHeaders,
        batchSize,
        method,
        acceptHeader,
      ),
    [
      path,
      stableStringifyQueryParams,
      stableStringifyArrayQueryParam,
      stableStringifyBody,
      stableStringifyCustomHeaders,
      batchSize,
      method,
      acceptHeader,
    ],
  );
  const {
    value: messages,
    resetValueAndCache,
    isCacheKeyNotEmpty,
    handleBatchUpdate,
    finalSyncValueWithCache,
    replaceBatchInForAnyKey,
    removeFromCache,
  } = useCachedValue<T>(cacheKey, batchSize);

  const { removeArrayFromCache } = useCacheInvalidator();
  const [error, setError] = useState<E | null>(null);
  const [isFinished, setIsFinished] = useState<boolean>(false);
  const [refetchState, setRefetchState] = useState(false);
  const { data: session, status: sessionStatus } = useSession();
  const [isAbsoluteFinished, setIsAbsoluteFinished] = useState(false);
  const [manualKeys, setManualKeys] = useState<string[]>([]);
  const refetchClosure = useRef(false);

  const resetFinishes = useCallback(() => {
    setIsFinished(false);
    setIsAbsoluteFinished(false);
  }, []);

  const refetch = useCallback(() => {
    resetValueAndCache();
    removeArrayFromCache(manualKeys);
    setManualKeys([]);
    setError(null);
    resetFinishes();
    setRefetchState((prevIndex) => !prevIndex);
    refetchClosure.current = true;
  }, [manualKeys, removeArrayFromCache, resetFinishes, resetValueAndCache]);

  const fetcher = useCallback(
    async (
      isMounted: boolean,
      abortController: AbortController,
      fetchProps: FetchStreamProps<T>,
    ) => {
      if (!isMounted) return;
      try {
        const fetchFunction = await deduplicateFetchStream<T, E>({
          ...fetchProps,
          dedupKey: cacheKey,
        });

        if (
          "abort" in fetchFunction &&
          typeof fetchFunction.abort === "function"
        ) {
          (abortController as any).customAbort = () => {
            (fetchFunction as any)?.abort?.();
          };
        }

        for await (const batchItem of fetchFunction) {
          if ("data" in batchItem) {
            handleBatchUpdate(batchItem.data, batchItem.batchIndex);
          } else {
            const res = batchItem.response;
            if (res.isFinished) {
              setIsFinished(res.isFinished);
              setIsAbsoluteFinished(true);
              if (res.error) {
                throw res.error;
              } else {
                finalSyncValueWithCache();
              }
            }
          }
        }
      } catch (err) {
        if (!isMounted) return;
        // resetValue();
        console.log("Error fetching", err);
        if (err && err instanceof DOMException && err?.name === "AbortError") {
          return;
        } else if (isBaseError(err)) {
          setError(err as E);
          resetValueAndCache();
        }
        setIsFinished(true);
        setIsAbsoluteFinished(true);
      }
    },
    [cacheKey, finalSyncValueWithCache, handleBatchUpdate, resetValueAndCache],
  );

  const manualFetcher: ManualFetcher<T> = useCallback(
    async ({
      fetchProps,
      localAuthToken = false,
      batchCallback,
      aboveController,
      errorCallback,
    }) => {
      if (sessionStatus === "loading") {
        return;
      }
      if (localAuthToken && !session?.user?.token) {
        throw new Error("No token");
      }

      const updatedProps: typeof fetchProps = {
        body: null,
        arrayQueryParam: {},
        queryParams: {},
        customHeaders: {},
        batchSize: 6,
        method: "GET",
        ...fetchProps,
      };

      const stableStringifyQueryParams = stableStringify(
        updatedProps.queryParams,
      );
      const stableStringifyArrayQueryParam = stableStringify(
        updatedProps.arrayQueryParam,
      );
      const stableStringifyBody = stableStringify(updatedProps.body);
      const abortController = aboveController || new AbortController();
      const token =
        localAuthToken && session?.user?.token ? session.user.token : "";

      const key = generateKey(
        updatedProps.path,
        stableStringifyQueryParams,
        stableStringifyArrayQueryParam,
        stableStringifyBody,
        stableStringifyCustomHeaders,
        batchSize,
        method,
        acceptHeader,
      );

      setManualKeys((prev) => [...prev, key]);

      // console.log(
      //   "useFetchStream - manual",
      //   path,
      //   key,
      //   fetchProps.path,
      //   stableStringifyQueryParams,
      //   stableStringifyArrayQueryParam,
      //   stableStringifyBody,
      //   stableStringifyCustomHeaders,
      //   batchSize,
      //   method,
      //   acceptHeader,
      // );
      try {
        const fetchFunction = await deduplicateFetchStream<T, E>({
          ...updatedProps,
          token,
          dedupKey: key,
          extraOptions: {
            priority: "low",
          },
        });
        if (
          "abort" in fetchFunction &&
          typeof fetchFunction.abort === "function"
        ) {
          (abortController as any).customAbort = () => {
            (fetchFunction as any)?.abort?.();
          };
        }

        for await (const batchItem of fetchFunction) {
          if ("data" in batchItem) {
            replaceBatchInForAnyKey(batchItem.data, batchItem.batchIndex, key);
            if (batchCallback) {
              batchCallback(batchItem.data, batchItem.batchIndex);
            }
          } else {
            const res = batchItem.response;
            if (res.isFinished && res.error) {
              throw res.error;
            }
          }
        }
      } catch (err) {
        if (abortController && !abortController?.signal?.aborted) {
          abortController?.abort();
          (abortController as any)?.customAbort?.();
        }
        errorCallback?.(err);
        throw err;
      }
    },
    [
      sessionStatus,
      session?.user?.token,
      stableStringifyCustomHeaders,
      batchSize,
      method,
      acceptHeader,
      path,
      replaceBatchInForAnyKey,
    ],
  );

  useEffect(() => {
    if (!trigger) return;

    if (sessionStatus === "loading") {
      return;
    }
    if (authToken && !session?.user?.token) {
      return () => {
        console.log("No token");
      };
    }
    setError(null);
    const localFinished = refetchClosure.current ? false : isCacheKeyNotEmpty();
    setIsFinished(localFinished);
    setIsAbsoluteFinished(false);
    const token = authToken && session?.user?.token ? session.user.token : "";
    const abortController = new AbortController();

    const fetchProps: FetchStreamProps<T> = {
      path,
      method,
      body,
      customHeaders,
      queryParams,
      arrayQueryParam,
      token,
      cache,
      aboveController: abortController,
      batchSize,
      updateOnEmpty: true,
      acceptHeader,
    };

    // console.log(
    //   "useFetchStream - localFinished",
    //   path,
    //   localFinished,
    //   cacheKey,
    //   fetchProps.path,
    //   stableStringifyQueryParams,
    //   stableStringifyArrayQueryParam,
    //   stableStringifyBody,
    //   stableStringifyCustomHeaders,
    //   batchSize,
    //   method,
    //   acceptHeader,
    //   refetchClosure.current,
    // );
    let isMounted = true;

    //moves the async operations outside of React's render phase

    // const timeoutId = setTimeout(
    //   () =>
    //     fetcher(isMounted, abortController, fetchProps)
    //       .catch((e) => {
    //         if (isBaseError(e)) {
    //           setError((prev) => (isDeepEqual(prev, e) ? prev : (e as E)));
    //           setIsFinished((prev) => prev || true);
    //           setIsAbsoluteFinished((prev) => prev || true);
    //         }
    //       })
    //       .finally(() => {
    //         if (refetchClosure.current) {
    //           refetchClosure.current = false;
    //         }
    //       }),
    //   0,
    // );

    const doFetch = async () => {
      if (!isMounted) return;
      try {
        await fetcher(isMounted, abortController, fetchProps);
      } catch (e) {
        if (isBaseError(e)) {
          setError((prev) => (isDeepEqual(prev, e) ? prev : (e as E)));
          setIsFinished((prev) => prev || true);
          setIsAbsoluteFinished((prev) => prev || true);
        }
      } finally {
        if (refetchClosure.current) {
          refetchClosure.current = false;
        }
      }
    };
    Promise.resolve().then(doFetch);
    return () => {
      isMounted = false;
      // clearTimeout(timeoutId);
      try {
        if (
          // useAbortController &&
          abortController &&
          !abortController?.signal?.aborted
        ) {
          abortController?.abort();
          (abortController as any)?.customAbort?.();
          // resetValueAndCache();
        }
      } catch (e) {
        if (e && e instanceof DOMException && e?.name === "AbortError") {
          return;
        } else {
          console.log(e);
        }
      }
    };
  }, [
    path,
    method,
    authToken,
    session?.user?.token,
    sessionStatus,
    refetchState,
    batchSize,
    cacheKey,
    acceptHeader,
    useAbortController,
    isCacheKeyNotEmpty,
    fetcher,
    stableStringifyQueryParams,
    stableStringifyArrayQueryParam,
    stableStringifyBody,
    stableStringifyCustomHeaders,
    cache,
    trigger,
  ]);

  useEffect(() => {
    if (!refetchOnFocus) return;
    let unfocusedTime: number | null = null;
    const handleBlur = () => {
      unfocusedTime = Date.now();
    };
    const handleFocus = () => {
      if (unfocusedTime !== null) {
        const unfocusedDuration = Date.now() - unfocusedTime;
        if (unfocusedDuration >= focusDelay * 1000) {
          setRefetchState((prev) => !prev);
        }
        unfocusedTime = null;
      }
    };
    window.addEventListener("blur", handleBlur);
    window.addEventListener("focus", handleFocus);

    return () => {
      window.removeEventListener("blur", handleBlur);
      window.removeEventListener("focus", handleFocus);
    };
  }, [focusDelay, refetchOnFocus]);

  return {
    messages,
    error,
    isFinished,
    refetch,
    cacheKey,
    resetValueAndCache,
    removeFromCache,
    refetchState,
    isAbsoluteFinished,
    manualFetcher,
    resetFinishes,
  };
}

export default useFetchStream;
