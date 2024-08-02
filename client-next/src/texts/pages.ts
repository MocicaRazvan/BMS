import {
  getElementHeaderTexts,
  getNutritionalTableTexts,
} from "@/texts/components/common";
import { getTranslations } from "next-intl/server";
import { SinglePostPageTexts } from "@/app/[locale]/(main)/(user)/posts/single/[id]/page-content";
import { getPostCommentsTexts } from "@/texts/components/posts";
import { ApprovedPostsTexts } from "@/app/[locale]/(main)/(user)/posts/approved/page-content";
import {
  getGridListTexts,
  getSortingItemSortingOptions,
  getUseApprovedFilterTexts,
  getUseBinaryTexts,
  getUseFilterDropdownTexts,
  getUseTagsExtraCriteriaTexts,
  sortingIngredientsSortingOptionsKeys,
  sortingOrdersSortingOptionsKeys,
  sortingPlansSortingOptionsKeys,
  sortingPostsSortingOptionsKeys,
  sortingRecipesSortingOptionsKeys,
  sortingUsersSortingOptionsKeys,
} from "@/texts/components/list";
import {
  getAdminEmailTexts,
  getCheckoutDrawerTexts,
  getIngredientFormTexts,
  getUpdateProfileTexts,
} from "@/texts/components/forms";
import { UserPostsPageContentTexts } from "@/app/[locale]/(main)/trainer/user/[id]/posts/page-content";
import {
  getIngredientTableColumnTexts,
  getIngredientTableTexts,
  getOrderTableTexts,
  getPlanTableTexts,
  getPostTableTexts,
  getRecipeTableTexts,
  getUserTableTexts,
} from "@/texts/components/table";
import { AdminPostsPageTexts } from "@/app/[locale]/admin/posts/page";
import { getThemeSwitchTexts } from "@/texts/components/nav";
import { UserPostsAdminPageTexts } from "@/app/[locale]/admin/users/[id]/posts/page";
import { AdminUsersPageTexts } from "@/app/[locale]/admin/users/page";
import { UserPageTexts } from "@/app/[locale]/(main)/(user)/users/single/[id]/page-content";
import { AdminIngredientsCreatePageTexts } from "@/app/[locale]/admin/ingredients/create/page";
import { AdminIngredientsPageTexts } from "@/app/[locale]/admin/ingredients/page";
import { AdminPageUpdateIngredientTexts } from "@/app/[locale]/admin/ingredients/update/[id]/page";
import { IngredientsPageTexts } from "@/app/[locale]/(main)/trainer/ingredients/page";
import { SingleIngredientPageTexts } from "@/app/[locale]/(main)/trainer/ingredients/single/[id]/page-content";
import {
  getAllRelativeItemTexts,
  getDailySalesTexts,
  getGeographyChartTexts,
  getIngredientPieChartTexts,
  getMonthlySalesTexts,
  getRelativeItemsSummaryTexts,
  getTotalAmountCountOrdersTexts,
} from "@/texts/components/charts";
import { UserRecipesPageTexts } from "@/app/[locale]/(main)/trainer/user/[id]/recipes/page";
import { AdminRecipesPageTexts } from "@/app/[locale]/admin/recipes/page";
import { UserRecipesAdminPageTexts } from "@/app/[locale]/admin/users/[id]/recipes/page";
import { SingleRecipePageTexts } from "@/app/[locale]/(main)/trainer/recipes/single/[id]/page-content";
import { AdminPlansPageTexts } from "@/app/[locale]/admin/plans/page";
import { UserPlansPageTexts } from "@/app/[locale]/(main)/trainer/user/[id]/plans/page";
import { UserPlansAdminPageTexts } from "@/app/[locale]/admin/users/[id]/plans/page";
import { SingleTrainerPlanPageTexts } from "@/app/[locale]/(main)/trainer/plans/single/[id]/page-content";
import { ApprovedPlansTexts } from "@/app/[locale]/(main)/(user)/plans/approved/page-content";
import { dietTypes } from "@/types/forms";
import { getAddToCartBtnTexts } from "@/texts/components/plans";
import { UserPlanPageContentTexts } from "@/app/[locale]/(main)/(user)/plans/single/[id]/page-content";
import { CartPageContentTexts } from "@/app/[locale]/(main)/(user)/cart/page-content";
import { SubscriptionsPageContentTexts } from "@/app/[locale]/(main)/(user)/subscriptions/page-content";
import { SingleSubscriptionTexts } from "@/app/[locale]/(main)/(user)/subscriptions/single/[id]/page-content";
import { AdminOrdersPageTexts } from "@/app/[locale]/admin/orders/page";
import { UserOrdersAdminPageTexts } from "@/app/[locale]/admin/users/[id]/orders/page";
import { UserOrdersPageTexts } from "@/app/[locale]/(main)/(user)/orders/page";
import { SingleOrderPageContentTexts } from "@/app/[locale]/(main)/(user)/orders/single/[id]/page-content";
import { AdminDashboardPageTexts } from "@/app/[locale]/admin/dashboard/page-content";
import { AdminMonthlySalesTexts } from "@/app/[locale]/admin/monthlySales/page";
import { getDateRangePickerTexts } from "@/texts/components/ui";
import { AdminCountriesTexts } from "@/app/[locale]/admin/countries/page";
import { AdminDailySalesTexts } from "@/app/[locale]/admin/dailySales/page";
import { UserMonthlySalesPageTexts } from "@/app/[locale]/(main)/trainer/user/[id]/plans/monthlySales/page";
import { UserDailySalesPageTexts } from "@/app/[locale]/(main)/trainer/user/[id]/plans/dailySales/page";
import { getAdminMenuTexts } from "@/texts/components/admin";
import { UserAdminMonthlySalesPageTexts } from "@/app/[locale]/admin/users/[id]/monthlySales/page";
import { UserAdminDailySalesPageTexts } from "@/app/[locale]/admin/users/[id]/dailySales/page";
import { OrderCompletePageTexts } from "@/app/[locale]/(main)/(user)/orderComplete/page";
import { HomeTexts } from "@/app/[locale]/(main)/page";
import {
  getHomeAboutTexts,
  getHomeCardsText,
  getHomeHeaderTexts,
  getHomeTimelineTexts,
} from "@/texts/components/home";
import { AdminEmailPageTexts } from "@/app/[locale]/admin/email/page";
import { AdminKanbanTexts } from "@/app/[locale]/admin/kanban/page";
import { getKanbanBoardTexts } from "@/texts/components/kanban";
import { KanbanPageTexts } from "@/app/[locale]/(main)/(user)/kanban/page";

