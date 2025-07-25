"use client";

import { UseListProps } from "@/hoooks/useList";
import { UserPlansAdminPageTexts } from "@/app/[locale]/admin/users/[id]/plans/page";
import useGetUser from "@/hoooks/useGetUser";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import LoadingSpinner from "@/components/common/loading-spinner";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import PlansTable from "@/components/table/plans-table";
import { useSidebarToggle } from "@/context/sidebar-toggle";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { Separator } from "@/components/ui/separator";
import TopPlans from "@/components/charts/top-plans";
import { Locale } from "@/navigation/navigation";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

interface Props extends UserPlansAdminPageTexts, UseListProps {
  id: string;
  locale: Locale;
}

export default function UserPlansAdminPageContent({
  id,
  title,
  themeSwitchTexts,
  plansTableTexts,
  header,
  path,
  sortingOptions,
  menuTexts,
  topPlansTexts,
  locale,
  findInSiteTexts,
}: Props) {
  const { authUser } = useAuthUserMinRole();

  const { isOpen } = useSidebarToggle();
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
            <Suspense fallback={<LoadingSpinner />}>
              <div className="mt-10 h-full">
                <PlansTable
                  path={path}
                  forWhom={"admin"}
                  sortingOptions={sortingOptions}
                  {...plansTableTexts}
                  sizeOptions={[10, 20, 30, 40]}
                  isSidebarOpen={isOpen}
                  mainDashboard={true}
                />
                <Separator className="mt-2" />
                <div className=" my-5 h-full w-full">
                  <TopPlans
                    texts={topPlansTexts}
                    locale={locale}
                    path={`/orders/trainer/topPlans/${id}`}
                  />
                </div>
              </div>
            </Suspense>
          </>
        )}
      </div>
    </SidebarContentLayout>
  );
}
