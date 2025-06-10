import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import AdminEmail, { AdminEmailTexts } from "@/components/forms/admin-email";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getAdminEmailPageTexts } from "@/texts/pages";
import Heading from "@/components/common/heading";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: { locale: Locale };
}

export interface AdminEmailPageTexts {
  adminEmail: AdminEmailTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("admin.Email", "/admin/email", locale);
}

export default async function AdminEmailPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getAdminEmailPageTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        ...texts,
        mappingKey: "admin",
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading {...texts} />
        <div className="mt-10 h-full w-full">
          <AdminEmail {...texts.adminEmail} />
        </div>
      </div>
    </SidebarContentLayout>
  );
}
