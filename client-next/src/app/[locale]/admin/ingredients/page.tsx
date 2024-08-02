import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getDataTableTexts } from "@/texts/components/table";
import { getUserWithMinRole } from "@/lib/user";
import IngredientForm, {
  IngredientFormTexts,
} from "@/components/forms/ingredient-form";
import {
  getSortingItemSortingOptions,
  sortingIngredientsSortingOptionsKeys,
} from "@/texts/components/list";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { getButtonSubmitTexts } from "@/texts/components/forms";
import IngredientsTable, {
  IngredientTableTexts,
} from "@/components/table/ingredients-table";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getAdminIngredientsPageTexts } from "@/texts/pages";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import AdminIngredientsCreatePageContent from "@/app/[locale]/admin/ingredients/page-content";
import { AdminMenuTexts } from "@/components/admin/menu-list";

interface Props {
  params: { locale: Locale };
}

export interface AdminIngredientsPageTexts {
  ingredientTableTexts: IngredientTableTexts;
  sortingIngredientsSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: AdminMenuTexts;
}

export default async function AdminIngredientsPage({
  params: { locale },
}: Props) {
  unstable_setRequestLocale(locale);
  const [
    {
      title,
      themeSwitchTexts,
      ingredientTableTexts,
      sortingIngredientsSortingOptions,
      header,
      menuTexts,
    },
    authUser,
  ] = await Promise.all([
    getAdminIngredientsPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);

  const ingredientOptions = getSortingOptions(
    sortingIngredientsSortingOptionsKeys,
    sortingIngredientsSortingOptions,
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
      <div className="w-full h-full bg-background ">
        <Heading title={title} header={header} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full w-full">
            <AdminIngredientsCreatePageContent
              path={"/ingredients/filtered"}
              {...ingredientTableTexts}
              sortingOptions={ingredientOptions}
              authUser={authUser}
              sizeOptions={[10, 15, 20, 50]}
              forWhom={"admin"}
            />
          </div>
        </Suspense>
      </div>
    </AdminContentLayout>
  );
}
