import { NextRequest, NextResponse } from "next/server";
import { encode, getToken } from "next-auth/jwt";

export default async function handleOauthCall(
  req: NextRequest,
  url: string,
  state?: string,
) {
  const { searchParams } = new URL(req.url);
  const code = searchParams.get("code");

  console.log("OAuth Code:", code);
  console.log("State:", state);
  console.log("OAuth URL:", url);

  if (!code) {
    return new Response(JSON.stringify({ error: "No code provided" }), {
      status: 400,
    });
  }

  try {
    const response = await fetch(url, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code, state: state ? state : null }),
    });

    const data = await response.json();
    console.log("OAuth Response Error Status:", response.status);

    console.log(data);
    if (response.ok) {
      const token = await getToken({
        req,
        secret: process.env.NEXTAUTH_SECRET!,
      });

      const updatedToken = {
        ...token,
        user: data,
      };

      const newToken = await encode({
        token: updatedToken,
        secret: process.env.NEXTAUTH_SECRET!,
      });

      const nextAuthUrl = process.env.NEXTAUTH_URL;
      const url = nextAuthUrl ? new URL(nextAuthUrl) : new URL("/");
      const secure =
        process.env.NODE_ENV === "production" && url.protocol === "https:";

      console.log("Redirect Oauth URL:", url);

      console.log("Secure:", secure);

      const nextAuthCookieName = secure
        ? "__Secure-next-auth.session-token"
        : "next-auth.session-token";

      const res = NextResponse.redirect(url, { status: 302 });
      res.cookies.set(nextAuthCookieName, newToken, {
        httpOnly: true,
        // secure: process.env.NODE_ENV === "production",
        secure,
        path: "/",
        sameSite: "lax",
      });

      return res;
    } else {
      return new Response(JSON.stringify(data), { status: response.status });
    }
  } catch (error) {
    console.log(error);
    return new Response(JSON.stringify({ error: "Internal server error" }), {
      status: 500,
    });
  }
}
