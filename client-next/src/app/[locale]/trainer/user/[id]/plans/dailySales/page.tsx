import DailySales, { DailySalesTexts } from "@/components/charts/daily-sales";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserDailySalesPageTexts } from "@/texts/pages";
import { getTheSameUserOrAdmin } from "@/lib/user";
import Heading from "@/components/common/heading";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";

export interface UserDailySalesPageTexts {
  dailySalesTexts: DailySalesTexts;
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
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
  const [texts, authUser] = await Promise.all([
    getUserDailySalesPageTexts(),
    getTheSameUserOrAdmin(id),
  ]);
  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,
        authUser,
        menuTexts: texts.menuTexts,
        mappingKey: "trainer",
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading {...texts} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full">
            <DailySales
              path={`/orders/trainer/countAndAmount/daily/${id}`}
              {...texts.dailySalesTexts}
              authUser={authUser}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
