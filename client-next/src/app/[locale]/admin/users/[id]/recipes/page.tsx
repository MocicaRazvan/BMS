import { RecipeTableTexts } from "@/components/table/recipes-table";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserRecipesAdminPageTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import { sortingRecipesSortingOptionsKeys } from "@/texts/components/list";
import UserRecipesAdminPageContent from "@/app/[locale]/admin/users/[id]/recipes/page-content";
import { notFound } from "next/navigation";
import { AdminMenuTexts } from "@/components/admin/menu-list";

export interface UserRecipesAdminPageTexts {
  recipesTableTexts: RecipeTableTexts;
  sortingRecipesSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: AdminMenuTexts;
}
interface Props {
  params: { locale: Locale; id: string };
}
export default async function UserRecipesAdminPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts, authUser] = await Promise.all([
    getUserRecipesAdminPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);
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
      authUser={authUser}
      sortingOptions={recipesOptions}
      {...texts}
      path={`/recipes/trainer/filteredWithCount/${id}`}
    />
  );
}
