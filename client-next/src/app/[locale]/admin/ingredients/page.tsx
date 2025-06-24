import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { sortingIngredientsSortingOptionsKeys } from "@/texts/components/list";
import { getSortingOptions, SortingOptionsTexts } from "@/types/constants";
import { IngredientTableTexts } from "@/components/table/ingredients-table";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getAdminIngredientsPageTexts } from "@/texts/pages";
import Heading from "@/components/common/heading";
import AdminIngredientsCreatePageContent from "@/app/[locale]/admin/ingredients/page-content";
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

export interface AdminIngredientsPageTexts {
  ingredientTableTexts: IngredientTableTexts;
  sortingIngredientsSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
  archiveIngredientsTexts: ArchiveQueueCardsTexts;
  findInSiteTexts: FindInSiteTexts;
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.Ingredients",
    "/admin/ingredients",
    locale,
  );
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
      archiveIngredientsTexts,
      findInSiteTexts,
    },
  ] = await Promise.all([getAdminIngredientsPageTexts()]);
  const ingredientOptions = getSortingOptions(
    sortingIngredientsSortingOptionsKeys,
    sortingIngredientsSortingOptions,
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
      <div className="w-full h-full bg-background ">
        <Heading title={title} header={header} />
        <div className="mt-10 h-full w-full space-y-10">
          <AdminIngredientsCreatePageContent
            path={"/ingredients/filtered"}
            {...ingredientTableTexts}
            sortingOptions={ingredientOptions}
            sizeOptions={[10, 15, 20, 50]}
            forWhom={"admin"}
            extraQueryParams={{
              admin: "true",
            }}
          />
          <Separator />
          <ArchiveQueueCards
            prefix={"ingredient"}
            locale={locale}
            showHeader={true}
            {...archiveIngredientsTexts}
          />
        </div>
      </div>
    </SidebarContentLayout>
  );
}
