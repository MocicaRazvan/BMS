import { ThemeSwitchTexts } from "@/texts/components/nav";
import GeographyChart from "@/components/charts/geography-chart";
import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getAdminCountriesTexts } from "@/texts/pages";
import Heading from "@/components/common/heading";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";
import { GeographyChartTexts } from "@/components/charts/geography-chart-content";

export interface AdminCountriesTexts {
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  geographyChartTexts: GeographyChartTexts;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}
interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("admin.Countries", "/admin/countries", locale);
}

export default async function AdminCountries({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);

  const [texts] = await Promise.all([getAdminCountriesTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,
        menuTexts: texts.menuTexts,
        mappingKey: "admin",
        findInSiteTexts: texts.findInSiteTexts,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading {...texts} />
        <div className="mt-10 h-full pb-5">
          <GeographyChart {...texts.geographyChartTexts} />
        </div>
      </div>
    </SidebarContentLayout>
  );
}
