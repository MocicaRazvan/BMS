import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";
import createMiddleware from "next-intl/middleware";
import { locales } from "@/navigation";
import { verifyCsrfToken } from "@/middleware/csrf-validator";

const intlMiddleware = createMiddleware({
  // A list of all locales that are supported
  locales,
  // Used when no locale matches
  defaultLocale: "en",
});
const redirectedLocales = ["auth", "success", "orders"];
const exemptedApiPaths = ["/api/auth"];

function pageMiddleware(request: NextRequest) {
  // Extract the locale from the request URL
  const { pathname, search, origin } = request.nextUrl;
  const locale = pathname.split("/")[1];
  const localeCookie = request.cookies.get("NEXT_LOCALE");

  if (redirectedLocales.includes(locale.toLowerCase()) && localeCookie) {
    const newPathname = `/${localeCookie.value}${pathname}`;
    const url = new URL(newPathname, origin);
    url.search = search;
    console.log("search", search);
    console.log("newPathname", newPathname);
    console.log("redirecting to", url.toString());
    return NextResponse.redirect(url);
  }

  // If the locale is not supported, redirect to /en
  if (!locales.includes(locale as never)) {
    return NextResponse.redirect(new URL("/en", request.url));
  }

  // Continue with the default middleware logic
  return intlMiddleware(request);
}

async function apiMiddleware(request: NextRequest) {
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

export async function middleware(request: NextRequest) {
  if (request.nextUrl.pathname.toLowerCase().startsWith("/api")) {
    return await apiMiddleware(request);
  }
  return pageMiddleware(request);
}

export const config = {
  matcher: [
    // "/((?!_next/static|_next/image.*\\.png$|favicon\\.ico$|public/.*).*)",
    "/((?!_next/static|_next/image.*\\.png$|favicon\\.ico$|public/.*|api/auth).*)",
  ],
};
