import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getRecipeFormTexts } from "@/texts/components/forms";
import UpdateRecipePageContent from "@/app/[locale]/trainer/recipes/update/[id]/page-content";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { getUpdateRecipePageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: {
    locale: Locale;
    id: string;
  };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.UpdateRecipe",
      "/trainer/recipes/update/" + id,
      locale,
    )),
  };
}
export interface UpdateRecipePageTexts {
  recipeFormTexts: Awaited<ReturnType<typeof getRecipeFormTexts>>;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}

export default async function UpdateRecipePage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [{ recipeFormTexts, ...rest }] = await Promise.all([
    getUpdateRecipePageTexts(),
  ]);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: recipeFormTexts.baseFormTexts.header,
        ...rest,
        mappingKey: "trainer",
      }}
    >
      <main className="flex items-center justify-center px-6 py-10">
        <UpdateRecipePageContent
          id={id}
          {...recipeFormTexts}
          path={`/recipes/updateWithVideos/${id}`}
        />
      </main>
    </SidebarContentLayout>
  );
}
