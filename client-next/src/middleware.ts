import type { NextRequest } from "next/server";
import { apiMiddleware } from "@/middleware/csrf-validator";
import { intlPageMiddleware } from "@/middleware/intl";
import { authMiddleware } from "@/middleware/auth-middleware";

const redirectedLocales = ["auth", "success", "orders"];
const exemptedApiPaths = ["/api/auth"];

export async function middleware(request: NextRequest) {
  if (request.nextUrl.pathname.toLowerCase().startsWith("/api")) {
    return await apiMiddleware(request, exemptedApiPaths);
  }
  return authMiddleware(request, (req) =>
    intlPageMiddleware(req, redirectedLocales),
  );
}

export const config = {
  matcher: [
    // "/((?!_next/static|_next/image.*\\.png$|favicon\\.ico$|public/.*).*)",
    "/((?!_next/static|_next/image.*\\.png$|favicon\\.ico$|public/.*|api/auth|cal$|images/).*)",
  ],
};
