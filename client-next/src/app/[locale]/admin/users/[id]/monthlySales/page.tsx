import { ThemeSwitchTexts } from "@/texts/components/nav";
import { MonthlySalesTexts } from "@/components/charts/monthly-sales";
import { AdminMenuTexts } from "@/components/admin/menu-list";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import { getUserAdminMonthlySalesPageTexts } from "@/texts/pages";
import UserAdminMonthlySalesPageContent from "@/app/[locale]/admin/users/[id]/monthlySales/page-content";
interface Props {
  params: { locale: Locale; id: string };
}
export interface UserAdminMonthlySalesPageTexts {
  title: string;
  header: string;
  monthlySalesTexts: MonthlySalesTexts;
  menuTexts: AdminMenuTexts;
  themeSwitchTexts: ThemeSwitchTexts;
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
