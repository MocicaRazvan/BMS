import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import { getPlanFormTexts } from "@/texts/components/forms";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import PlanForm, { PlanFormTexts } from "@/components/forms/plan-form";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getCreatePlanPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";

interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.CreatePlan",
      "/trainer/plans/create",
      locale,
    )),
  };
}

export interface CreatePlanPageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  planFormTexts: PlanFormTexts;
}

export default async function CreatePlanPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [authUser, { planFormTexts, ...rest }] = await Promise.all([
    getUserWithMinRole("ROLE_TRAINER"),
    getCreatePlanPageTexts(),
  ]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: planFormTexts.baseFormTexts.header,
        ...rest,
        authUser,
        mappingKey: "trainer",
      }}
    >
      <main className="flex items-center justify-center px-6 py-10">
        <Suspense fallback={<LoadingSpinner />}>
          <PlanForm
            authUser={authUser}
            {...planFormTexts}
            path={"/plans/createWithImages"}
            type="create"
          />
        </Suspense>
      </main>
    </SidebarContentLayout>
  );
}
