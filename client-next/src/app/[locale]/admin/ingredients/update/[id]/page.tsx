import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import LoadingSpinner from "@/components/common/loading-spinner";
import { getThemeSwitchTexts, ThemeSwitchTexts } from "@/texts/components/nav";
import { getUserWithMinRole } from "@/lib/user";
import {
  getAdminIngredientsCreatePageTexts,
  getAdminPageUpdateIngredientTexts,
} from "@/texts/pages";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import { Suspense } from "react";
import { IngredientFormTexts } from "@/components/forms/ingredient-form";
import AdminIngredientsPageContent from "@/app/[locale]/admin/ingredients/update/[id]/page-content";
import { AdminMenuTexts } from "@/components/admin/menu-list";

interface Props {
  params: {
    id: string;
    locale: Locale;
  };
}

export interface AdminPageUpdateIngredientTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  ingredientFormTexts: IngredientFormTexts;
  title: string;
  menuTexts: AdminMenuTexts;
}

export default async function AdminPageUpdateIngredient({
  params: { id, locale },
}: Props) {
  unstable_setRequestLocale(locale);
  const [authUser, texts] = await Promise.all([
    getUserWithMinRole("ROLE_ADMIN"),
    getAdminPageUpdateIngredientTexts(),
  ]);
  return (
    <AdminContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,
        authUser,
        menuTexts: texts.menuTexts,
      }}
    >
      <Suspense fallback={<LoadingSpinner />}>
        <AdminIngredientsPageContent
          id={id}
          authUser={authUser}
          ingredientFormTexts={texts.ingredientFormTexts}
        />
      </Suspense>
    </AdminContentLayout>
  );
}
