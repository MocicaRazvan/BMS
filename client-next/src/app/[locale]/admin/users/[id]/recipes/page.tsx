import { RecipeTableTexts } from "@/components/table/recipes-table";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserRecipesAdminPageTexts } from "@/texts/pages";
import { sortingRecipesSortingOptionsKeys } from "@/texts/components/list";
import UserRecipesAdminPageContent from "@/app/[locale]/admin/users/[id]/recipes/page-content";
import { notFound } from "next/navigation";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

export interface UserRecipesAdminPageTexts {
  recipesTableTexts: RecipeTableTexts;
  sortingRecipesSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}
interface Props {
  params: { locale: Locale; id: string };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.UserRecipes",
    "/admin/users/" + id + "/recipes",
    locale,
  );
}

export default async function UserRecipesAdminPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getUserRecipesAdminPageTexts()]);

  const recipesOptions = getSortingOptions(
    sortingRecipesSortingOptionsKeys,
    texts.sortingRecipesSortingOptions,
  );
  if (!id) {
    notFound();
  }
  return (
    <UserRecipesAdminPageContent
      id={id}
      sortingOptions={recipesOptions}
      {...texts}
      path={`/recipes/trainer/filteredWithCount/${id}`}
    />
  );
}
