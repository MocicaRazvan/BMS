"use client";

import PlansTable, { PlanTableProps } from "@/components/table/plans-table";
import { useSidebarToggle } from "@/context/sidebar-toggle";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

export default function AdminPlansPageContent(props: PlanTableProps) {
  const { isOpen } = useSidebarToggle();
  const { authUser } = useAuthUserMinRole();

  return <PlansTable {...props} isSidebarOpen={isOpen} />;
}
