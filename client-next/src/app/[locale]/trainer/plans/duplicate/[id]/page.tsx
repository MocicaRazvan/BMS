import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { PlanFormTexts } from "@/components/forms/plan-form";
import { getDuplicatePlanPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import DuplicatePlanPageContent from "@/app/[locale]/trainer/plans/duplicate/[id]/page-content";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: {
    locale: Locale;
    id: string;
  };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.DuplicatePlan",
      "/trainer/plans/duplicate/" + id,
      locale,
    )),
  };
}

export interface DuplicatePlanPageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  planFormTexts: PlanFormTexts;
  findInSiteTexts: FindInSiteTexts;
}

export default async function DuplicatePlanPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [{ planFormTexts, ...rest }] = await Promise.all([
    getDuplicatePlanPageTexts(),
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
        <DuplicatePlanPageContent
          id={id}
          {...planFormTexts}
          path={`/plans/createWithImages`}
        />
      </main>
    </SidebarContentLayout>
  );
}
