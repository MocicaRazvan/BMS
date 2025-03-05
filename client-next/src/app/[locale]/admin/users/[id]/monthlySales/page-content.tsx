"use client";
import { UserAdminMonthlySalesPageTexts } from "@/app/[locale]/admin/users/[id]/monthlySales/page";
import { WithUser } from "@/lib/user";
import useGetUser from "@/hoooks/useGetUser";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import LoadingSpinner from "@/components/common/loading-spinner";
import Heading from "@/components/common/heading";
import MonthlySales from "@/components/charts/monthly-sales";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { MetadataValue } from "@/components/nav/find-in-site";

interface Props extends UserAdminMonthlySalesPageTexts, WithUser {
  id: string;
  metadataValues: MetadataValue[];
}

export default function UserAdminMonthlySalesPageContent({
  id,
  monthlySalesTexts,
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
              <MonthlySales
                path={`/orders/trainer/countAndAmount/${id}`}
                predictionPath={`/orders/trainer/countAndAmount/prediction/${id}`}
                {...monthlySalesTexts}
                authUser={authUser}
              />
            </div>
          </>
        )}
      </div>
    </SidebarContentLayout>
  );
}
