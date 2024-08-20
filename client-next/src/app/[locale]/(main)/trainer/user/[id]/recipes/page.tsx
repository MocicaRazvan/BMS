import RecipeTable, {
  RecipeTableTexts,
} from "@/components/table/recipes-table";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserRecipesPageContentTexts } from "@/texts/pages";
import { getTheSameUserOrAdmin } from "@/lib/user";
import { sortingRecipesSortingOptionsKeys } from "@/texts/components/list";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

export interface UserRecipesPageTexts {
  recipesTableTexts: RecipeTableTexts;
  sortingRecipesSortingOptions: SortingOptionsTexts;
  title: string;
  header: string;
}
interface Props {
  params: { locale: Locale; id: string };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.TrainerRecipes",
      "/trainer/user/" + id + "/recipes",
      locale,
    )),
  };
}

export default async function UsersRecipesPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [userRecipesPageTexts, authUser] = await Promise.all([
    getUserRecipesPageContentTexts(),
    getTheSameUserOrAdmin(id),
  ]);
  const recipesOptions = getSortingOptions(
    sortingRecipesSortingOptionsKeys,
    userRecipesPageTexts.sortingRecipesSortingOptions,
  );

  console.log("AUTH", authUser);
  return (
    <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 max-w-[1250px] mx-auto ">
      <Heading {...userRecipesPageTexts} />

      <Suspense fallback={<LoadingSpinner />}>
        <div className="">
          <RecipeTable
            path={`/recipes/trainer/filteredWithCount/${id}`}
            forWhom="trainer"
            sortingOptions={recipesOptions}
            {...userRecipesPageTexts.recipesTableTexts}
            authUser={authUser}
            sizeOptions={[10, 20, 30, 40]}
          />
        </div>
      </Suspense>
    </div>
  );
}
