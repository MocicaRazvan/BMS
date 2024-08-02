import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getThemeSwitchTexts } from "@/texts/components/nav";
import { getUserWithMinRole } from "@/lib/user";
import AdminContentLayout from "@/components/admin/admin-content-layout";
import AdminIngredientsCreatePageContent from "@/app/[locale]/admin/ingredients/create/page-content";
import { IngredientFormTexts } from "@/components/forms/ingredient-form";
import { getAdminIngredientsCreatePageTexts } from "@/texts/pages";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { AdminMenuTexts } from "@/components/admin/menu-list";

export interface AdminIngredientsCreatePageTexts {
  ingredientForm: IngredientFormTexts;
  title: string;
  menuTexts: AdminMenuTexts;
}
interface Props {
  params: { locale: Locale };
}

export default async function AdminIngredientsCreatePage({
  params: { locale },
}: Props) {
  unstable_setRequestLocale(locale);

  const [themeSwitchTexts, authUser, texts] = await Promise.all([
    getThemeSwitchTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
    getAdminIngredientsCreatePageTexts(),
  ]);
  unstable_setRequestLocale(locale);
  return (
    <AdminContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts,
        authUser,
        menuTexts: texts.menuTexts,
      }}
    >
      <Suspense fallback={<LoadingSpinner />}>
        <AdminIngredientsCreatePageContent
          authUser={authUser}
          texts={texts.ingredientForm}
        />
      </Suspense>
    </AdminContentLayout>
  );
}
