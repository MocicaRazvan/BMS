import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import PlansTable, { PlanTableTexts } from "@/components/table/plans-table";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getAdminPlansPageTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import { sortingPlansSortingOptionsKeys } from "@/texts/components/list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import AdminPlansPageContent from "@/app/[locale]/admin/plans/page-content";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: { locale: Locale };
}

export interface AdminPlansPageTexts {
  planTableTexts: PlanTableTexts;
  sortingPlansSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
}

export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("admin.Plans", "/admin/plans", locale);
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
    <SidebarContentLayout
      navbarProps={{
        title,
        themeSwitchTexts,
        authUser,
        menuTexts,
        mappingKey: "admin",
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
    </SidebarContentLayout>
  );
}
