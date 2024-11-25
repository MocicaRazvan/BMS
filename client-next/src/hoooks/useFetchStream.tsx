import { useCallback, useEffect, useState } from "react";
import { useSession } from "next-auth/react";
import { BaseError } from "@/types/responses";
import { fetchStream, FetchStreamProps } from "./fetchStream";
import { AcceptHeader } from "@/types/fetch-utils";

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
}

interface UseFetchStreamReturn<T, E> {
  messages: T[];
  error: E | null;
  isFinished: boolean;
  refetch: () => void;
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
}: UseFetchStreamProps): UseFetchStreamReturn<T, E> {
  const [messages, setMessages] = useState<T[]>([]);
  const [error, setError] = useState<E | null>(null);
  const [isFinished, setIsFinished] = useState<boolean>(false);
  const [refetchState, setRefetchState] = useState(false);
  const { data: session } = useSession();
  const refetch = useCallback(() => {
    setRefetchState((prevIndex) => !prevIndex);
  }, []);
  useEffect(() => {
    setMessages([]);
    setError(null);
    setIsFinished(false);
    if (authToken && !session?.user?.token) {
      return () => {
        console.log("No token");
      };
    }

    const token = authToken && session?.user?.token ? session.user.token : "";
    const abortController = new AbortController();

    console.log("QP", queryParams);

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
      successArrayCallback: (data) => {
        setMessages((prev) => [...prev, ...data]);
      },
      // successCallback: (data) => {
      //   setMessages((prev) => [...prev, data]);
      // },
      acceptHeader,
    };

    fetchStream<T, E>(fetchProps)
      .then(({ error, isFinished }) => {
        setError(error);
        setIsFinished(isFinished);
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
    JSON.stringify(body),
    authToken,
    JSON.stringify(session?.user?.token),
    JSON.stringify(customHeaders),
    JSON.stringify(queryParams),
    JSON.stringify(arrayQueryParam),
    refetchState,
    batchSize,
  ]);

  // console.log(messages.length);
  return { messages, error, isFinished, refetch };
}

export default useFetchStream;