export async function getSinglePostPageTexts(): Promise<SinglePostPageTexts> {
  const [elementHeaderTexts, t, postCommentsTexts] = await Promise.all([
    getElementHeaderTexts(),
    getTranslations("pages.posts.SinglePageTexts"),
    getPostCommentsTexts(),
  ]);
  return {
    elementHeaderTexts,
    updateButton: t("updateButton"),
    postCommentsTexts,
  };
}

export async function getApprovedPostsPageTexts(): Promise<ApprovedPostsTexts> {
  const [t, gridListTexts, tagsCriteriaTexts, sortingPostsSortingOptions] =
    await Promise.all([
      getTranslations("pages.posts.ApprovedPostsTexts"),
      getGridListTexts(),
      getUseTagsExtraCriteriaTexts(),
      getSortingItemSortingOptions("posts", sortingPostsSortingOptionsKeys),
    ]);

  return {
    gridListTexts,
    title: t("title"),
    header: t("header"),
    sortingPostsSortingOptions,
    tagsCriteriaTexts,
  };
}

export async function getUserPostsPageContentTexts(): Promise<UserPostsPageContentTexts> {
  const [postTableTexts, sortingPostsSortingOptions, t] = await Promise.all([
    getPostTableTexts(),
    getSortingItemSortingOptions("posts", sortingPostsSortingOptionsKeys),
    getTranslations("pages.posts.UserPostsPageContentTexts"),
  ]);
  return {
    postTableTexts,
    sortingPostsSortingOptions,
    header: t("header"),
    title: t("title"),
  };
}

