import { NextRequest } from "next/server";
import handleOauthCall from "../handle";
import { emitInfo } from "@/logger";

export async function GET(req: NextRequest) {
  emitInfo("GET /api/auth/callback/github");
  const springUrl = process.env.NEXT_PUBLIC_SPRING!;

  return await handleOauthCall(req, `${springUrl}/auth/github/callback`);
}
