"use client";

import PlansTable, { PlanTableProps } from "@/components/table/plans-table";
import { useSidebarToggle } from "@/context/sidebar-toggle";

export default function UsersPlansPageContent(props: PlanTableProps) {
  const { isOpen } = useSidebarToggle();

  return <PlansTable {...props} isSidebarOpen={isOpen} />;
}
