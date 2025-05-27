import { Locale } from "@/navigation";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getAdminMonthlySalesTexts } from "@/texts/pages";
import { unstable_setRequestLocale } from "next-intl/server";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import MonthlySales, {
  MonthlySalesTexts,
} from "@/components/charts/monthly-sales";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { Separator } from "@/components/ui/separator";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

interface Props {
  params: { locale: Locale };
}
export interface AdminMonthlySalesTexts {
  title: string;
  header: string;
  plansTitle: string;
  monthlySalesTexts: MonthlySalesTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  plansMonthlySales: MonthlySalesTexts;
  findInSiteTexts: FindInSiteTexts;
}

export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.MonthlySales",
    "/admin/monthlySales",
    locale,
  );
}

export default async function AdminMonthlySales({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getAdminMonthlySalesTexts()]);

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
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full">
            <MonthlySales
              path={"/orders/admin/countAndAmount"}
              predictionPath={"/orders/admin/countAndAmount/prediction"}
              {...texts.monthlySalesTexts}
            />
          </div>
        </Suspense>
        <Separator className="my-10" />
        <h1 className="text-xl lg:text-2xl font-bold tracking-tight">
          {texts.plansTitle}
        </h1>
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full">
            <MonthlySales
              path={"/orders/admin/plans/countAndAmount"}
              predictionPath={"/orders/admin/plans/countAndAmount/prediction"}
              {...texts.plansMonthlySales}
              hideTotalAmount={true}
              countColorIndex={9}
              totalAmountColorIndex={10}
              characteristicProps={{
                plansPaths: {
                  typePath: "/orders/admin/countAndAmount/type",
                  objectivePath: "/orders/admin/countAndAmount/objective",
                  scatterPath: "/orders/admin/countAndAmount/objectiveType",
                },
                colors: {
                  countColorIndex: 9,
                  totalAmountColorIndex: 10,
                  averageAmountColorIndex: 8,
                  lineColorIndex: 2,
                },
              }}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
