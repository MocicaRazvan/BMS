import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import AdminDashboardPageContent from "@/app/[locale]/admin/dashboard/page-content";

import { getAdminDashboardPageTexts } from "@/texts/pages";
import Heading from "@/components/common/heading";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("admin.Dashboard", "/admin/dashboard", locale);
}

export default async function AdminDashboard({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getAdminDashboardPageTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,
        menuTexts: texts.menuTexts,
        mappingKey: "admin",
        findInSiteTexts: texts.findInSiteTexts,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading {...texts} />
        <div className="mt-10 h-full">
          <AdminDashboardPageContent {...texts} locale={locale} />
        </div>
      </div>
    </SidebarContentLayout>
  );
}
