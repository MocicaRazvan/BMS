import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import SingleDayTrainerPageContent from "@/app/[locale]/trainer/days/single/[id]/page-content";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SingleDayTexts } from "@/components/days/single-day";
import { getSingleDayTrainerPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import ScrollProgress from "@/components/common/scroll-progress";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: { locale: Locale; id: string };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "trainer.SingleDay",
    "/trainer/days/single/" + id,
    locale,
  );
}

export interface SingleDayTrainerPageTexts {
  singleDayTexts: SingleDayTexts;
  title: string;
  menuTexts: SidebarMenuTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  findInSiteTexts: FindInSiteTexts;
}

export default async function SingleDayTrainerPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);

  const [texts] = await Promise.all([getSingleDayTrainerPageTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        ...texts,
        mappingKey: "trainer",
      }}
    >
      <ScrollProgress />
      <div className="w-full bg-background ">
        <div className="mt-5">
          <SingleDayTrainerPageContent id={id} {...texts} />
        </div>
      </div>
    </SidebarContentLayout>
  );
}
