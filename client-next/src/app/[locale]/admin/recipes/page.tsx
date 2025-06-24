import { Locale } from "@/navigation/navigation";
import RecipeTable, {
  RecipeTableTexts,
} from "@/components/table/recipes-table";
import { getSortingOptions, SortingOptionsTexts } from "@/types/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { unstable_setRequestLocale } from "next-intl/server";
import { getAdminRecipesPageTexts } from "@/texts/pages";
import { sortingRecipesSortingOptionsKeys } from "@/texts/components/list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import ArchiveQueueCards, {
  ArchiveQueueCardsTexts,
} from "@/components/archive/archive-queue-card";
import { Separator } from "@/components/ui/separator";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

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
  findInSiteTexts: FindInSiteTexts;
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
      findInSiteTexts,
    },
  ] = await Promise.all([getAdminRecipesPageTexts()]);

  const recipesOptions = getSortingOptions(
    sortingRecipesSortingOptionsKeys,
    sortingRecipesSortingOptions,
  );
  return (
    <SidebarContentLayout
      navbarProps={{
        title,
        themeSwitchTexts,
        menuTexts,
        mappingKey: "admin",
        findInSiteTexts,
        locale,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading title={title} header={header} />
        <div className="mt-10 h-full space-y-10">
          <RecipeTable
            path={`/recipes/filteredWithCount`}
            forWhom="admin"
            sortingOptions={recipesOptions}
            {...recipeTableTexts}
            sizeOptions={[10, 20, 30, 40]}
            mainDashboard={true}
            extraQueryParams={{
              admin: "true",
            }}
          />
          <Separator />
          <div className="space-y-5">
            <ArchiveQueueCards
              prefix={"recipe"}
              locale={locale}
              showHeader={true}
              {...archiveRecipesTexts}
            />
            <ArchiveQueueCards
              prefix={"meal"}
              locale={locale}
              showHeader={false}
              {...archiveMealsTexts}
            />
          </div>
        </div>
      </div>
    </SidebarContentLayout>
  );
}
