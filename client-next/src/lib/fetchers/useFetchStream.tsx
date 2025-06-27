"use client";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useSession } from "next-auth/react";
import { BaseError, isBaseError } from "@/types/responses";
import { AcceptHeader } from "@/types/fetch-utils";
import { isDeepEqual, stableStringify, wrapItemToString } from "@/lib/utils";
import { v3 as murmurV3 } from "murmurhash";
import { deduplicateFetchStream } from "@/lib/fetchers/deduplicateFetchStream";
import { FetchStreamProps } from "@/lib/fetchers/fetchStream";
import { useDeepCompareMemo } from "@/hoooks/use-deep-memo";
import useFetchStreamState from "@/lib/fetchers/use-fetch-stream-state";
import { CustomAbortController } from "@/lib/fetchers/custom-abort-controller";

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
  batchSize?: number;
  refetchOnFocus?: boolean;
  focusDelay?: number;
  trigger?: boolean;
  aboveController?: CustomAbortController;
  onBlurCallback?: () => void;
  beforeFetchCallback?: () => void;
  afterFetchCallback?: () => void;
  prefetchOverrideCache?: boolean;
}
type ManualFetcher<T> = (args: {
  fetchProps: Omit<
    FetchStreamProps<T>,
    "token" | "successArrayCallback" | "successCallback" | "errorCallback"
  >;
  localAuthToken?: boolean;
  batchCallback?: (data: T[], batchIndex: number) => void;
  aboveController?: CustomAbortController;
  errorCallback?: (error: unknown) => void;
}) => Promise<void>;
export interface UseFetchStreamReturn<T, E> {
  messages: T[];
  error: E | null;
  isFinished: boolean;
  refetch: () => void;
  cacheKey: string;
  removeFromCache: () => void;
  refetchState: boolean;
  isAbsoluteFinished: boolean;
  manualFetcher: ManualFetcher<T>;
  resetFinishes: () => void;
  isRefetchClosure: boolean;
}

