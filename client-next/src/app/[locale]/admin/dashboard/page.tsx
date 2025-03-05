import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { getUserWithMinRole } from "@/lib/user";
import AdminDashboardPageContent from "@/app/[locale]/admin/dashboard/page-content";

import { getAdminDashboardPageTexts } from "@/texts/pages";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import { Metadata } from "next";
import { getIntlMetadata, getMetadataValues } from "@/texts/metadata";

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
  const [texts, authUser] = await Promise.all([
    getAdminDashboardPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);
  const metadataValues = await getMetadataValues(authUser, locale);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,
        authUser,
        menuTexts: texts.menuTexts,
        mappingKey: "admin",
        findInSiteTexts: texts.findInSiteTexts,
        metadataValues,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading {...texts} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full">
            <AdminDashboardPageContent
              authUser={authUser}
              {...texts}
              locale={locale}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
