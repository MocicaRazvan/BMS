import { NextRequest } from "next/server";
import handleOauthCall from "../handle";
import { cookies } from "next/headers";
import { emitInfo } from "@/logger";
import { GOOGLE_STATE_COOKIE_NAME } from "@/types/constants";

export async function GET(req: NextRequest) {
  emitInfo("GET /api/auth/callback/google");
  const springUrl = process.env.NEXT_PUBLIC_SPRING!;

  return await handleOauthCall(
    req,
    `${springUrl}/auth/google/callback`,
    cookies().get(GOOGLE_STATE_COOKIE_NAME)?.value,
    async () => {
      cookies().delete(GOOGLE_STATE_COOKIE_NAME);
    },
  );
}
