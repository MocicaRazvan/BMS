import { Locale } from "@/navigation/navigation";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { unstable_setRequestLocale } from "next-intl/server";
import { getAdminDailySalesTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import DailySales, { DailySalesTexts } from "@/components/charts/daily-sales";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { Separator } from "@/components/ui/separator";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: { locale: Locale };
}

export interface AdminDailySalesTexts {
  title: string;
  header: string;
  plansTitle: string;
  dailySalesTexts: DailySalesTexts;
  plansDailySalesTexts: DailySalesTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}

export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("admin.DailySales", "/admin/dailySales", locale);
}
export default async function AdminDailySales({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getAdminDailySalesTexts()]);

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
        <div className="mt-10 h-full">
          <DailySales
            path={"/orders/admin/countAndAmount/daily"}
            {...texts.dailySalesTexts}
          />
        </div>
        <Separator className="my-10" />
        <h1 className="text-xl lg:text-2xl font-bold tracking-tight">
          {texts.plansTitle}
        </h1>
        <div className="mt-10 h-full">
          <DailySales
            path={"/orders/admin/countAndAmount/daily"}
            {...texts.plansDailySalesTexts}
            hideTotalAmount={true}
            countColorIndex={9}
          />
        </div>
      </div>
    </SidebarContentLayout>
  );
}
