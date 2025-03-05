import MonthlySales, {
  MonthlySalesTexts,
} from "@/components/charts/monthly-sales";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserMonthlySalesPageTexts } from "@/texts/pages";
import { getTheSameUserOrAdmin } from "@/lib/user";
import Heading from "@/components/common/heading";
import { Metadata } from "next";
import { getIntlMetadata, getMetadataValues } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import LoadingSpinner from "@/components/common/loading-spinner";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

export interface UserMonthlySalesPageTexts {
  monthlySalesTexts: MonthlySalesTexts;
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}
interface Props {
  params: { locale: Locale; id: string };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.MonthlySales",
      "/trainer/user/" + id + "/plans/monthlySales",
      locale,
    )),
  };
}

export default async function UsersMonthlySalesPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts, authUser] = await Promise.all([
    getUserMonthlySalesPageTexts(),
    getTheSameUserOrAdmin(id),
  ]);
  const metadataValues = await getMetadataValues(authUser, locale);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,
        authUser,
        menuTexts: texts.menuTexts,
        mappingKey: "trainer",
        findInSiteTexts: texts.findInSiteTexts,
        metadataValues,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading {...texts} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full">
            <MonthlySales
              path={`/orders/trainer/countAndAmount/${id}`}
              predictionPath={`/orders/trainer/countAndAmount/prediction/${id}`}
              {...texts.monthlySalesTexts}
              authUser={authUser}
              characteristicProps={{
                plansPaths: {
                  typePath: `/orders/trainer/countAndAmount/type/${id}`,
                  objectivePath: `/orders/trainer/countAndAmount/objective/${id}`,
                  scatterPath: `/orders/trainer/countAndAmount/objectiveType/${id}`,
                },
              }}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
