import { NextRequest } from "next/server";
import handleOauthCall from "../handle";
import { cookies } from "next/headers";

export async function GET(req: NextRequest) {
  const springUrl = process.env.NEXT_PUBLIC_SPRING!;

  const { searchParams } = new URL(req.url);
  for (const [key, value] of searchParams.entries()) {
    console.log(`${key}: ${value}`);
  }
  console.log(cookies().getAll());
  console.log(cookies().get("googleState"));

  return await handleOauthCall(
    req,
    `${springUrl}/auth/google/callback`,
    cookies().get("googleState")?.value,
  );
}
