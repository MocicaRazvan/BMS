import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import SingleIngredientPageContent, {
  SingleIngredientPageTexts,
} from "@/app/[locale]/trainer/ingredients/single/[id]/page-content";
import { getTrainerSingleIngredientPageTexts } from "@/texts/pages";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: { id: string; locale: Locale };
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.SingleIngredient",
      "/trainer/single/" + id,
      locale,
    )),
  };
}
export interface TrainerSingleIngredientPageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  menuTexts: SidebarMenuTexts;
  singleIngredientPageTexts: SingleIngredientPageTexts;
  findInSiteTexts: FindInSiteTexts;
}
export default async function SingleIngredientPage({
  params: { id, locale },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getTrainerSingleIngredientPageTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        ...texts,
        mappingKey: "trainer",
      }}
    >
      <div className="w-full bg-background ">
        <div className="mt-5">
          <SingleIngredientPageContent
            {...texts.singleIngredientPageTexts}
            id={id}
          />
        </div>
      </div>
    </SidebarContentLayout>
  );
}
