import { unstable_setRequestLocale } from "next-intl/server";
import { PlanTableTexts } from "@/components/table/plans-table";
import { getSortingOptions, SortingOptionsTexts } from "@/types/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getAdminPlansPageTexts } from "@/texts/pages";
import { sortingPlansSortingOptionsKeys } from "@/texts/components/list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import AdminPlansPageContent from "@/app/[locale]/admin/plans/page-content";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import ArchiveQueueCards, {
  ArchiveQueueCardsTexts,
} from "@/components/archive/archive-queue-card";
import { Separator } from "@/components/ui/separator";
import TopPlans, { TopPlansTexts } from "@/components/charts/top-plans";
import { LocaleProps } from "@/navigation";
import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

export interface AdminPlansPageTexts {
  planTableTexts: PlanTableTexts;
  sortingPlansSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
  archivePlansTexts: ArchiveQueueCardsTexts;
  archiveDayTexts: ArchiveQueueCardsTexts;
  topPlansTexts: TopPlansTexts;
  findInSiteTexts: FindInSiteTexts;
}

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return await getIntlMetadata("admin.Plans", "/admin/plans", locale);
}

export default async function AdminPlansPage({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const [
    {
      planTableTexts,
      sortingPlansSortingOptions,
      themeSwitchTexts,
      header,
      title,
      menuTexts,
      archivePlansTexts,
      topPlansTexts,
      findInSiteTexts,
      archiveDayTexts,
    },
  ] = await Promise.all([getAdminPlansPageTexts()]);

  const plansOptions = getSortingOptions(
    sortingPlansSortingOptionsKeys,
    sortingPlansSortingOptions,
  );

  return (
    <SidebarContentLayout
      navbarProps={{
        title,
        themeSwitchTexts,

        menuTexts,
        mappingKey: "admin",
        findInSiteTexts,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading title={title} header={header} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full space-y-10">
            <AdminPlansPageContent
              path={"/plans/filteredWithCount"}
              forWhom={"admin"}
              {...planTableTexts}
              sortingOptions={plansOptions}
              sizeOptions={[10, 20, 30, 40]}
              mainDashboard={true}
              extraQueryParams={{
                admin: "true",
              }}
            />
            <Separator className="mt-2" />
            <div className=" my-5 h-full w-full">
              <TopPlans
                texts={topPlansTexts}
                locale={locale}
                path="/orders/admin/topPlans"
              />
            </div>
            <Separator />
            <div className="space-y-5">
              <ArchiveQueueCards
                prefix={"plan"}
                locale={locale}
                showHeader={true}
                {...archivePlansTexts}
              />
              <ArchiveQueueCards
                prefix={"day"}
                locale={locale}
                showHeader={false}
                {...archiveDayTexts}
              />
            </div>
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
