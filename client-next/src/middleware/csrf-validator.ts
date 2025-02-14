import { NextRequest } from "next/server";
import { emitError } from "@/logger";

const NEXT_CSRF_COOKIES = [
  "__Host-next-auth.csrf-token",
  "next-auth.csrf-token",
] as const;

const NEXT_CSRF_HEADER = "x-csrf-token" as const;

const enableCsrfProtection = process.env.NEXT_CSRF
  ? process.env.NEXT_CSRF.toLowerCase() === "true"
  : true;

export const verifyCsrfToken = async (req: NextRequest): Promise<boolean> => {
  if (!enableCsrfProtection) {
    return true;
  }
  // console.log("req", req);
  try {
    let parsedCsrfTokenAndHash = getCookie(req);

    // console.log("parsedCsrfTokenAndHash", parsedCsrfTokenAndHash);
    if (!parsedCsrfTokenAndHash) {
      parsedCsrfTokenAndHash = req.headers.get(NEXT_CSRF_HEADER) ?? undefined;
    }

    if (!parsedCsrfTokenAndHash) {
      return emitFalseCsrfTokenErrorAndReturn(req);
    }
    // delimiter could be either a '|' or a '%7C'
    const tokenHashDelimiter =
      parsedCsrfTokenAndHash.indexOf("|") !== -1 ? "|" : "%7C";

    const [requestToken, requestHash] =
      parsedCsrfTokenAndHash.split(tokenHashDelimiter);

    const secret = process.env.NEXTAUTH_SECRET;

    // compute the valid hash
    const validHash = await computeSha256(`${requestToken}${secret}`);

    // console.log("validHash", validHash);
    // console.log("requestHash", requestHash);
    if (requestHash !== validHash) {
      return emitFalseCsrfTokenErrorAndReturn(req);
    }
  } catch (err) {
    emitError({
      message: "Error verifying CSRF token",
      error: err instanceof Object ? JSON.stringify(err) : "Error",
    });
    console.error("Error verifying CSRF token", err);
    return false;
  }
  return true;
};

const emitFalseCsrfTokenErrorAndReturn = (req: NextRequest): boolean => {
  emitError({
    message: "Invalid CSRF token",
    error: `Invalid CSRF token for request ${req.url}`,
  });

  return false;
};

const getCookie = (req: NextRequest): string | undefined => {
  for (const cookieName of NEXT_CSRF_COOKIES) {
    // console.log("cookieName", cookieName);
    const cookie = req.cookies.get(cookieName);
    // console.log("cookie", cookie);
    if (cookie) {
      return cookie.value;
    }
  }

  return undefined;
};

const computeSha256 = async (data: string): Promise<string> => {
  const dataBuffer = new TextEncoder().encode(data);
  const hashBuffer = await crypto.subtle.digest("SHA-256", dataBuffer);
  return bufferToHex(hashBuffer);
};

const bufferToHex = (buffer: ArrayBuffer): string => {
  return [...new Uint8Array(buffer)]
    .map((byte) => byte.toString(16).padStart(2, "0"))
    .join("");
};
