import { PlanTableTexts } from "@/components/table/plans-table";
import { getSortingOptions, SortingOptionsTexts } from "@/types/constants";
import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserPlansPageTexts } from "@/texts/pages";
import { sortingPlansSortingOptionsKeys } from "@/texts/components/list";
import Heading from "@/components/common/heading";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import UsersPlansPageContent from "@/app/[locale]/trainer/user/[id]/plans/page-content";
import { Separator } from "@/components/ui/separator";
import TopPlans, { TopPlansTexts } from "@/components/charts/top-plans";
import IsTheSameUserOrAdmin from "@/app/[locale]/trainer/user/is-the-same-user-or-admin";
import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

export interface UserPlansPageTexts {
  planTableTexts: PlanTableTexts;
  sortingPlansSortingOptions: SortingOptionsTexts;
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  topPlansTexts: TopPlansTexts;
  findInSiteTexts: FindInSiteTexts;
}
interface Props {
  params: { locale: Locale; id: string };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.TrainerPlans",
      "/trainer/user/" + id + "/plans",
      locale,
    )),
  };
}

export default async function UsersPlansPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);

  const [userPlansPageTexts] = await Promise.all([getUserPlansPageTexts()]);

  const plansOptions = getSortingOptions(
    sortingPlansSortingOptionsKeys,
    userPlansPageTexts.sortingPlansSortingOptions,
  );
  return (
    <IsTheSameUserOrAdmin id={id}>
      <SidebarContentLayout
        navbarProps={{
          ...userPlansPageTexts,
          mappingKey: "trainer",
        }}
      >
        <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 mx-auto ">
          <Heading {...userPlansPageTexts} />
          <div>
            <UsersPlansPageContent
              path={`/plans/trainer/filteredWithCount/${id}`}
              forWhom={"trainer"}
              {...userPlansPageTexts.planTableTexts}
              sortingOptions={plansOptions}
              sizeOptions={[10, 20, 30, 40]}
            />
            <Separator className="mt-2" />
            <div className=" my-5 h-full w-full">
              <TopPlans
                texts={userPlansPageTexts.topPlansTexts}
                locale={locale}
                path={`/orders/trainer/topPlans/${id}`}
              />
            </div>
          </div>
        </div>
      </SidebarContentLayout>
    </IsTheSameUserOrAdmin>
  );
}
