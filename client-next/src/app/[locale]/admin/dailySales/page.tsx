import { Locale } from "@/navigation";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { unstable_setRequestLocale } from "next-intl/server";
import { getAdminDailySalesTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import DailySales, { DailySalesTexts } from "@/components/charts/daily-sales";
import { AdminMenuTexts } from "@/components/admin/menu-list";

interface Props {
  params: { locale: Locale };
}

export interface AdminDailySalesTexts {
  title: string;
  header: string;
  dailySalesTexts: DailySalesTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: AdminMenuTexts;
}

export default async function AdminDailySales({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [texts, authUser] = await Promise.all([
    getAdminDailySalesTexts(),
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
            <DailySales
              path={"/orders/admin/countAndAmount/daily"}
              authUser={authUser}
              {...texts.dailySalesTexts}
            />
          </div>
        </Suspense>
      </div>
    </AdminContentLayout>
  );
}
