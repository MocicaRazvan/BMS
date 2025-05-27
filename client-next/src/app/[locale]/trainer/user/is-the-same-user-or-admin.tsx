"use client";
import { ReactNode, useEffect } from "react";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import { useRouter } from "@/navigation";
import { wrapItemToString } from "@/lib/utils";

export default function IsTheSameUserOrAdmin({
  children,
  id,
}: {
  children: ReactNode;
  id: string;
}) {
  const router = useRouter();
  const { authUser } = useAuthUserMinRole();
  useEffect(() => {
    if (
      authUser.role !== "ROLE_ADMIN" &&
      wrapItemToString(id) !== wrapItemToString(authUser.id)
    ) {
      router.replace("/not-found");
    }
  }, [authUser.id, authUser.role, id, router]);

  return children;
}
