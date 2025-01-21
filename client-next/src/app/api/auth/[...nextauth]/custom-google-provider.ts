import { Issuer } from "openid-client";
import { Provider } from "next-auth/providers/index";

const springUrl = process.env.NEXT_PUBLIC_SPRING!;
const nextUrl = process.env.NEXTAUTH_URL!;

export const GOOGLE_STATE_COOKIE_NAME = "googleState";
const googleIssuer = new Issuer({
  issuer: springUrl,
  authorization_endpoint: `${nextUrl}/api/auth/callback/cookies?cookieName=${GOOGLE_STATE_COOKIE_NAME}`,
  token_endpoint: `${springUrl}/auth/google/callback`,
  userinfo_endpoint: "https://www.googleapis.com/oauth2/v3/userinfo",
});
const googleIssuerMetadata = (
  gi: Issuer,
): gi is Issuer & {
  metadata: {
    issuer: string;
    authorization_endpoint: string;
    token_endpoint: string;
    userinfo_endpoint: string;
  };
} => {
  const { metadata } = gi;
  return (
    typeof metadata.authorization_endpoint === "string" &&
    typeof metadata.token_endpoint === "string" &&
    typeof metadata.userinfo_endpoint === "string"
  );
};

if (!googleIssuerMetadata(googleIssuer)) {
  throw new Error("Invalid Google issuer metadata");
}

export const CustomGoogleProvider: Provider = {
  id: "custom-google-provider",
  name: "CustomGoogle",
  type: "oauth",
  version: "2.0",
  wellKnown: undefined,
  issuer: googleIssuer.metadata.issuer,
  authorization: {
    url: googleIssuer.metadata.authorization_endpoint,
    params: {
      scope: "openid email profile",
    },
  },
  token: {
    url: googleIssuer.metadata.token_endpoint,
    params: {
      grant_type: "authorization_code",
    },
  },
  userinfo: {
    url: googleIssuer.metadata.userinfo_endpoint,
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