export async function getAdminPostsPageTexts(): Promise<AdminPostsPageTexts> {
  const [
    t,
    postTableTexts,
    sortingPostsSortingOptions,
    themeSwitchTexts,
    menuTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminPostsPageTexts"),
    getPostTableTexts(),
    getSortingItemSortingOptions("posts", sortingPostsSortingOptionsKeys),
    getThemeSwitchTexts(),
    getAdminMenuTexts(),
  ]);

  return {
    postTableTexts,
    title: t("title"),
    header: t("header"),
    sortingPostsSortingOptions,
    themeSwitchTexts,
    menuTexts,
  };
}
export async function getAdminRecipesPageTexts(): Promise<AdminRecipesPageTexts> {
  const [
    t,
    recipeTableTexts,
    sortingRecipesSortingOptions,
    themeSwitchTexts,
    menuTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminRecipesPageTexts"),
    getRecipeTableTexts(),
    getSortingItemSortingOptions("recipes", sortingRecipesSortingOptionsKeys),
    getThemeSwitchTexts(),
    getAdminMenuTexts(),
  ]);

  return {
    recipeTableTexts,
    title: t("title"),
    header: t("header"),
    sortingRecipesSortingOptions,
    themeSwitchTexts,
    menuTexts,
  };
}

export async function getUserPostsAdminPageTexts(): Promise<UserPostsAdminPageTexts> {
  const [
    t,
    postTableTexts,
    sortingPostsSortingOptions,
    themeSwitchTexts,
    menuTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.UserPostsAdminPageTexts"),
    getPostTableTexts(),
    getSortingItemSortingOptions("posts", sortingPostsSortingOptionsKeys),
    getThemeSwitchTexts(),
    getAdminMenuTexts(),
  ]);

  return {
    postTableTexts,
    title: t("title"),
    header: t("header"),
    sortingPostsSortingOptions,
    themeSwitchTexts,
    menuTexts,
  };
}

export async function getAdminUsersPageTexts(): Promise<AdminUsersPageTexts> {
  const [
    userTableTexts,
    themeSwitchTexts,
    userSortingOptionsTexts,
    t,
    menuTexts,
  ] = await Promise.all([
    getUserTableTexts(),
    getThemeSwitchTexts(),
    getSortingItemSortingOptions("users", sortingUsersSortingOptionsKeys),
    getTranslations("pages.admin.AdminUsersPageTexts"),
    getAdminMenuTexts(),
  ]);
  return {
    userTableTexts,
    themeSwitchTexts,
    userSortingOptionsTexts,
    header: t("header"),
    title: t("title"),
    menuTexts,
  };
}

export const getUserPageTexts: () => Promise<UserPageTexts> = async () => {
  const [updateProfileTexts, t] = await Promise.all([
    getUpdateProfileTexts(),
    getTranslations("pages.users.UserPageTexts"),
  ]);
  return {
    updateProfileTexts,
    changePassword: t("changePassword"),
    editProfile: t("editProfile"),
    firstNameLabel: t("firstNameLabel"),
    lastNameLabel: t("lastNameLabel"),
    ownerTitle: t("ownerTitle"),
    startChat: t("startChat"),
    verifyEmail: t("verifyEmail"),
    visitorTitle: t("visitorTitle"),
    errorText: t("errorText"),
    emailSent: t("emailSent"),
  };
};

export async function getAdminIngredientsCreatePageTexts(): Promise<AdminIngredientsCreatePageTexts> {
  const [ingredientForm, t, menuTexts] = await Promise.all([
    getIngredientFormTexts("create"),
    getTranslations("pages.admin.AdminIngredientsCreatePage"),
    getAdminMenuTexts(),
  ]);

  return {
    ingredientForm,
    title: t("title"),
    menuTexts,
  };
}

export async function getAdminPageUpdateIngredientTexts(): Promise<AdminPageUpdateIngredientTexts> {
  const [themeSwitchTexts, ingredientFormTexts, t, menuTexts] =
    await Promise.all([
      getThemeSwitchTexts(),
      getIngredientFormTexts("update"),
      getTranslations("pages.admin.AdminPageUpdateIngredientTexts"),
      getAdminMenuTexts(),
    ]);
  return {
    themeSwitchTexts,
    ingredientFormTexts,
    title: t("title"),
    menuTexts,
  };
}

