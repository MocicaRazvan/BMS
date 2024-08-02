import { Locale } from "@/navigation";
import RecipeTable, {
  RecipeTableTexts,
} from "@/components/table/recipes-table";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { unstable_setRequestLocale } from "next-intl/server";
import { getAdminRecipesPageTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import { sortingRecipesSortingOptionsKeys } from "@/texts/components/list";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { AdminMenuTexts } from "@/components/admin/menu-list";

interface Props {
  params: { locale: Locale };
}

export interface AdminRecipesPageTexts {
  recipeTableTexts: RecipeTableTexts;
  sortingRecipesSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: AdminMenuTexts;
}

export default async function AdminRecipesPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [
    {
      title,
      themeSwitchTexts,
      recipeTableTexts,
      sortingRecipesSortingOptions,
      header,
      menuTexts,
    },
    authUser,
  ] = await Promise.all([
    getAdminRecipesPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);

  const recipesOptions = getSortingOptions(
    sortingRecipesSortingOptionsKeys,
    sortingRecipesSortingOptions,
  );
  return (
    <AdminContentLayout
      navbarProps={{
        title,
        themeSwitchTexts,
        authUser,
        menuTexts,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading title={title} header={header} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full">
            <RecipeTable
              path={`/recipes/filteredWithCount`}
              forWhom="trainer"
              sortingOptions={recipesOptions}
              {...recipeTableTexts}
              authUser={authUser}
              sizeOptions={[10, 20, 30, 40]}
            />
          </div>
        </Suspense>
      </div>
    </AdminContentLayout>
  );
}
