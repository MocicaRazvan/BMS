import { Locale } from "@/navigation";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { unstable_setRequestLocale } from "next-intl/server";
import { getAdminDailySalesTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import DailySales, { DailySalesTexts } from "@/components/charts/daily-sales";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: { locale: Locale };
}

export interface AdminDailySalesTexts {
  title: string;
  header: string;
  dailySalesTexts: DailySalesTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
}

export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("admin.DailySales", "/admin/dailySales", locale);
}
export default async function AdminDailySales({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [texts, authUser] = await Promise.all([
    getAdminDailySalesTexts(),
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
          <div className="mt-10 h-full">
            <DailySales
              path={"/orders/admin/countAndAmount/daily"}
              authUser={authUser}
              {...texts.dailySalesTexts}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
