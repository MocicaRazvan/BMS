"use client";

import { UserAdminDailySalesPageTexts } from "@/app/[locale]/admin/users/[id]/dailySales/page";
import useGetUser from "@/hoooks/useGetUser";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import LoadingSpinner from "@/components/common/loading-spinner";
import Heading from "@/components/common/heading";
import DailySales from "@/components/charts/daily-sales";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import { Locale } from "@/navigation/navigation";

interface Props extends UserAdminDailySalesPageTexts {
  id: string;
  locale: Locale;
}

export default function UserAdminDailySalesPageContent({
  id,
  dailySalesTexts,
  menuTexts,
  themeSwitchTexts,
  header,
  title,
  findInSiteTexts,
  locale,
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
        locale,
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
              />
            </div>
          </>
        )}
      </div>
    </SidebarContentLayout>
  );
}
