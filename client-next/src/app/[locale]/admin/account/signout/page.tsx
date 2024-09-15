import { Locale } from "@/navigation";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { unstable_setRequestLocale } from "next-intl/server";
import { getSignOutPageTexts } from "@/texts/pages";
import { getThemeSwitchTexts } from "@/texts/components/nav";
import { getSidebarMenuTexts } from "@/texts/components/sidebar";
import {
  adminGroupLabels,
  adminLabels,
  adminSubLabels,
} from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { getUser } from "@/lib/user";
import SignOut from "@/app/[locale]/(main)/auth/signout/page-content";

interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.SignOut",
    "/admin/account/signout",
    locale,
  );
}

export default async function AdminSignOut({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [authUser, texts, themeSwitchTexts, menuTexts] = await Promise.all([
    getUser(),
    getSignOutPageTexts(),
    getThemeSwitchTexts(),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
  ]);
  return (
    <SidebarContentLayout
      navbarProps={{
        title: "Sign Out",
        themeSwitchTexts: themeSwitchTexts,
        authUser,
        menuTexts: menuTexts,
        mappingKey: "admin",
      }}
    >
      <div className="w-full h-full bg-background">
        <SignOut {...texts} />
      </div>
    </SidebarContentLayout>
  );
}
