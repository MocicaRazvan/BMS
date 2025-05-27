"use client";
import { UserAdminMonthlySalesPageTexts } from "@/app/[locale]/admin/users/[id]/monthlySales/page";
import useGetUser from "@/hoooks/useGetUser";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import LoadingSpinner from "@/components/common/loading-spinner";
import Heading from "@/components/common/heading";
import MonthlySales from "@/components/charts/monthly-sales";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

interface Props extends UserAdminMonthlySalesPageTexts {
  id: string;
}

export default function UserAdminMonthlySalesPageContent({
  id,
  monthlySalesTexts,
  menuTexts,
  themeSwitchTexts,
  header,
  title,
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
              />
            </div>
          </>
        )}
      </div>
    </SidebarContentLayout>
  );
}
