import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";
import { v4 as uuidv4 } from "uuid";

export async function GET(req: NextRequest) {
  const state = uuidv4();
  const { searchParams } = new URL(req.url);
  const cookieName = searchParams.get("cookieName");

  if (!cookieName) {
    return new NextResponse("No cookieName provided", { status: 400 });
  }

  cookies().set(cookieName, state);
  const springUrl = process.env.NEXT_PUBLIC_SPRING!;

  const backendResponse = await fetch(
    `${springUrl}/auth/google/login?state=${state}`,
    {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    },
  );
  console.log(backendResponse);
  console.log(await backendResponse.json());
  if (!backendResponse.ok) {
    return new NextResponse("Failed to initiate Google login", {
      status: backendResponse.status,
    });
  }

  const location = backendResponse.headers.get("Location");
  console.log(location);

  if (location) {
    return NextResponse.redirect(location);
  } else {
    return new NextResponse("Authorization URL not found", { status: 500 });
  }
}
