"use client";

import { signOut, useSession } from "next-auth/react";
import { ReactNode, useEffect } from "react";
import { useRouter } from "@/navigation";
import { fetchStream } from "@/lib/fetchers/fetchStream";

export default function ValidUserSessionContext({
  children,
}: {
  children: ReactNode;
}) {
  const session = useSession();
  const router = useRouter();

  useEffect(() => {
    if (
      session.status === "authenticated" &&
      session.data?.user?.token &&
      session.data?.user?.role &&
      process.env.NEXT_PUBLIC_SPRING_CLIENT
    ) {
      const handleSignOut = async () => {
        await signOut({ redirect: false });
        router.replace("/");
      };
      fetchStream<{
        valid: boolean;
      }>({
        path: "/auth/validateToken",
        method: "POST",
        acceptHeader: "application/json",
        body: {
          token: session.data.user.token,
          minRoleRequired: session.data.user.role,
        },
      })
        .then(async ({ messages, error }) => {
          if (error) {
            console.error("ValidUserSessionContext " + error);
            await handleSignOut();
          } else if (!messages[0].valid) {
            console.error("ValidUserSessionContext Token is not valid");
            await handleSignOut();
          } else {
            console.log("ValidUserSessionContext Token is valid");
          }
        })
        .catch(async (e) => {
          console.error("ValidUserSessionContext " + e);
          await handleSignOut();
        });
    }
  }, [session.data?.user?.role, session.data?.user?.token, session.status]);

  return children;
}