function generateKey(
  args: Pick<
    UseFetchStreamProps,
    | "path"
    | "queryParams"
    | "arrayQueryParam"
    | "body"
    | "customHeaders"
    | "batchSize"
    | "method"
    | "acceptHeader"
  >,
) {
  const stringified = Object.keys(args)
    .sort()
    .reduce(
      (acc, key) => acc + stableStringify(args[key as keyof typeof args]) + "-",
      "",
    );
  return wrapItemToString(murmurV3(stringified));
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
  batchSize = 6,
  refetchOnFocus = true,
  focusDelay = 300,
  trigger = true,
  onBlurCallback,
  aboveController,
  beforeFetchCallback,
  afterFetchCallback,
  prefetchOverrideCache = false,
}: UseFetchStreamProps): UseFetchStreamReturn<T, E> {
  const stableQueryParams = useDeepCompareMemo(
    () => queryParams,
    [queryParams],
  );
  const stableArrayQueryParam = useDeepCompareMemo(
    () => arrayQueryParam,
    [arrayQueryParam],
  );
  const stableBody = useDeepCompareMemo(() => body, [body]);
  const stableCustomHeaders = useDeepCompareMemo(
    () => customHeaders,
    [customHeaders],
  );

  const cacheKey = useMemo(
    () =>
      generateKey({
        path,
        method,
        body: stableBody,
        customHeaders: stableCustomHeaders,
        queryParams: stableQueryParams,
        arrayQueryParam: stableArrayQueryParam,
        batchSize,
      }),
    [
      path,
      method,
      stableBody,
      stableCustomHeaders,
      stableQueryParams,
      stableArrayQueryParam,
      batchSize,
    ],
  );
  const {
    messages,
    isCacheKeyNotEmpty,
    handleBatchUpdate,
    replaceBatchInForAnyKey,
    removeFromCache,
    removeArrayFromCache,
    isFinished,
    isAbsoluteFinished,
    error,
    resetFinishes,
    resetAdditionalArgs,
    setErrorWithFinishes,
    setFinishes,
    isKeyInCache,
  } = useFetchStreamState<T, E>({ cacheKey });
  const [refetchState, setRefetchState] = useState(false);
  const { data: session, status: sessionStatus } = useSession();
  const maybeSessionToken = session?.user?.token;
  // for some very rare edge cases, but it can be removed now with dedup
  const refetchClosure = useRef(false);
  const historyKeys = useRef<Set<string>>(new Set());

  const refetch = useCallback(() => {
    if (refetchClosure.current) {
      return;
    }

    removeFromCache();
    removeArrayFromCache(historyKeys.current);
    resetAdditionalArgs();

    refetchClosure.current = true;
    historyKeys.current.clear();

    setRefetchState((prevIndex) => !prevIndex);
  }, [removeArrayFromCache, resetAdditionalArgs, removeFromCache]);

  const fetcher = useCallback(
    async (
      abortController: CustomAbortController,
      fetchProps: FetchStreamProps<T>,
    ) => {
      if (abortController.signal.aborted) {
        return;
      }
      try {
        const fetchFunction = await deduplicateFetchStream<T, E>({
          ...fetchProps,
          dedupKey: cacheKey,
        });

        if (abortController.signal.aborted) {
          return;
        }

        abortController.setAdditionalAbortFromFetch(fetchFunction);

        for await (const batchItem of fetchFunction) {
          if (abortController.signal.aborted) {
            return;
          }
          if ("data" in batchItem) {
            handleBatchUpdate(batchItem.data, batchItem.batchIndex);
          } else {
            const res = batchItem.response;
            if (res.isFinished) {
              setFinishes({
                isFinished: true,
                isAbsoluteFinished: true,
              });
              if (res.error) {
                throw res.error;
              } else {
                historyKeys.current.add(cacheKey);
              }
              break;
            }
          }
        }
      } catch (err) {
        // resetValue();
        console.log("Error fetching", err);
        if (err && err instanceof DOMException && err?.name === "AbortError") {
          return;
        } else if (!abortController.signal.aborted) {
          if (isBaseError(err)) {
            setErrorWithFinishes({
              error: err as E,
              isFinished: true,
              isAbsoluteFinished: true,
            });
            removeFromCache();
            historyKeys.current.delete(cacheKey);
            // resetValueAndCache();
          } else {
            setFinishes({
              isFinished: true,
              isAbsoluteFinished: true,
            });
          }
        }
      }
    },
    [
      cacheKey,
      handleBatchUpdate,
      removeFromCache,
      setErrorWithFinishes,
      setFinishes,
    ],
  );

  const manualFetcher: ManualFetcher<T> = useCallback(
    async ({
      fetchProps,
      localAuthToken = false,
      batchCallback,
      aboveController,
      errorCallback,
    }) => {
      if (aboveController && aboveController.signal.aborted) {
        return;
      }

      if (sessionStatus === "loading") {
        return;
      }
      if (localAuthToken && !maybeSessionToken) {
        throw new Error("No token");
      }

      const abortController = aboveController || new CustomAbortController();
      const token =
        localAuthToken && maybeSessionToken ? maybeSessionToken : "";
      const updatedProps: typeof fetchProps = {
        body: null,
        arrayQueryParam: {},
        queryParams: {},
        customHeaders: {},
        batchSize: 6,
        method: "GET",
        ...fetchProps,
      };

      const key = generateKey({
        path: updatedProps.path,
        method: updatedProps.method,
        body: updatedProps.body,
        customHeaders: updatedProps.customHeaders,
        queryParams: updatedProps.queryParams,
        arrayQueryParam: updatedProps.arrayQueryParam,
        batchSize: updatedProps.batchSize,
      });

      if (historyKeys.current.has(key)) {
        return;
      }

      if (isKeyInCache(key) && !prefetchOverrideCache) {
        historyKeys.current.add(key);
        return;
      }

      try {
        const fetchFunction = await deduplicateFetchStream<T, E>({
          ...updatedProps,
          token,
          dedupKey: key,
          extraOptions: {
            ...updatedProps.extraOptions,
            priority: "low",
          },
        });

        if (abortController.signal.aborted) {
          return;
        }

        abortController.setAdditionalAbortFromFetch(fetchFunction);

        for await (const batchItem of fetchFunction) {
          if (abortController.signal.aborted) {
            return;
          }
          if ("data" in batchItem) {
            replaceBatchInForAnyKey(batchItem.data, batchItem.batchIndex, key);
            if (batchCallback) {
              batchCallback(batchItem.data, batchItem.batchIndex);
            }
          } else {
            const res = batchItem.response;
            if (res.isFinished) {
              if (res.error) {
                historyKeys.current.delete(key);
                throw res.error;
              } else {
                historyKeys.current.add(key);
              }
              break;
            }
          }
        }
      } catch (err) {
        if (err && err instanceof DOMException && err?.name === "AbortError") {
          return;
        }

        if (abortController && !abortController?.signal?.aborted) {
          abortController?.abort();
        }
        errorCallback?.(err);
      }
    },
    [
      sessionStatus,
      maybeSessionToken,
      isKeyInCache,
      prefetchOverrideCache,
      replaceBatchInForAnyKey,
    ],
  );

  useEffect(() => {
    if (!trigger) return;

    if (sessionStatus === "loading") {
      return;
    }

    if (authToken && !maybeSessionToken) {
      return () => {
        console.log("No token");
      };
    }

    if (aboveController && aboveController.signal.aborted) {
      return;
    }

    const localFinished = refetchClosure.current ? false : isCacheKeyNotEmpty();

    if (
      prefetchOverrideCache &&
      localFinished &&
      isKeyInCache(cacheKey) && // no stale
      historyKeys.current.has(cacheKey)
    ) {
      // check if this makes stale refetch
      refetchClosure.current = false;
      return;
    }

    const abortController = aboveController || new CustomAbortController();

    let mounted = true;

    setErrorWithFinishes({
      error: null,
      isFinished: localFinished,
      isAbsoluteFinished: false,
    });

    const token = authToken && maybeSessionToken ? maybeSessionToken : "";

    const fetchProps: FetchStreamProps<T> = {
      path,
      method,
      body: stableBody,
      customHeaders: stableCustomHeaders,
      queryParams: stableQueryParams,
      arrayQueryParam: stableArrayQueryParam,
      token,
      cache,
      aboveController: abortController,
      batchSize,
      updateOnEmpty: true,
      acceptHeader,
    };

    const doFetch = async () => {
      if (abortController.signal.aborted || !mounted) {
        return;
      }
      try {
        beforeFetchCallback?.();
        if (abortController.signal.aborted || !mounted) return;

        await fetcher(abortController, fetchProps);

        if (mounted && !abortController.signal.aborted) {
          historyKeys.current.add(cacheKey);
        }
      } catch (e) {
        if (isBaseError(e) && !abortController.signal.aborted) {
          setErrorWithFinishes({
            error: (prev) => (isDeepEqual(prev, e) ? prev : (e as E)),
            isFinished: true,
            isAbsoluteFinished: true,
          });
        }
      } finally {
        // check if this makes stale refetch
        if (mounted && !abortController.signal.aborted) {
          refetchClosure.current = false;

          afterFetchCallback?.();
        }
      }
    };

    //moves the async operations outside of React's render phase
    (async () => {
      if (!mounted || abortController.signal.aborted) return;
      await doFetch();
    })();

    return () => {
      mounted = false;
      try {
        if (abortController && !abortController.signal.aborted) {
          abortController.abort(!refetchClosure.current);
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
    sessionStatus,
    refetchState,
    batchSize,
    cacheKey,
    acceptHeader,
    isCacheKeyNotEmpty,
    fetcher,
    cache,
    trigger,
    setErrorWithFinishes,
    maybeSessionToken,
    stableBody,
    stableCustomHeaders,
    stableQueryParams,
    stableArrayQueryParam,
    beforeFetchCallback,
    afterFetchCallback,
    isKeyInCache,
    // intentionally omitted aboveController, it's only used for optional cleanup and should not trigger refetch
    // aboveController,
  ]);

  useEffect(() => {
    if (!refetchOnFocus) return;

    let mounted = true;
    let unfocusedTime: number | null = null;
    const handleBlur = () => {
      unfocusedTime = Date.now();
      onBlurCallback?.();
    };
    const handleFocus = () => {
      if (unfocusedTime !== null && mounted) {
        const unfocusedDuration = Date.now() - unfocusedTime;
        if (unfocusedDuration >= focusDelay * 1000) {
          refetch();
        }
        unfocusedTime = null;
      }
    };
    window.addEventListener("blur", handleBlur);
    window.addEventListener("focus", handleFocus);

    return () => {
      mounted = false;
      window.removeEventListener("blur", handleBlur);
      window.removeEventListener("focus", handleFocus);
    };
  }, [focusDelay, onBlurCallback, refetch, refetchOnFocus]);

  return {
    messages,
    error,
    isFinished,
    refetch,
    cacheKey,
    removeFromCache,
    refetchState,
    isAbsoluteFinished,
    manualFetcher,
    resetFinishes,
    isRefetchClosure: refetchClosure.current,
  };
}

export default useFetchStream;