export async function getAdminIngredientsPageTexts(): Promise<AdminIngredientsPageTexts> {
  const [
    ingredientTableTexts,
    sortingIngredientsSortingOptions,
    themeSwitchTexts,
    t,
    menuTexts,
  ] = await Promise.all([
    getIngredientTableTexts(),
    getSortingItemSortingOptions(
      "ingredients",
      sortingIngredientsSortingOptionsKeys,
    ),
    getThemeSwitchTexts(),
    getTranslations("pages.admin.AdminIngredientsPageTexts"),
    getAdminMenuTexts(),
  ]);

  return {
    ingredientTableTexts,
    sortingIngredientsSortingOptions,
    themeSwitchTexts,
    title: t("title"),
    header: t("header"),
    menuTexts,
  };
}
export async function getIngredientsPageTexts(): Promise<IngredientsPageTexts> {
  const [ingredientTableTexts, sortingIngredientsSortingOptions, t] =
    await Promise.all([
      getIngredientTableTexts(),
      getSortingItemSortingOptions(
        "ingredients",
        sortingIngredientsSortingOptionsKeys,
      ),
      getTranslations("pages.ingredients.IngredientsPageTexts"),
    ]);

  return {
    ingredientTableTexts,
    sortingIngredientsSortingOptions,
    title: t("title"),
    header: t("header"),
  };
}

export async function getSingleIngredientPageTexts(): Promise<SingleIngredientPageTexts> {
  const [
    ingredientColumnTexts,
    ingredientPieChartTexts,
    nutritionalTableTexts,
  ] = await Promise.all([
    getIngredientTableColumnTexts(),
    getIngredientPieChartTexts(),
    getNutritionalTableTexts(),
  ]);

  return {
    ingredientColumnTexts,
    ingredientPieChartTexts,
    nutritionalTableTexts,
  };
}
export async function getUserRecipesPageContentTexts(): Promise<UserRecipesPageTexts> {
  const [recipesTableTexts, sortingRecipesSortingOptions, t] =
    await Promise.all([
      getRecipeTableTexts(),
      getSortingItemSortingOptions("recipes", sortingRecipesSortingOptionsKeys),
      getTranslations("pages.recipes.UserRecipesPageContentTexts"),
    ]);
  return {
    recipesTableTexts,
    sortingRecipesSortingOptions,
    header: t("header"),
    title: t("title"),
  };
}
// getTranslations("pages.admin.UserRecipesAdminPageTexts"),
export async function getUserRecipesAdminPageTexts(): Promise<UserRecipesAdminPageTexts> {
  const [
    recipesTableTexts,
    sortingRecipesSortingOptions,
    themeSwitchTexts,
    t,
    menuTexts,
  ] = await Promise.all([
    getRecipeTableTexts(),
    getSortingItemSortingOptions("recipes", sortingRecipesSortingOptionsKeys),
    getThemeSwitchTexts(),
    getTranslations("pages.admin.UserRecipesAdminPageTexts"),
    getAdminMenuTexts(),
  ]);
  return {
    recipesTableTexts,
    sortingRecipesSortingOptions,
    themeSwitchTexts,
    header: t("header"),
    title: t("title"),
    menuTexts,
  };
}

export async function getUserPlansAdminPageTexts(): Promise<UserPlansAdminPageTexts> {
  const [
    plansTableTexts,
    sortingPlansSortingOptions,
    themeSwitchTexts,
    t,
    menuTexts,
  ] = await Promise.all([
    getPlanTableTexts(),
    getSortingItemSortingOptions("plans", sortingPlansSortingOptionsKeys),
    getThemeSwitchTexts(),
    getTranslations("pages.admin.UserPlansAdminPageTexts"),
    getAdminMenuTexts(),
  ]);
  return {
    plansTableTexts,
    sortingPlansSortingOptions,
    themeSwitchTexts,
    title: t("title"),
    header: t("header"),
    menuTexts,
  };
}
export async function getUserOrdersAdminPageTexts(): Promise<UserOrdersAdminPageTexts> {
  const [
    orderTableTexts,
    sortingOrdersSortingOptions,
    themeSwitchTexts,
    t,
    menuTexts,
  ] = await Promise.all([
    getOrderTableTexts(),
    getSortingItemSortingOptions("orders", sortingOrdersSortingOptionsKeys),
    getThemeSwitchTexts(),
    getTranslations("pages.admin.UserOrdersAdminPageTexts"),
    getAdminMenuTexts(),
  ]);
  return {
    orderTableTexts,
    sortingOrdersSortingOptions,
    themeSwitchTexts,
    title: t("title"),
    header: t("header"),
    menuTexts,
  };
}
export async function getSingleRecipePageTexts(): Promise<SingleRecipePageTexts> {
  const [
    elementHeaderTexts,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    t,
  ] = await Promise.all([
    getElementHeaderTexts(),
    getNutritionalTableTexts(),
    getIngredientPieChartTexts(),
    getTranslations("pages.recipes.SingleRecipePageTexts"),
  ]);
  return {
    elementHeaderTexts,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    showIngredients: t("showIngredients"),
  };
}

