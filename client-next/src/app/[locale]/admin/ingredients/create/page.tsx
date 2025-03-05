import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getThemeSwitchTexts } from "@/texts/components/nav";
import { getUserWithMinRole } from "@/lib/user";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import AdminIngredientsCreatePageContent from "@/app/[locale]/admin/ingredients/create/page-content";
import { IngredientFormTexts } from "@/components/forms/ingredient-form";
import { getAdminIngredientsCreatePageTexts } from "@/texts/pages";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata, getMetadataValues } from "@/texts/metadata";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

export interface AdminIngredientsCreatePageTexts {
  ingredientForm: IngredientFormTexts;
  title: string;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}
interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.CreateIngredient",
    "/admin/ingredients/create",
    locale,
  );
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
  const metadataValues = await getMetadataValues(authUser, locale);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts,
        authUser,
        menuTexts: texts.menuTexts,
        mappingKey: "admin",
        findInSiteTexts: texts.findInSiteTexts,
        metadataValues,
      }}
    >
      <Suspense fallback={<LoadingSpinner />}>
        <AdminIngredientsCreatePageContent
          authUser={authUser}
          texts={texts.ingredientForm}
        />
      </Suspense>
    </SidebarContentLayout>
  );
}
