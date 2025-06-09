import { AuthProvider, Role } from "@/types/fetch-utils";
import { Session } from "next-auth";

declare module "next-auth" {
  interface User {
    firstName: string;
    lastName: string;
    email: string;
    token: string;
    role: Role;
    image: string;
    error?: string;
    provider: AuthProvider;
    emailVerified: boolean;
  }

  interface Session {
    user?: User & {
      firstName: string;
      lastName: string;
      email: string;
      token: string;
      role: Role;
      image: string;
      provider: AuthProvider;
      emailVerified: boolean;
    };
    jwt?: string;
  }
}

declare module "next-auth/jwt" {
  interface JWT {
    firstName?: string;
    lastName?: string;
    email?: string;
    token?: string;
    role?: Role;
    user?: Session["user"];
    image?: string;
    error?: string;
  }
}
