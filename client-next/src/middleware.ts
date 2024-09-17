import type { NextRequest } from "next/server";
import { NextResponse } from "next/server";
import createMiddleware from "next-intl/middleware";
import { locales } from "@/navigation";

const intlMiddleware = createMiddleware({
  // A list of all locales that are supported
  locales,
  // Used when no locale matches
  defaultLocale: "en",
});
const redirectedLocales = ["auth", "success", "orders"];
export function middleware(request: NextRequest) {
  // Extract the locale from the request URL
  const { pathname, search, origin } = request.nextUrl;
  const locale = pathname.split("/")[1];
  const localeCookie = request.cookies.get("NEXT_LOCALE");
  console.log("locale", locale);
  console.log("localeCookie", localeCookie);
  console.log("search", search);
  if (redirectedLocales.includes(locale.toLowerCase()) && localeCookie) {
    const newPathname = `/${localeCookie.value}${pathname}`;
    const url = new URL(newPathname, origin);
    url.search = search; // Append the search parameters
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

export const config = {
  // Match only internationalized pathnames
  matcher: [
    "/((?!api|_next/static|_next/image.*\\.png$|favicon\\.ico$|public/.*).*)",
  ],
};
