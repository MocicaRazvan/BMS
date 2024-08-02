"use client";
import { SessionProvider } from "next-auth/react";
import * as React from "react";

export function NextAuthSessionProvider({
  children,
}: {
  children: React.ReactNode;
}) {
  return <SessionProvider>{children}</SessionProvider>;
}
