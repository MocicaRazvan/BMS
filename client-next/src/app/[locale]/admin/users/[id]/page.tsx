import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUser } from "@/lib/user";
import { getAdminUserPageTexts } from "@/texts/pages";
import { UserPageTexts } from "@/app/[locale]/(main)/(user)/users/single/[id]/page-content";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import AdminUserPageContent from "@/app/[locale]/admin/users/[id]/page-content";

interface Props {
  params: { locale: Locale; id: string };
}

export interface AdminUserPageTexts {
  userPageTexts: UserPageTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  header: string;
  title: string;
  menuTexts: SidebarMenuTexts;
}

export default async function AdminUserPage({ params: { locale, id } }: Props) {
  unstable_setRequestLocale(locale);

  const [authUser, texts] = await Promise.all([
    getUser(),
    getAdminUserPageTexts(),
  ]);

  return <AdminUserPageContent authUser={authUser} id={id} {...texts} />;
}
