import {NextRequest, NextResponse} from "next/server";
import {encode, getToken} from "next-auth/jwt";

export default async function handleOauthCall(
    req: NextRequest,
    url: string,
    state?: string,
) {
    const {searchParams} = new URL(req.url);
    const code = searchParams.get("code");

    console.log("OAuth Code:", code);
    console.log("State:", state);
    console.log("OAuth URL:", url);

    if (!code) {
        return new Response(JSON.stringify({error: "No code provided"}), {
            status: 400,
        });
    }

    try {
        const response = await fetch(url, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify({code, state: state ? state : null}),
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

            const res = NextResponse.redirect(url, {status: 302});
            res.cookies.set("next-auth.session-token", newToken, {
                httpOnly: true,
                secure: process.env.NODE_ENV === "production",
                path: "/",
            });

            return res;
        } else {
            return new Response(JSON.stringify(data), {status: response.status});
        }
    } catch (error) {
        console.log(error);
        return new Response(JSON.stringify({error: "Internal server error"}), {
            status: 500,
        });
    }
}
