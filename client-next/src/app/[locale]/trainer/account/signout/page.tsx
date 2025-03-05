import { Locale } from "@/navigation";
import { Metadata } from "next";
import { getIntlMetadata, getMetadataValues } from "@/texts/metadata";
import { unstable_setRequestLocale } from "next-intl/server";
import { getSignOutPageTexts } from "@/texts/pages";
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
import { getUser } from "@/lib/user";
import SignOut from "@/app/[locale]/(main)/auth/signout/page-content";

interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "trainer.SignOut",
    "/trainer/account/signout",
    locale,
  );
}

export default async function TrainerSignOut({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [authUser, texts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getUser(),
      getSignOutPageTexts(),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  const metadataValues = await getMetadataValues(authUser, locale);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: "Sign Out",
        themeSwitchTexts: themeSwitchTexts,
        authUser,
        menuTexts: menuTexts,
        mappingKey: "trainer",
        findInSiteTexts,
        metadataValues,
      }}
    >
      <div className="w-full h-full bg-background">
        <SignOut {...texts} locale={locale} />
      </div>
    </SidebarContentLayout>
  );
}
