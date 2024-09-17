import { PlanTableTexts } from "@/components/table/plans-table";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserPlansPageTexts } from "@/texts/pages";
import { getTheSameUserOrAdmin } from "@/lib/user";
import { sortingPlansSortingOptionsKeys } from "@/texts/components/list";
import Heading from "@/components/common/heading";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import LoadingSpinner from "@/components/common/loading-spinner";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import UsersPlansPageContent from "@/app/[locale]/trainer/user/[id]/plans/page-content";

export interface UserPlansPageTexts {
  planTableTexts: PlanTableTexts;
  sortingPlansSortingOptions: SortingOptionsTexts;
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
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

  const [userPlansPageTexts, authUser] = await Promise.all([
    getUserPlansPageTexts(),
    getTheSameUserOrAdmin(id),
  ]);

  const plansOptions = getSortingOptions(
    sortingPlansSortingOptionsKeys,
    userPlansPageTexts.sortingPlansSortingOptions,
  );
  return (
    <SidebarContentLayout
      navbarProps={{
        ...userPlansPageTexts,
        authUser,
        mappingKey: "trainer",
      }}
    >
      <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 mx-auto ">
        <Heading {...userPlansPageTexts} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="">
            <UsersPlansPageContent
              path={`/plans/trainer/filteredWithCount/${id}`}
              forWhom={"trainer"}
              {...userPlansPageTexts.planTableTexts}
              sortingOptions={plansOptions}
              authUser={authUser}
              sizeOptions={[10, 20, 30, 40]}
            />
          </div>
        </Suspense>
      </div>{" "}
    </SidebarContentLayout>
  );
}
