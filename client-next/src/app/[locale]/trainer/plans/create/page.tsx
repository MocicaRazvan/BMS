import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import PlanForm, { PlanFormTexts } from "@/components/forms/plan-form";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getCreatePlanPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

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
  findInSiteTexts: FindInSiteTexts;
}

export default async function CreatePlanPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [{ planFormTexts, ...rest }] = await Promise.all([
    getCreatePlanPageTexts(),
  ]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: planFormTexts.baseFormTexts.header,
        ...rest,
        mappingKey: "trainer",
      }}
    >
      <main className="flex items-center justify-center px-6 py-10">
        <PlanForm
          {...planFormTexts}
          path={"/plans/createWithImages"}
          type="create"
        />
      </main>
    </SidebarContentLayout>
  );
}
