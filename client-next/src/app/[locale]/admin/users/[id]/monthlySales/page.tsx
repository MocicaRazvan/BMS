import { ThemeSwitchTexts } from "@/texts/components/nav";
import { MonthlySalesTexts } from "@/components/charts/monthly-sales";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import { getUserAdminMonthlySalesPageTexts } from "@/texts/pages";
import UserAdminMonthlySalesPageContent from "@/app/[locale]/admin/users/[id]/monthlySales/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: { locale: Locale; id: string };
}
export interface UserAdminMonthlySalesPageTexts {
  title: string;
  header: string;
  monthlySalesTexts: MonthlySalesTexts;
  menuTexts: SidebarMenuTexts;
  themeSwitchTexts: ThemeSwitchTexts;
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.UserMonthlySales",
    "/admin/users/" + id + "/monthlySales",
    locale,
  );
}

export default async function UserAdminMonthlySalesPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts, authUser] = await Promise.all([
    getUserAdminMonthlySalesPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);

  return (
    <UserAdminMonthlySalesPageContent id={id} {...texts} authUser={authUser} />
  );
}
