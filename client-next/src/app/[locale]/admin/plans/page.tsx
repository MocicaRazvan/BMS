import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import PlansTable, { PlanTableTexts } from "@/components/table/plans-table";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getAdminPlansPageTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import { sortingPlansSortingOptionsKeys } from "@/texts/components/list";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import AdminPlansPageContent from "@/app/[locale]/admin/plans/page-content";
import { AdminMenuTexts } from "@/components/admin/menu-list";

interface Props {
  params: { locale: Locale };
}

export interface AdminPlansPageTexts {
  planTableTexts: PlanTableTexts;
  sortingPlansSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: AdminMenuTexts;
}

export default async function AdminPlansPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [
    {
      planTableTexts,
      sortingPlansSortingOptions,
      themeSwitchTexts,
      header,
      title,
      menuTexts,
    },
    authUser,
  ] = await Promise.all([
    getAdminPlansPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);

  const plansOptions = getSortingOptions(
    sortingPlansSortingOptionsKeys,
    sortingPlansSortingOptions,
  );

  return (
    <AdminContentLayout
      navbarProps={{
        title,
        themeSwitchTexts,
        authUser,
        menuTexts,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading title={title} header={header} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full">
            <AdminPlansPageContent
              path={"/plans/filteredWithCount"}
              forWhom={"admin"}
              {...planTableTexts}
              sortingOptions={plansOptions}
              sizeOptions={[10, 20, 30, 40]}
              authUser={authUser}
              mainDashboard={true}
            />
          </div>
        </Suspense>
      </div>
    </AdminContentLayout>
  );
}
