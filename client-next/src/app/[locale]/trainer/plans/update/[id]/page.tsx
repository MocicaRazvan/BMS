import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import UpdatePlanPageContent from "@/app/[locale]/trainer/plans/update/[id]/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { PlanFormTexts } from "@/components/forms/plan-form";
import { getUpdatePlanPageTexts } from "@/texts/pages";
import LoadingSpinner from "@/components/common/loading-spinner";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

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
      "trainer.UpdatePlan",
      "/trainer/plans/update/" + id,
      locale,
    )),
  };
}

export interface UpdatePlanPageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  planFormTexts: PlanFormTexts;
  findInSiteTexts: FindInSiteTexts;
}

export default async function UpdatePlanPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [{ planFormTexts, ...rest }] = await Promise.all([
    getUpdatePlanPageTexts(),
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
        <Suspense fallback={<LoadingSpinner />}>
          <UpdatePlanPageContent
            id={id}
            {...planFormTexts}
            path={`/plans/updateWithImages/${id}`}
          />
        </Suspense>
      </main>
    </SidebarContentLayout>
  );
}
