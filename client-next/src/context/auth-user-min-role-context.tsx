"use client";

import { Session } from "next-auth";
import { createContext, ReactNode, useContext, useEffect } from "react";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { useSession } from "next-auth/react";
import { HIERARCHY } from "@/types/constants";
import { Skeleton } from "@/components/ui/skeleton";
import { motion } from "framer-motion";
import { Role } from "@/types/fetch-utils";
import { useRouter } from "@/navigation";

export interface AuthUserMinRoleContextType {
  authUser: NonNullable<Session["user"]>;
}

const AuthUserMinRoleContext = createContext<AuthUserMinRoleContextType | null>(
  null,
);

export const AuthUserMinRoleProvider = ({
  children,
  minRole,
}: {
  children: ReactNode;
  minRole: Role;
}) => {
  const router = useRouter();
  const { navigateToNotFound } = useClientNotFound();
  const { data, status } = useSession({
    required: true,
    onUnauthenticated: () => {
      router.replace("/not-found");
    },
  });
  useEffect(() => {
    if (status === "loading") {
      return;
    }

    const userRole = data?.user?.role;
    if (userRole) {
      if (HIERARCHY[userRole] < HIERARCHY[minRole]) {
        navigateToNotFound();
      }
    }
  }, [data?.user?.role, minRole, navigateToNotFound, status]);

  if (status !== "authenticated" || !data?.user) {
    return (
      <motion.div
        className="size-full min-h-screen "
        initial={{ opacity: 0.5 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.5, delay: 1 }}
      >
        <Skeleton className="size-full" />
      </motion.div>
    );
  }

  const authUser = data.user;

  return (
    <AuthUserMinRoleContext.Provider value={{ authUser }}>
      {children}
    </AuthUserMinRoleContext.Provider>
  );
};

export const useAuthUserMinRole = () => {
  const context = useContext(AuthUserMinRoleContext);
  if (!context) {
    throw new Error(
      "useAuthUserMinRole must be used within an AuthUserMinRoleProvider",
    );
  }
  return context;
};
