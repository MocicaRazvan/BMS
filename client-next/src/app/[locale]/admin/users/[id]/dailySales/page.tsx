import { Locale } from "@/navigation";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { DailySalesTexts } from "@/components/charts/daily-sales";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserAdminDailySalesPageTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import UserAdminDailySalesPageContent from "@/app/[locale]/admin/users/[id]/dailySales/page-content";
import { Metadata } from "next";
import { getIntlMetadata, getMetadataValues } from "@/texts/metadata";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

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

  const [texts, authUser] = await Promise.all([
    getUserAdminDailySalesPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);
  const metadataValues = await getMetadataValues(authUser, locale);

  return (
    <UserAdminDailySalesPageContent
      id={id}
      {...texts}
      authUser={authUser}
      metadataValues={metadataValues}
    />
  );
}
