"use client";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useSession } from "next-auth/react";
import { BaseError } from "@/types/responses";
import { fetchStream, FetchStreamProps } from "../lib/fetchers/fetchStream";
import { AcceptHeader } from "@/types/fetch-utils";
import { wrapItemToString } from "@/lib/utils";
import murmur from "murmurhash";
import useCachedValue from "@/hoooks/use-cached-value";
import { useDeepCompareEffect } from "react-use";

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
}

export interface UseFetchStreamReturn<T, E> {
  messages: T[];
  error: E | null;
  isFinished: boolean;
  refetch: () => void;
}
function stableStringify(obj: any): string {
  if (obj === undefined) {
    return "undefined";
  }
  if (obj === null || typeof obj !== "object") return JSON.stringify(obj);

  return JSON.stringify(
    Object.keys(obj)
      .sort()
      .reduce(
        (sortedObj, key) => {
          sortedObj[key] = obj[key];
          return sortedObj;
        },
        {} as Record<string, any>,
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
  focusDelay = 20,
}: UseFetchStreamProps): UseFetchStreamReturn<T, E> {
  const cacheKey = useMemo(
    () =>
      wrapItemToString(
        murmur.v3(
          `${path}-${stableStringify(queryParams)}-${stableStringify(arrayQueryParam)}-${stableStringify(body)}-${stableStringify(customHeaders)}-${batchSize}-${method}`,
        ),
      ),
    [
      path,
      JSON.stringify(queryParams),
      JSON.stringify(arrayQueryParam),
      JSON.stringify(body),
      JSON.stringify(customHeaders),
      batchSize,
      method,
    ],
  );

  const {
    value: messages,
    resetValueAndCache,
    isCacheKeyNotEmpty,
    handleBatchUpdate,
    finalSyncValueWithCache,
  } = useCachedValue<T>(cacheKey, batchSize);
  const [error, setError] = useState<E | null>(null);
  const [isFinished, setIsFinished] = useState<boolean>(false);
  const [refetchState, setRefetchState] = useState(false);
  const { data: session } = useSession();

  const refetch = useCallback(() => {
    resetValueAndCache();
    setIsFinished(false);
    setRefetchState((prevIndex) => !prevIndex);
  }, [resetValueAndCache]);

  useDeepCompareEffect(() => {
    setError(null);
    const localFinished = isCacheKeyNotEmpty();
    setIsFinished(localFinished);

    if (authToken && !session?.user?.token) {
      return () => {
        console.log("No token");
      };
    }

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

      successArrayCallback: (data, batchIndex) => {
        handleBatchUpdate(data, batchIndex);
      },
      acceptHeader,
    };

    fetchStream<T, E>(fetchProps)
      .then(({ error, isFinished }) => {
        setError(error);
        if (isFinished) {
          setIsFinished(isFinished);
          if (!error) {
            finalSyncValueWithCache();
          }
        }
      })
      .catch((err) => {
        console.log("Error fetching", err);
        if (err && err instanceof DOMException && err?.name === "AbortError") {
          return;
        } else if (err instanceof Object && "message" in err) {
          setError(err as E);
        }
        setIsFinished(true);
      });

    return () => {
      try {
        if (
          useAbortController &&
          abortController &&
          !abortController?.signal?.aborted
        )
          abortController?.abort();
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
    body,
    authToken,
    session?.user?.token,
    customHeaders,
    queryParams,
    arrayQueryParam,
    refetchState,
    batchSize,
    cacheKey,
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
  return { messages, error, isFinished, refetch };
}

export default useFetchStream;

// FIXME old
// "use client";
// import { useCallback, useEffect, useState } from "react";
// import { useSession } from "next-auth/react";
// import { BaseError } from "@/types/responses";
// import { fetchStream, FetchStreamProps } from "./fetchStream";
// import { AcceptHeader } from "@/types/fetch-utils";
//
// export interface UseFetchStreamProps {
//   path: string;
//   method?: "GET" | "POST" | "PUT" | "DELETE" | "HEAD" | "PATCH";
//   body?: object | null;
//   authToken?: boolean;
//   customHeaders?: HeadersInit;
//   queryParams?: Record<string, string>;
//   arrayQueryParam?: Record<string, string[]>;
//   cache?: RequestCache;
//   acceptHeader?: AcceptHeader;
//   useAbortController?: boolean;
//   batchSize?: number;
// }
//
// export interface UseFetchStreamReturn<T, E> {
//   messages: T[];
//   error: E | null;
//   isFinished: boolean;
//   refetch: () => void;
// }
//
// export function useFetchStream<T = unknown, E extends BaseError = BaseError>({
//   path,
//   method = "GET",
//   body = null,
//   authToken = false,
//   customHeaders = {},
//   queryParams = {},
//   cache = "no-cache",
//   arrayQueryParam = {},
//   acceptHeader = "application/x-ndjson",
//   useAbortController = true,
//   batchSize = 6,
// }: UseFetchStreamProps): UseFetchStreamReturn<T, E> {
//   const [messages, setMessages] = useState<T[]>([]);
//   const [error, setError] = useState<E | null>(null);
//   const [isFinished, setIsFinished] = useState<boolean>(false);
//   const [refetchState, setRefetchState] = useState(false);
//   const { data: session } = useSession();
//   const refetch = useCallback(() => {
//     setRefetchState((prevIndex) => !prevIndex);
//   }, []);
//   useEffect(() => {
//     setMessages([]);
//     setError(null);
//     setIsFinished(false);
//     if (authToken && !session?.user?.token) {
//       return () => {
//         console.log("No token");
//       };
//     }
//
//     const token = authToken && session?.user?.token ? session.user.token : "";
//     const abortController = new AbortController();
//
//     const fetchProps: FetchStreamProps<T> = {
//       path,
//       method,
//       body,
//       customHeaders,
//       queryParams,
//       arrayQueryParam,
//       token,
//       cache,
//       aboveController: abortController,
//       batchSize,
//       successArrayCallback: (data) => {
//         setMessages((prev) => [...prev, ...data]);
//       },
//       acceptHeader,
//     };
//
//     fetchStream<T, E>(fetchProps)
//       .then(({ error, isFinished }) => {
//         setError(error);
//         setIsFinished(isFinished);
//       })
//       .catch((err) => {
//         console.log("Error fetching", err);
//         if (err && err instanceof DOMException && err?.name === "AbortError") {
//           return;
//         } else if (err instanceof Object && "message" in err) {
//           setError(err as E);
//         }
//         setIsFinished(true);
//       });
//
//     return () => {
//       try {
//         if (
//           useAbortController &&
//           abortController &&
//           !abortController?.signal?.aborted
//         )
//           abortController?.abort();
//       } catch (e) {
//         if (e && e instanceof DOMException && e?.name === "AbortError") {
//           return;
//         } else {
//           console.log(e);
//         }
//       }
//     };
//   }, [
//     path,
//     method,
//     JSON.stringify(body),
//     authToken,
//     JSON.stringify(session?.user?.token),
//     JSON.stringify(customHeaders),
//     JSON.stringify(queryParams),
//     JSON.stringify(arrayQueryParam),
//     refetchState,
//     batchSize,
//   ]);
//
//   return { messages, error, isFinished, refetch };
// }
//
// export default useFetchStream;
