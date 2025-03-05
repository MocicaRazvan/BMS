"use client";

import { UserAdminDailySalesPageTexts } from "@/app/[locale]/admin/users/[id]/dailySales/page";
import { WithUser } from "@/lib/user";
import useGetUser from "@/hoooks/useGetUser";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import LoadingSpinner from "@/components/common/loading-spinner";
import Heading from "@/components/common/heading";
import DailySales from "@/components/charts/daily-sales";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { MetadataValue } from "@/components/nav/find-in-site";

interface Props extends UserAdminDailySalesPageTexts, WithUser {
  id: string;
  metadataValues: MetadataValue[];
}

export default function UserAdminDailySalesPageContent({
  id,
  dailySalesTexts,
  menuTexts,
  themeSwitchTexts,
  authUser,
  header,
  title,
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
        {!isFinished ? (
          <LoadingSpinner />
        ) : (
          <>
            <Heading
              title={`${title} ${user?.email}`}
              header={`${header} ${user?.email}`}
            />
            <div className="mt-10 h-full">
              <DailySales
                path={`/orders/trainer/countAndAmount/daily/${id}`}
                {...dailySalesTexts}
                authUser={authUser}
              />
            </div>
          </>
        )}
      </div>
    </SidebarContentLayout>
  );
}
