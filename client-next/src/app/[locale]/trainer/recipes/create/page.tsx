import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getRecipeFormTexts } from "@/texts/components/forms";
import RecipeForm from "@/components/forms/recipe-form";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getCreateRecipePageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.CreateRecipe",
      "/trainer/recipes/create",
      locale,
    )),
  };
}
export interface CreateRecipePageTexts {
  recipeFormTexts: Awaited<ReturnType<typeof getRecipeFormTexts>>;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}

export default async function CreateRecipePage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [{ recipeFormTexts, themeSwitchTexts, menuTexts, findInSiteTexts }] =
    await Promise.all([getCreateRecipePageTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: recipeFormTexts.baseFormTexts.header,
        themeSwitchTexts,
        menuTexts,
        mappingKey: "trainer",
        findInSiteTexts,
      }}
    >
      <main className="flex items-center justify-center px-6 py-10">
        <RecipeForm
          {...recipeFormTexts}
          path={"/recipes/createWithVideos"}
          type="create"
        />
      </main>
    </SidebarContentLayout>
  );
}
