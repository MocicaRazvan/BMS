import { ThemeSwitchTexts } from "@/texts/components/nav";
import GeographyChart, {
  GeographyChartTexts,
} from "@/components/charts/geography-chart";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getAdminCountriesTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

export interface AdminCountriesTexts {
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  geographyChartTexts: GeographyChartTexts;
  menuTexts: SidebarMenuTexts;
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

  const [texts, authUser] = await Promise.all([
    getAdminCountriesTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,
        authUser,
        menuTexts: texts.menuTexts,
        mappingKey: "admin",
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading {...texts} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full pb-5">
            <GeographyChart {...texts.geographyChartTexts} />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
