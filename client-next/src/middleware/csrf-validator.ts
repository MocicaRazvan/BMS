import { NextRequest, NextResponse } from "next/server";

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
  try {
    let parsedCsrfTokenAndHash = getCookie(req);

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

    // compute the valid hash
    const validHash = await computeSha256(`${requestToken}${secret}`);

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
    const cookie = req.cookies.get(cookieName);
    if (cookie) {
      return cookie.value;
    }
  }

  return undefined;
};

export async function apiMiddleware(
  request: NextRequest,
  exemptedApiPaths: string[],
) {
  // console.log(`Runtime: ${process.env.NEXT_RUNTIME || "nodejs"}`);

  const lowerCasePath = request.nextUrl.pathname.toLowerCase();
  console.log("apiMiddleware" + lowerCasePath);
  if (exemptedApiPaths.some((path) => lowerCasePath.startsWith(path))) {
    return NextResponse.next();
  }

  const method = request.method;
  if (method === "OPTIONS" || method === "GET" || method === "HEAD") {
    return NextResponse.next();
  }

  const validCsrf = await verifyCsrfToken(request);
  if (!validCsrf) {
    return NextResponse.json(
      {
        error: "Invalid CSRF token",
      },
      {
        status: 403,
      },
    );
  }

  return NextResponse.next();
}

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
