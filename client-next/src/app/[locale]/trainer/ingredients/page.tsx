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
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";

interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.Ingredients",
      "/trainer/ingredients",
      locale,
    )),
  };
}
export interface IngredientsPageTexts {
  ingredientTableTexts: IngredientTableTexts;
  sortingIngredientsSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
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
  return (
    <SidebarContentLayout
      navbarProps={{
        ...ingredientsPageTexts,
        authUser,
        mappingKey: "trainer",
      }}
    >
      <div className="w-full h-full bg-background ">
        <Heading {...ingredientsPageTexts} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="h-full w-full mt-10">
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
    </SidebarContentLayout>
  );
}
