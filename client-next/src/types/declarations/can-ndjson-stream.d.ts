declare module "can-ndjson-stream" {
  export default function ndjsonStream<T, E>(
    data: unknown,
  ): {
    getReader: () => {
      read: () => Promise<{ done: boolean; value: T | E }>;
    };
    cancel: () => void;
  };
}
