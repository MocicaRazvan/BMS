import RecipeTable, {
  RecipeTableTexts,
} from "@/components/table/recipes-table";
import { getSortingOptions, SortingOptionsTexts } from "@/types/constants";
import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUserRecipesPageContentTexts } from "@/texts/pages";
import { sortingRecipesSortingOptionsKeys } from "@/texts/components/list";
import Heading from "@/components/common/heading";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import IsTheSameUserOrAdmin from "@/app/[locale]/trainer/user/is-the-same-user-or-admin";
import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

export interface UserRecipesPageTexts {
  recipesTableTexts: RecipeTableTexts;
  sortingRecipesSortingOptions: SortingOptionsTexts;
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}
interface Props {
  params: { locale: Locale; id: string };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.TrainerRecipes",
      "/trainer/user/" + id + "/recipes",
      locale,
    )),
  };
}

export default async function UsersRecipesPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [userRecipesPageTexts] = await Promise.all([
    getUserRecipesPageContentTexts(),
  ]);

  const recipesOptions = getSortingOptions(
    sortingRecipesSortingOptionsKeys,
    userRecipesPageTexts.sortingRecipesSortingOptions,
  );

  return (
    <IsTheSameUserOrAdmin id={id}>
      <SidebarContentLayout
        navbarProps={{
          title: userRecipesPageTexts.title,
          themeSwitchTexts: userRecipesPageTexts.themeSwitchTexts,
          menuTexts: userRecipesPageTexts.menuTexts,
          mappingKey: "trainer",
          findInSiteTexts: userRecipesPageTexts.findInSiteTexts,
          locale,
        }}
      >
        <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 mx-auto ">
          <Heading {...userRecipesPageTexts} />
          <div className="">
            <RecipeTable
              path={`/recipes/trainer/filteredWithCount/${id}`}
              forWhom="trainer"
              sortingOptions={recipesOptions}
              {...userRecipesPageTexts.recipesTableTexts}
              sizeOptions={[10, 20, 30, 40]}
            />
          </div>
        </div>
      </SidebarContentLayout>
    </IsTheSameUserOrAdmin>
  );
}