export async function getAdminPlansPageTexts(): Promise<AdminPlansPageTexts> {
  const [
    t,
    planTableTexts,
    sortingPlansSortingOptions,
    themeSwitchTexts,
    menuTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminPlansPageTexts"),
    getPlanTableTexts(),
    getSortingItemSortingOptions("plans", sortingPlansSortingOptionsKeys),
    getThemeSwitchTexts(),
    getAdminMenuTexts(),
  ]);

  return {
    planTableTexts,
    title: t("title"),
    header: t("header"),
    sortingPlansSortingOptions,
    themeSwitchTexts,
    menuTexts,
  };
}

export async function getUserPlansPageTexts(): Promise<UserPlansPageTexts> {
  const [planTableTexts, sortingPlansSortingOptions, t] = await Promise.all([
    getPlanTableTexts(),
    getSortingItemSortingOptions("plans", sortingPlansSortingOptionsKeys),
    getTranslations("pages.plans.UserPlansPageTexts"),
  ]);
  return {
    planTableTexts,
    sortingPlansSortingOptions,
    header: t("header"),
    title: t("title"),
  };
}
export async function getSingleTrainerPlanPageTexts(): Promise<SingleTrainerPlanPageTexts> {
  const [
    elementHeaderTexts,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    t,
  ] = await Promise.all([
    getElementHeaderTexts(),
    getNutritionalTableTexts(),
    getIngredientPieChartTexts(),
    getTranslations("pages.plans.SingleTrainerPlanPageTexts"),
  ]);

  return {
    elementHeaderTexts,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    displayed: t("displayed"),
    notDisplayed: t("notDisplayed"),
    price: t("price"),
  };
}

export async function getApprovedPlansTexts(): Promise<ApprovedPlansTexts> {
  const [
    gridListTexts,
    dietDropdownTexts,
    useApprovedFilterTexts,
    displayFilterTexts,
    sortingPlansSortingOptions,
    addToCartBtnTexts,
    t,
  ] = await Promise.all([
    getGridListTexts(),
    getUseFilterDropdownTexts(
      "UseDietDropdownTexts",
      dietTypes as unknown as string[],
    ),
    getUseApprovedFilterTexts(),
    getUseBinaryTexts("UseDisplayFilterTexts"),
    getSortingItemSortingOptions("plans", sortingPlansSortingOptionsKeys),
    getAddToCartBtnTexts(),
    getTranslations("pages.plans.ApprovedPlansTexts"),
  ]);
  return {
    gridListTexts,
    dietDropdownTexts,
    useApprovedFilterTexts,
    displayFilterTexts,
    title: t("title"),
    header: t("header"),
    buyableLabel: t("buyableLabel"),
    sortingPlansSortingOptions,
    addToCartBtnTexts,
  };
}

export async function getUserPlanPageContentTexts(): Promise<UserPlanPageContentTexts> {
  const [elementHeaderTexts, addToCartBtnTexts, t] = await Promise.all([
    getElementHeaderTexts(),
    getAddToCartBtnTexts(),
    getTranslations("pages.plans.UserPlanPageContentTexts"),
  ]);
  return {
    elementHeaderTexts,
    addToCartBtnTexts,
    price: t("price"),
  };
}

export async function getCartPageContentTexts(): Promise<CartPageContentTexts> {
  const [checkoutDrawerTexts, t] = await Promise.all([
    getCheckoutDrawerTexts(),
    getTranslations("pages.cart.CartPageContentTexts"),
  ]);
  return {
    title: t("title"),
    clearAll: t("clearAll"),
    emptyCart: t("emptyCart"),
    toastRemoveAllDescription: t("toastRemoveAllDescription"),
    toastRemoveAllTitle: t("toastRemoveAllTitle"),
    toastRemovedDescription: t("toastRemovedDescription"),
    toastRemovedTitle: t("toastRemovedTitle"),
    toastUndo: t("toastUndo"),
    seeThePlans: t("seeThePlans"),
    subtotal: t("subtotal"),
    checkoutDrawerTexts,
  };
}

