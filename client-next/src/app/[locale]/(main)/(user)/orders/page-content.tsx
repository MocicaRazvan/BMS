"use client";

import OrdersTable, { OrdersTableProps } from "@/components/table/orders-table";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

export default function UserOrdersPageContent(
  props: Omit<OrdersTableProps, "path">,
) {
  const { authUser } = useAuthUserMinRole();

  return <OrdersTable {...props} path={`/orders/filtered/${authUser.id}`} />;
}
