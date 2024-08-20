import { Locale } from "@/navigation";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getAdminMonthlySalesTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import { unstable_setRequestLocale } from "next-intl/server";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import MonthlySales, {
  MonthlySalesTexts,
} from "@/components/charts/monthly-sales";
import { AdminMenuTexts } from "@/components/admin/menu-list";

interface Props {
  params: { locale: Locale };
}
export interface AdminMonthlySalesTexts {
  title: string;
  header: string;
  monthlySalesTexts: MonthlySalesTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: AdminMenuTexts;
}

export default async function AdminMonthlySales({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [texts, authUser] = await Promise.all([
    getAdminMonthlySalesTexts(),
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
            <MonthlySales
              path={"/orders/admin/countAndAmount"}
              authUser={authUser}
              {...texts.monthlySalesTexts}
            />
          </div>
        </Suspense>
      </div>
    </AdminContentLayout>
  );
}