export async function getSubscriptionsPageContentTexts(): Promise<SubscriptionsPageContentTexts> {
  const [gridListTexts, sortingPlansSortingOptions, dietDropdownTexts, t] =
    await Promise.all([
      getGridListTexts(),
      getSortingItemSortingOptions("plans", sortingPlansSortingOptionsKeys),
      getUseFilterDropdownTexts(
        "UseDietDropdownTexts",
        dietTypes as unknown as string[],
      ),
      getTranslations("pages.subscriptions.SubscriptionsPageContentTexts"),
    ]);
  return {
    gridListTexts,
    sortingPlansSortingOptions,
    dietDropdownTexts,
    title: t("title"),
    header: t("header"),
  };
}

export async function getSingleSubscriptionTexts(): Promise<SingleSubscriptionTexts> {
  const [elementHeaderTexts, nutritionalTableTexts, ingredientPieChartTexts] =
    await Promise.all([
      getElementHeaderTexts(),
      getNutritionalTableTexts(),
      getIngredientPieChartTexts(),
    ]);

  return { elementHeaderTexts, nutritionalTableTexts, ingredientPieChartTexts };
}

export async function getAdminOrdersPageTexts(): Promise<AdminOrdersPageTexts> {
  const [
    t,
    orderTableTexts,
    sortingOrdersSortingOptions,
    themeSwitchTexts,
    menuTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminOrdersPageTexts"),
    getOrderTableTexts(),
    getSortingItemSortingOptions("orders", sortingOrdersSortingOptionsKeys),
    getThemeSwitchTexts(),
    getAdminMenuTexts(),
  ]);

  return {
    orderTableTexts,
    title: t("title"),
    header: t("header"),
    sortingOrdersSortingOptions,
    themeSwitchTexts,
    menuTexts,
  };
}

export async function getUserOrdersPageTexts(): Promise<UserOrdersPageTexts> {
  const [orderTableTexts, sortingOrdersSortingOptions, t] = await Promise.all([
    getOrderTableTexts(),
    getSortingItemSortingOptions("orders", sortingOrdersSortingOptionsKeys),
    getTranslations("pages.orders.UserOrdersPageTexts"),
  ]);
  return {
    orderTableTexts,
    sortingOrdersSortingOptions,
    header: t("header"),
    title: t("title"),
  };
}
export async function getSingleOrderPageContentTexts(): Promise<SingleOrderPageContentTexts> {
  const t = await getTranslations("pages.orders.SingleOrderPageContentTexts");
  return {
    title: t("title"),
    seeInvoice: t("seeInvoice"),
    total: t("total"),
  };
}

export async function getAdminDashboardPageTexts(): Promise<AdminDashboardPageTexts> {
  const [
    t,
    relativeItemTexts,
    themeSwitchTexts,
    relativeItemsSummaryTexts,
    menuTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminDashboardPageTexts"),
    getAllRelativeItemTexts(),
    getThemeSwitchTexts(),
    getRelativeItemsSummaryTexts(),
    getAdminMenuTexts(),
  ]);

  return {
    title: t("title"),
    header: t("header"),
    relativeItemTexts,
    themeSwitchTexts,
    relativeItemsSummaryTexts,
    menuTexts,
  };
}
export async function getAdminMonthlySalesTexts(): Promise<AdminMonthlySalesTexts> {
  const [t, monthlySalesTexts, themeSwitchTexts, menuTexts] = await Promise.all(
    [
      getTranslations("pages.admin.AdminMonthlySalesTexts"),
      getMonthlySalesTexts("orders"),
      getThemeSwitchTexts(),
      getAdminMenuTexts(),
    ],
  );
  return {
    title: t("title"),
    header: t("header"),
    monthlySalesTexts,
    themeSwitchTexts,
    menuTexts,
  };
}
export async function getAdminCountriesTexts(): Promise<AdminCountriesTexts> {
  const [t, themeSwitchTexts, geographyChartTexts, menuTexts] =
    await Promise.all([
      getTranslations("pages.admin.AdminCountriesTexts"),
      getThemeSwitchTexts(),
      getGeographyChartTexts(),
      getAdminMenuTexts(),
    ]);
  return {
    title: t("title"),
    header: t("header"),
    themeSwitchTexts,
    geographyChartTexts,
    menuTexts,
  };
}

