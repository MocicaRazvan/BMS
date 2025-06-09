import SingeRecipePageContent, {
  SingleRecipePageTexts,
} from "@/app/[locale]/trainer/recipes/single/[id]/page-content";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getTrainerSingleRecipePageTexts } from "@/texts/pages";
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
      "trainer.SingleRecipe",
      "/trainer/recipes/single/" + id,
      locale,
    )),
  };
}

export interface TrainerSingleRecipePageTexts {
  singleRecipePageTexts: SingleRecipePageTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  title: string;
  findInSiteTexts: FindInSiteTexts;
}

export default async function SingleRecipePage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);

  const [texts] = await Promise.all([getTrainerSingleRecipePageTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        ...texts,
        mappingKey: "trainer",
      }}
    >
      <ScrollProgress />
      <div className="w-full bg-background ">
        <SingeRecipePageContent id={id} {...texts.singleRecipePageTexts} />
      </div>
    </SidebarContentLayout>
  );
}
