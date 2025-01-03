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
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import ArchiveQueueCards, {
  ArchiveQueueCardsTexts,
} from "@/components/common/archive-queue-card";

interface Props {
  params: { locale: Locale };
}

export interface AdminRecipesPageTexts {
  recipeTableTexts: RecipeTableTexts;
  sortingRecipesSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
  archiveRecipesTexts: ArchiveQueueCardsTexts;
  archiveMealsTexts: ArchiveQueueCardsTexts;
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("admin.Recipes", "/admin/recipes", locale);
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
      archiveRecipesTexts,
      archiveMealsTexts,
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
    <SidebarContentLayout
      navbarProps={{
        title,
        themeSwitchTexts,
        authUser,
        menuTexts,
        mappingKey: "admin",
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading title={title} header={header} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full space-y-10">
            <RecipeTable
              path={`/recipes/filteredWithCount`}
              forWhom="admin"
              sortingOptions={recipesOptions}
              {...recipeTableTexts}
              authUser={authUser}
              sizeOptions={[10, 20, 30, 40]}
              mainDashboard={true}
              extraQueryParams={{
                admin: "true",
              }}
            />
            <div className="space-y-5">
              <ArchiveQueueCards
                prefix={"recipe"}
                locale={locale}
                showHeader={true}
                {...archiveRecipesTexts}
                authUser={authUser}
              />
              <ArchiveQueueCards
                prefix={"meal"}
                locale={locale}
                showHeader={false}
                {...archiveMealsTexts}
                authUser={authUser}
              />
            </div>
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
