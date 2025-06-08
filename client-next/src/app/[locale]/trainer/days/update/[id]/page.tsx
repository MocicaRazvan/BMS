import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { Suspense } from "react";
import UpdateDayPageContent from "@/app/[locale]/trainer/days/update/[id]/page-content";
import LoadingSpinner from "@/components/common/loading-spinner";
import { DayFromTexts } from "@/components/forms/day-form";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getUpdateDayPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

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
  return await getIntlMetadata(
    "trainer.UpdateDay",
    "/trainer/days/update/" + id,
    locale,
  );
}

export interface UpdateDayPageTexts {
  dayFormTexts: DayFromTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}

export default async function UpdateDayPage({ params: { locale, id } }: Props) {
  unstable_setRequestLocale(locale);
  const [{ dayFormTexts, ...rest }] = await Promise.all([
    getUpdateDayPageTexts(),
  ]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: dayFormTexts.baseFormTexts.header,
        ...rest,
        mappingKey: "trainer",
      }}
    >
      <main className="flex items-center justify-center px-6 py-10">
        <Suspense fallback={<LoadingSpinner />}>
          <UpdateDayPageContent
            id={id}
            {...dayFormTexts}
            path={`/days/update/meals/${id}`}
          />
        </Suspense>
      </main>
    </SidebarContentLayout>
  );
}
