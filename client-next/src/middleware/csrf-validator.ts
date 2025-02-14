import { NextRequest } from "next/server";

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
      return logErrorAndReturn(req);
    }
    // delimiter could be either a '|' or a '%7C'
    const tokenHashDelimiter =
      parsedCsrfTokenAndHash.indexOf("|") !== -1 ? "|" : "%7C";

    const [requestToken, requestHash] =
      parsedCsrfTokenAndHash.split(tokenHashDelimiter);

    const secret = process.env.NEXTAUTH_SECRET;
    console.error("verifyCsrfTokenSecret", secret);

    // compute the valid hash
    const validHash = await computeSha256(`${requestToken}${secret}`);

    // console.log("validHash", validHash);
    // console.log("requestHash", requestHash);
    if (requestHash !== validHash) {
      return logErrorAndReturn(req);
    }
  } catch (err) {
    console.error("Error verifying CSRF token", err);
    return false;
  }
  return true;
};

const logErrorAndReturn = (req: NextRequest): boolean => {
  console.error("Invalid CSRF token", req.nextUrl.pathname);
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
