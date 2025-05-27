"use client";

import { UserOrdersAdminPageTexts } from "@/app/[locale]/admin/users/[id]/orders/page";
import { UseListProps } from "@/hoooks/useList";
import useGetUser from "@/hoooks/useGetUser";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import OrdersTable from "@/components/table/orders-table";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

interface Props extends UserOrdersAdminPageTexts, UseListProps {
  id: string;
}

export default function UserOrdersAdminPageContent({
  id,
  title,
  themeSwitchTexts,
  orderTableTexts,
  sortingOrdersSortingOptions,
  header,
  path,
  sortingOptions,
  menuTexts,
  findInSiteTexts,
}: Props) {
  const { authUser } = useAuthUserMinRole();

  const { user, messages, error, isFinished } = useGetUser(id);
  const { navigateToNotFound } = useClientNotFound();
  if (isFinished && error?.status) {
    return navigateToNotFound();
  }
  return (
    <SidebarContentLayout
      navbarProps={{
        title: `${title} ${user?.email || ""}`,
        themeSwitchTexts,
        menuTexts,
        mappingKey: "admin",
        findInSiteTexts,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading
          title={`${title} ${user?.email}`}
          header={`${header} ${user?.email}`}
        />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full">
            <OrdersTable
              path={path}
              forWhom="admin"
              sortingOptions={sortingOptions}
              {...orderTableTexts}
              sizeOptions={[1, 10, 20, 30, 40]}
              mainDashboard={true}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