export async function getAdminDailySalesTexts(): Promise<AdminDailySalesTexts> {
  const [t, dailySalesTexts, themeSwitchTexts, menuTexts] = await Promise.all([
    getTranslations("pages.admin.AdminDailySalesTexts"),
    getDailySalesTexts("orders"),
    getThemeSwitchTexts(),
    getAdminMenuTexts(),
  ]);
  return {
    title: t("title"),
    header: t("header"),
    dailySalesTexts,
    themeSwitchTexts,
    menuTexts,
  };
}
export async function getUserMonthlySalesPageTexts(): Promise<UserMonthlySalesPageTexts> {
  const [t, monthlySalesTexts] = await Promise.all([
    getTranslations("pages.plans.UserMonthlySalesPageTexts"),
    getMonthlySalesTexts("plans"),
  ]);
  return {
    title: t("title"),
    header: t("header"),
    monthlySalesTexts,
  };
}

export async function getUserDailySalesPageTexts(): Promise<UserDailySalesPageTexts> {
  const [t, dailySalesTexts] = await Promise.all([
    getTranslations("pages.plans.UserDailySalesPageTexts"),
    getDailySalesTexts("plans"),
  ]);
  return {
    title: t("title"),
    header: t("header"),
    dailySalesTexts,
  };
}

export async function getUserAdminMonthlySalesPageTexts(): Promise<UserAdminMonthlySalesPageTexts> {
  const [t, monthlySalesTexts, menuTexts, themeSwitchTexts] = await Promise.all(
    [
      getTranslations("pages.admin.UserAdminMonthlySalesPageTexts"),
      getMonthlySalesTexts("plans"),
      getAdminMenuTexts(),
      getThemeSwitchTexts(),
    ],
  );
  return {
    title: t("title"),
    header: t("header"),
    monthlySalesTexts,
    menuTexts,
    themeSwitchTexts,
  };
}

export async function getUserAdminDailySalesPageTexts(): Promise<UserAdminDailySalesPageTexts> {
  const [t, dailySalesTexts, menuTexts, themeSwitchTexts] = await Promise.all([
    getTranslations("pages.admin.UserAdminDailySalesPageTexts"),
    getDailySalesTexts("plans"),
    getAdminMenuTexts(),
    getThemeSwitchTexts(),
  ]);
  return {
    title: t("title"),
    header: t("header"),
    dailySalesTexts,
    menuTexts,
    themeSwitchTexts,
  };
}

export async function getOrderCompletePageTexts(): Promise<OrderCompletePageTexts> {
  const t = await getTranslations("pages.orders.OrderCompletePageTexts");

  return {
    ordersBtn: t("ordersBtn"),
    plansBtn: t("plansBtn"),
    title: t("title"),
  };
}

export async function getHomeTexts(): Promise<HomeTexts> {
  const [homeHeaderTexts, homeCardsTexts, homeAboutTexts, homeTimelineTexts] =
    await Promise.all([
      getHomeHeaderTexts(),
      getHomeCardsText(),
      getHomeAboutTexts(),
      getHomeTimelineTexts(),
    ]);
  return {
    homeHeaderTexts,
    homeCardsTexts,
    homeAboutTexts,
    homeTimelineTexts,
  };
}

export async function getAdminEmailPageTexts(): Promise<AdminEmailPageTexts> {
  const [adminEmail, themeSwitchTexts, menuTexts, t] = await Promise.all([
    getAdminEmailTexts(),
    getThemeSwitchTexts(),
    getAdminMenuTexts(),
    getTranslations("pages.admin.AdminEmailPageTexts"),
  ]);

  return {
    adminEmail,
    themeSwitchTexts,
    title: t("title"),
    header: t("header"),
    menuTexts,
  };
}

export async function getAdminKanbanTexts(): Promise<AdminKanbanTexts> {
  const [t, themeSwitchTexts, menuTexts, kanbanBoardTexts] = await Promise.all([
    getTranslations("pages.admin.AdminKanbanTexts"),
    getThemeSwitchTexts(),
    getAdminMenuTexts(),
    getKanbanBoardTexts(),
  ]);
  return {
    title: t("title"),
    header: t("header"),
    themeSwitchTexts,
    menuTexts,
    kanbanBoardTexts,
  };
}
export async function getKanbanPageTexts(): Promise<KanbanPageTexts> {
  const [t, kanbanBoardTexts] = await Promise.all([
    getTranslations("pages.kanban.KanbanPageTexts"),
    getKanbanBoardTexts(),
  ]);
  return {
    title: t("title"),
    kanbanBoardTexts,
    header: t("header"),
  };
}
