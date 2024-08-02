import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import { getUserWithMinRole } from "@/lib/user";
import AdminDashboardPageContent from "@/app/[locale]/admin/dashboard/page-content";

import { getAdminDashboardPageTexts } from "@/texts/pages";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";

interface Props {
  params: { locale: Locale };
}

export default async function AdminDashboard({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [texts, authUser] = await Promise.all([
    getAdminDashboardPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);
  return (
    <AdminContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,
        authUser,
        menuTexts: texts.menuTexts,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading {...texts} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full">
            <AdminDashboardPageContent authUser={authUser} {...texts} />
          </div>
        </Suspense>
      </div>
    </AdminContentLayout>
  );
}
