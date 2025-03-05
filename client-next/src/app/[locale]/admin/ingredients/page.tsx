import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserWithMinRole } from "@/lib/user";
import { sortingIngredientsSortingOptionsKeys } from "@/texts/components/list";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { IngredientTableTexts } from "@/components/table/ingredients-table";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getAdminIngredientsPageTexts } from "@/texts/pages";
import Heading from "@/components/common/heading";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import AdminIngredientsCreatePageContent from "@/app/[locale]/admin/ingredients/page-content";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata, getMetadataValues } from "@/texts/metadata";
import ArchiveQueueCards, {
  ArchiveQueueCardsTexts,
} from "@/components/common/archive-queue-card";
import { Separator } from "@/components/ui/separator";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

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
    authUser,
  ] = await Promise.all([
    getAdminIngredientsPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);
  const metadataValues = await getMetadataValues(authUser, locale);
  const ingredientOptions = getSortingOptions(
    sortingIngredientsSortingOptionsKeys,
    sortingIngredientsSortingOptions,
  );

  return (
    <SidebarContentLayout
      navbarProps={{
        title,
        themeSwitchTexts,
        authUser,
        menuTexts,
        mappingKey: "admin",
        findInSiteTexts,
        metadataValues,
      }}
    >
      <div className="w-full h-full bg-background ">
        <Heading title={title} header={header} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full w-full space-y-10">
            <AdminIngredientsCreatePageContent
              path={"/ingredients/filtered"}
              {...ingredientTableTexts}
              sortingOptions={ingredientOptions}
              authUser={authUser}
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
              authUser={authUser}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
