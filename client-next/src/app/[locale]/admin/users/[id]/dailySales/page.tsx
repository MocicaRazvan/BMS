import { Locale } from "@/navigation";
import { AdminMenuTexts } from "@/components/admin/menu-list";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { DailySalesTexts } from "@/components/charts/daily-sales";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserAdminDailySalesPageTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import UserAdminDailySalesPageContent from "@/app/[locale]/admin/users/[id]/dailySales/page-content";

interface Props {
  params: { locale: Locale; id: string };
}

export interface UserAdminDailySalesPageTexts {
  title: string;
  header: string;
  menuTexts: AdminMenuTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  dailySalesTexts: DailySalesTexts;
}

export default async function UserAdminDailySalesPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);

  const [texts, authUser] = await Promise.all([
    getUserAdminDailySalesPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);
  return (
    <UserAdminDailySalesPageContent id={id} {...texts} authUser={authUser} />
  );
}
