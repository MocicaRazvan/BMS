import { Locale } from "@/navigation";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserPageTexts } from "@/texts/pages";
import UserPageContent from "@/app/[locale]/(main)/(user)/users/single/[id]/page-content";
import {
  getFindInSiteTexts,
  getThemeSwitchTexts,
} from "@/texts/components/nav";
import { getSidebarMenuTexts } from "@/texts/components/sidebar";
import {
  trainerGroupLabels,
  trainerLabels,
  trainerSubLabels,
} from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";

interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("trainer.Account", "/trainer/account", locale);
}

export default async function TrainerAccountPage({
  params: { locale },
}: Props) {
  unstable_setRequestLocale(locale);

  const [userPageTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getUserPageTexts(),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: userPageTexts.ownerTitle,
        themeSwitchTexts: themeSwitchTexts,
        menuTexts: menuTexts,
        mappingKey: "trainer",
        findInSiteTexts,
      }}
    >
      <div className="w-full h-full bg-background">
        <UserPageContent {...userPageTexts} />
      </div>
    </SidebarContentLayout>
  );
}
