import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getAdminUserPageTexts } from "@/texts/pages";
import { UserPageTexts } from "@/app/[locale]/(main)/(user)/users/single/[id]/page-content";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import AdminUserPageContent from "@/app/[locale]/admin/users/[id]/page-content";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: { locale: Locale; id: string };
}

export interface AdminUserPageTexts {
  userPageTexts: UserPageTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  header: string;
  title: string;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}

export default async function AdminUserPage({ params: { locale, id } }: Props) {
  unstable_setRequestLocale(locale);

  const [texts] = await Promise.all([getAdminUserPageTexts()]);

  return <AdminUserPageContent id={id} {...texts} />;
}
