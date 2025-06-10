import { Locale } from "@/navigation/navigation";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { DailySalesTexts } from "@/components/charts/daily-sales";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserAdminDailySalesPageTexts } from "@/texts/pages";
import UserAdminDailySalesPageContent from "@/app/[locale]/admin/users/[id]/dailySales/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: { locale: Locale; id: string };
}

export interface UserAdminDailySalesPageTexts {
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  dailySalesTexts: DailySalesTexts;
  findInSiteTexts: FindInSiteTexts;
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.UserDailySales",
    "/admin/users/" + id + "/dailySales",
    locale,
  );
}

export default async function UserAdminDailySalesPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);

  const [texts] = await Promise.all([getUserAdminDailySalesPageTexts()]);

  return <UserAdminDailySalesPageContent id={id} {...texts} />;
}
