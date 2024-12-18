import { NextRequest } from "next/server";
import handleOauthCall from "../handle";
import { cookies } from "next/headers";
import { emitInfo } from "@/logger";

export async function GET(req: NextRequest) {
  emitInfo("GET /api/auth/callback/google");
  const springUrl = process.env.NEXT_PUBLIC_SPRING!;

  return await handleOauthCall(
    req,
    `${springUrl}/auth/google/callback`,
    cookies().get("googleState")?.value,
  );
}
