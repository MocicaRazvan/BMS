import { NextRequest, NextResponse } from "next/server";
import { locales } from "@/navigation";
import createMiddleware from "next-intl/middleware";
const intlMiddleware = createMiddleware({
  // A list of all locales that are supported
  locales,
  // Used when no locale matches
  defaultLocale: "en",
});
export function pageMiddleware(
  request: NextRequest,
  redirectedLocales: string[],
) {
  // Extract the locale from the request URL
  // console.log(`Runtime: ${process.env.NEXT_RUNTIME || "nodejs"}`);

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
