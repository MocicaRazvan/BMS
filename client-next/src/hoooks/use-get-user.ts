"use client";

import { useSession } from "next-auth/react";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { useEffect } from "react";
import { Role } from "@/types/fetch-utils";
import { HIERARCHY } from "@/lib/constants";

export default function useGetUser(minRole: Role) {
  const { navigateToNotFound } = useClientNotFound();
  const { data, status } = useSession({
    required: true,
    onUnauthenticated: navigateToNotFound,
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

  return {
    authUser: data?.user,
    status,
  };
}
