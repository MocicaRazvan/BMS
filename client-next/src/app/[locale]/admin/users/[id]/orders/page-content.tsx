"use client";

import { UserOrdersAdminPageTexts } from "@/app/[locale]/admin/users/[id]/orders/page";
import { WithUser } from "@/lib/user";
import { UseListProps } from "@/hoooks/useList";
import useGetUser from "@/hoooks/useGetUser";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import OrdersTable from "@/components/table/orders-table";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { MetadataValue } from "@/components/nav/find-in-site";

interface Props extends UserOrdersAdminPageTexts, WithUser, UseListProps {
  id: string;
  metadataValues: MetadataValue[];
}

export default function UserOrdersAdminPageContent({
  id,
  title,
  themeSwitchTexts,
  orderTableTexts,
  sortingOrdersSortingOptions,
  header,
  authUser,
  path,
  sortingOptions,
  menuTexts,
  findInSiteTexts,
  metadataValues,
}: Props) {
  const { user, messages, error, isFinished } = useGetUser(id);
  const { navigateToNotFound } = useClientNotFound();
  if (isFinished && error?.status) {
    return navigateToNotFound();
  }
  return (
    <SidebarContentLayout
      navbarProps={{
        title: `${title} ${user?.email || ""}`,
        authUser,
        themeSwitchTexts,
        menuTexts,
        mappingKey: "admin",
        findInSiteTexts,
        metadataValues,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading
          title={`${title} ${user?.email}`}
          header={`${header} ${user?.email}`}
        />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full">
            {" "}
            <OrdersTable
              path={path}
              forWhom="admin"
              sortingOptions={sortingOptions}
              {...orderTableTexts}
              authUser={authUser}
              sizeOptions={[1, 10, 20, 30, 40]}
              mainDashboard={true}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
