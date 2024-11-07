import ndjsonStream from "can-ndjson-stream";
import { BaseError } from "@/types/responses";
import { AcceptHeader } from "@/types/fetch-utils";

export interface FetchStreamProps<T> {
  path: string;
  method?: "GET" | "POST" | "PUT" | "DELETE" | "HEAD" | "PATCH";
  body?: object | null;
  customHeaders?: HeadersInit;
  queryParams?: Record<string, string>;
  arrayQueryParam?: Record<string, string[]>;
  token?: string;
  cache?: RequestCache;
  aboveController?: AbortController;
  successCallback?: (data: T) => void;
  errorCallback?: (error: BaseError) => void;
  acceptHeader?: AcceptHeader;
}

export async function fetchStream<T = any, E extends BaseError = BaseError>({
  path,
  method = "GET",
  body = null,
  customHeaders = {},
  queryParams = {},
  token = "",
  arrayQueryParam = {},
  cache = "default",
  aboveController,
  successCallback,
  errorCallback,
  acceptHeader = "application/x-ndjson",
}: FetchStreamProps<T>) {
  let messages: T[] = [];
  let error: E | null = null;
  let isFinished = false;

  const abortController = aboveController || new AbortController();

  const headers = new Headers(customHeaders);
  headers.set("Accept", acceptHeader);

  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  if (body !== null && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const querySearch = new URLSearchParams(queryParams).toString();

  const arrayQueryStrings = Object.entries(arrayQueryParam)
    .map(([key, values]) => {
      return values
        .map(
          (value) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`,
        )
        .join("&");
    })
    .join("&");

  const combinedQuery = [querySearch, arrayQueryStrings]
    .filter((part) => part)
    .join("&");

  const fetchOptions: RequestInit = {
    method,
    headers,
    signal: abortController.signal,
    cache,
  };

  if (body !== null && method !== "GET" && method !== "HEAD") {
    fetchOptions.body = JSON.stringify(body);
  }

  const url = combinedQuery ? `${path}?${combinedQuery}` : path;

  try {
    const res = await fetch(
      `${process.env.NEXT_PUBLIC_SPRING_CLIENT}${url}`,
      fetchOptions,
    );
    const stream = ndjsonStream<T, E>(res.body);
    const reader = stream.getReader();

    const read = async (): Promise<void> => {
      const { done, value } = await reader.read();
      if (done) {
        isFinished = true;
        return;
      }
      if (!res.ok) {
        error = value as E;
        errorCallback?.(value as E);
      } else {
        messages = [...messages, value as T];
        successCallback?.(value as T);
      }
      await read();
    };

    await read();
  } catch (err) {
    // check if error is AbortError
    if (err && err instanceof DOMException && err?.name === "AbortError") {
      isFinished = false;
    } else {
      error = err as E;
      isFinished = true;
    }
  }

  const cleanUp = () => {
    if (!abortController.signal.aborted) {
      abortController.abort();
    }
  };

  return { messages, error, isFinished, cleanUp };
}
