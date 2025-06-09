import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getRecipeFormTexts } from "@/texts/components/forms";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { getDuplicateRecipePageTexts } from "@/texts/pages";
import DuplicateRecipePageContent from "@/app/[locale]/trainer/recipes/duplicate/[id]/page-content";

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
      "trainer.DuplicateRecipe",
      "/trainer/recipes/duplicate/" + id,
      locale,
    )),
  };
}
export interface DuplicateRecipePageTexts {
  recipeFormTexts: Awaited<ReturnType<typeof getRecipeFormTexts>>;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}

export default async function DuplicateRecipePage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [{ recipeFormTexts, ...rest }] = await Promise.all([
    getDuplicateRecipePageTexts(),
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
        <DuplicateRecipePageContent
          id={id}
          {...recipeFormTexts}
          path={`/recipes/createWithVideos`}
        />
      </main>
    </SidebarContentLayout>
  );
}
