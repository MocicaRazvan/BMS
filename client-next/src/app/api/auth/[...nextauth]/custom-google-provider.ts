import { Issuer } from "openid-client";
import { Provider } from "next-auth/providers/index";

const springUrl = process.env.NEXT_PUBLIC_SPRING!;
const nextUrl = process.env.NEXTAUTH_URL!;

const googleIssuer = new Issuer({
  issuer: springUrl,
  authorization_endpoint: `${nextUrl}/api/auth/callback/cookies?cookieName=googleState`,
  token_endpoint: `${springUrl}/auth/google/callback`,
  userinfo_endpoint: "https://www.googleapis.com/oauth2/v3/userinfo",
});

export const CustomGoogleProvider: Provider = {
  id: "custom-google-provider",
  name: "CustomGoogle",
  type: "oauth",
  version: "2.0",
  wellKnown: undefined,
  issuer: googleIssuer.issuer as string,
  authorization: {
    url: googleIssuer.authorization_endpoint as string,
    params: {
      scope: "openid email profile",
    },
  },
  token: {
    url: googleIssuer.token_endpoint as string,
    params: {
      grant_type: "authorization_code",
    },
  },
  userinfo: {
    url: googleIssuer.userinfo_endpoint as string,
  },
  profile(profile, tokens) {
    return {
      id: profile.sub,
      name: profile.name,
      email: profile.email,
      image: profile.picture,
      firstName: "",
      lastName: "",
      token: tokens.access_token || "",
      role: "ROLE_USER",
      provider: "GOOGLE",
      emailVerified: true,
    };
  },
  clientId: process.env.GOOGLE_CLIENT_ID!,
  clientSecret: process.env.GOOGLE_CLIENT_SECRET!,
};
