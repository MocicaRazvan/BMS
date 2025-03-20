import fetchRetry, { FetchLibrary } from "fetch-retry";
import { emitError, emitInfo } from "@/logger";
const UNRETRIEABLE_STATUS_CODES = [400, 401, 403, 404];

export default function fetchFactory<F extends FetchLibrary>(baseFetch: F) {
  return fetchRetry(baseFetch, {
    retryDelay: (attempt) => Math.pow(2, attempt) * 500,
    retryOn: (attempt, error, response) => {
      if (attempt >= 3) return false;
      if (
        error !== null ||
        (response?.status &&
          response?.status >= 400 &&
          !UNRETRIEABLE_STATUS_CODES.includes(response?.status))
      ) {
        emitError(
          `fetchStream failed with status ${response?.status} and error ${error}`,
        );
        emitInfo(`fetchStream retrying attempt ${attempt}`);
        console.log("fetchStream retrying attempt", attempt);
        return true;
      }
      return false;
    },
  });
}

export const fetchFactoryString = `
  async function fetchFactory(baseFetch, UNRETRIEABLE_STATUS_CODES) {
    const fetchRetry = (await import('fetch-retry')).default;

    return fetchRetry(baseFetch, {
      retryDelay: (attempt) => Math.pow(2, attempt) * 500,
      retryOn: (attempt, error, response) => {
        if (attempt >= 3) return false;
        if (
          error !== null ||
          (response?.status &&
            response?.status >= 400 &&
            !UNRETRIEABLE_STATUS_CODES.includes(response?.status))
        ) {
          console.error(\`fetchStream failed with status \${response?.status} and error \${error}\`);
          console.info(\`fetchStream retrying attempt \${attempt}\`);
          console.log("fetchStream retrying attempt", attempt);
          return true;
        }
        return false;
      },
    });
  }
`;
