import DayForm, { DayFromTexts } from "@/components/forms/day-form";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getCreateDayPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Locale } from "@/navigation/navigation";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { unstable_setRequestLocale } from "next-intl/server";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: { locale: Locale };
}

export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "trainer.CreateDay",
    "/trainer/days/create",
    locale,
  );
}
export interface CreateDayPageTexts {
  dayFormTexts: DayFromTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}

export default async function CreateDayPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [{ dayFormTexts, ...rest }] = await Promise.all([
    getCreateDayPageTexts(),
  ]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: dayFormTexts.baseFormTexts.header,
        ...rest,
        mappingKey: "trainer",
        locale,
      }}
    >
      <main className="flex items-center justify-center px-6 py-10">
        <DayForm {...dayFormTexts} path={"/days/create/meals"} />
      </main>
    </SidebarContentLayout>
  );
}
