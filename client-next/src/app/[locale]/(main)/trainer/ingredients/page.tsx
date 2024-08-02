import { unstable_setRequestLocale } from "next-intl/server";
import { Locale } from "@/navigation";
import IngredientsTable, {
  IngredientTableTexts,
} from "@/components/table/ingredients-table";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { getIngredientsPageTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import { sortingIngredientsSortingOptionsKeys } from "@/texts/components/list";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";

interface Props {
  params: { locale: Locale };
}

export interface IngredientsPageTexts {
  ingredientTableTexts: IngredientTableTexts;
  sortingIngredientsSortingOptions: SortingOptionsTexts;
  title: string;
  header: string;
}

export default async function IngredientsPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [ingredientsPageTexts, authUser] = await Promise.all([
    getIngredientsPageTexts(),
    getUserWithMinRole("ROLE_TRAINER"),
  ]);

  const ingredientOptions = getSortingOptions(
    sortingIngredientsSortingOptionsKeys,
    ingredientsPageTexts.sortingIngredientsSortingOptions,
  );
  // todo single ingredient page si legare la table la nav, si receipie service cu valid ids client la ingredient, dupa aia cu count iara alt client custom la recipie, fa  general
  return (
    <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 max-w-[1350px] mx-auto mb-5 ">
      <Heading {...ingredientsPageTexts} />
      <Suspense fallback={<LoadingSpinner />}>
        <div className="h-full w-full pb-5">
          <IngredientsTable
            path={"/ingredients/filtered"}
            sortingOptions={ingredientOptions}
            forWhom={"trainer"}
            authUser={authUser}
            {...ingredientsPageTexts.ingredientTableTexts}
            sizeOptions={[10, 15, 20, 50]}
          />
        </div>
      </Suspense>
    </div>
  );
}
