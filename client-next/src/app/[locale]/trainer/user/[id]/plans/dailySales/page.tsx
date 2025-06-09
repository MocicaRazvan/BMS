import DailySales, { DailySalesTexts } from "@/components/charts/daily-sales";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserDailySalesPageTexts } from "@/texts/pages";
import Heading from "@/components/common/heading";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import IsTheSameUserOrAdmin from "@/app/[locale]/trainer/user/is-the-same-user-or-admin";
import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

export interface UserDailySalesPageTexts {
  dailySalesTexts: DailySalesTexts;
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}
interface Props {
  params: { locale: Locale; id: string };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.DailySales",
      "/trainer/user/" + id + "/plans/dailySales",
      locale,
    )),
  };
}

export default async function UsersDailySalesPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getUserDailySalesPageTexts()]);

  return (
    <IsTheSameUserOrAdmin id={id}>
      <SidebarContentLayout
        navbarProps={{
          title: texts.title,
          themeSwitchTexts: texts.themeSwitchTexts,
          menuTexts: texts.menuTexts,
          mappingKey: "trainer",
          findInSiteTexts: texts.findInSiteTexts,
        }}
      >
        <div className="w-full h-full bg-background">
          <Heading {...texts} />
          <div className="mt-10 h-full">
            <DailySales
              path={`/orders/trainer/countAndAmount/daily/${id}`}
              {...texts.dailySalesTexts}
            />
          </div>
        </div>
      </SidebarContentLayout>
    </IsTheSameUserOrAdmin>
  );
}
