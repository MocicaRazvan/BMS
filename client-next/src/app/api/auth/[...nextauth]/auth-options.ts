import { NextAuthOptions } from "next-auth";
import CredentialsProvider from "next-auth/providers/credentials";
import GitHubProvider from "next-auth/providers/github";
import { CustomGoogleProvider } from "@/app/api/auth/[...nextauth]/custom-google-provider";
import { getCsrfNextAuthHeader } from "@/actions/get-csr-next-auth";
import fetchFactory from "@/lib/fetchers/fetchWithRetry";

export const authOptions: NextAuthOptions = {
  providers: [
    CredentialsProvider({
      id: "credentials",
      name: "Credentials",
      credentials: {
        email: {
          label: "Email",
          type: "email",
          placeholder: "johndoe@gmail.com",
        },
        password: { label: "Password", type: "password" },
      },
      async authorize(credentials, req) {
        try {
          if (!credentials?.email || !credentials?.password) {
            return { error: "Missing credentials" };
          }
          const csrfHeader = await getCsrfNextAuthHeader();

          const resp = await fetchFactory(fetch)(
            `${process.env.NEXT_PUBLIC_SPRING}/auth/login`,
            {
              method: "POST",
              body: JSON.stringify(credentials),
              headers: {
                "Content-Type": "application/json",
                ...csrfHeader,
              },
            },
          );

          if (!resp.ok) {
            return { error: "CredentailsSingin" };
          }

          const user = await resp.json();

          if (!user) {
            return { error: "Invalid credentials" };
          }

          return user;
        } catch (error) {
          return { error: "An error occurred" };
        }
      },
    }),
    GitHubProvider({
      name: "GitHub",
      clientSecret: process.env.GITHUB_CLIENT_SECRET!,
      clientId: process.env.GITHUB_CLIENT_ID!,
    }),
    CustomGoogleProvider,
  ],
  callbacks: {
    async jwt({ token, user, trigger, session }) {
      if (trigger === "update") {
        token.user = session.data.user;
        return token;
      }
      if (user) {
        token.user = {
          ...user,
          id: `${user.id}`,
          emailVerified: !!user.emailVerified,
        };
      }

      return token;
    },
    async session({ session, token }) {
      if (token.user) {
        session.user = token.user;
      }
      return session;
    },
    async signIn({ user }) {
      if (user?.error) {
        throw new Error(user.error);
      }
      return true;
    },
  },
  pages: {
    signIn: "/auth/signin",
    signOut: "/auth/signout",
  },
  session: {
    strategy: "jwt",
    maxAge: 30 * 24 * 60 * 60,
  },
  jwt: {
    maxAge: 30 * 24 * 60 * 60,
  },
};
