"use server";

import { cookies } from "next/headers";
import {
  NEXT_CSRF_COOKIES,
  NEXT_CSRF_HEADER,
  NEXT_CSRF_HEADER_TOKEN,
} from "@/types/constants";

export async function getCsrfNextAuth() {
  const allCookies = await cookies();
  const matchingCookie = NEXT_CSRF_COOKIES.find((cookieName) =>
    allCookies.has(cookieName),
  );

  if (!matchingCookie) {
    return "";
  }
  const value = allCookies.get(matchingCookie)?.value;

  if (!value) {
    return "";
  }

  // console.log("CSRF token", value);

  return value;
}

export async function getCsrfNextAuthHeader() {
  const token = await getCsrfNextAuth();
  const tokenHashDelimiter = token.indexOf("|") !== -1 ? "|" : "%7C";

  const rawToken = token.split(tokenHashDelimiter)[0];
  return {
    [NEXT_CSRF_HEADER_TOKEN]: token,
    [NEXT_CSRF_HEADER]: rawToken,
  };
}
