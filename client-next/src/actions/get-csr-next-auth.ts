"use server";

import { cookies } from "next/headers";

const NEXT_CSRF_COOKIES = [
  "__Host-next-auth.csrf-token",
  "next-auth.csrf-token",
] as const;

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

  console.log("CSRF token", value);

  return value;
}

export async function getCsrfNextAuthHeader() {
  return {
    "x-csrf-token": await getCsrfNextAuth(),
  };
}
