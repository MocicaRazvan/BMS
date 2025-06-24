import { Locale } from "@/navigation/navigation";
import SingleTrainerPlanPageContent, {
  SingleTrainerPlanPageTexts,
} from "@/app/[locale]/trainer/plans/single/[id]/page-content";
import { getTrainerPlanPageTexts } from "@/texts/pages";
import { unstable_setRequestLocale } from "next-intl/server";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import ScrollProgress from "@/components/common/scroll-progress";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: { locale: Locale; id: string };
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.SinglePlan",
      "/trainer/plans/single/" + id,
      locale,
    )),
  };
}

export interface TrainerPlanPageTexts {
  singleTrainerPlanPageTexts: SingleTrainerPlanPageTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}

export default async function SingleTrainerPlanPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [{ singleTrainerPlanPageTexts, ...rest }] = await Promise.all([
    getTrainerPlanPageTexts(),
  ]);

  return (
    <SidebarContentLayout
      navbarProps={{
        ...rest,
        mappingKey: "trainer",
        locale,
      }}
    >
      <ScrollProgress />
      <div className="w-full bg-background ">
        <SingleTrainerPlanPageContent id={id} {...singleTrainerPlanPageTexts} />
      </div>
    </SidebarContentLayout>
  );
}
