import { NextRequest } from "next/server";
import handleOauthCall from "../handle";

export async function GET(req: NextRequest) {
  const springUrl = process.env.NEXT_PUBLIC_SPRING!;

  return await handleOauthCall(req, `${springUrl}/auth/github/callback`);
}
