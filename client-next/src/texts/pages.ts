import {
  getAllArchiveQueueCardsTexts,
  getArchiveQueueCardsTexts,
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
  sortingDaysSortingOptionsKeys,
  sortingIngredientsSortingOptionsKeys,
  sortingOrdersSortingOptionsKeys,
  sortingPlansSortingOptionsKeys,
  sortingPostsSortingOptionsKeys,
  sortingRecipesSortingOptionsKeys,
  sortingUsersSortingOptionsKeys,
} from "@/texts/components/list";
import {
  getAddDayToCalendarTexts,
  getAdminEmailTexts,
  getAnswerFromBodyFormTexts,
  getButtonSubmitTexts,
  getCheckoutDrawerTexts,
  getDayFromTexts,
  getIngredientFormTexts,
  getPlanFormTexts,
  getPostFormTexts,
  getRecipeFormTexts,
  getUpdateProfileTexts,
} from "@/texts/components/forms";
import { UserPostsPageContentTexts } from "@/app/[locale]/trainer/user/[id]/posts/page-content";
import {
  getArchiveQueuesTableTexts,
  getDayTableTexts,
  getIngredientTableColumnTexts,
  getIngredientTableTexts,
  getOrderTableTexts,
  getPlanTableTexts,
  getPostTableTexts,
  getRecipeTableTexts,
  getUserTableTexts,
} from "@/texts/components/table";
import { AdminPostsPageTexts } from "@/app/[locale]/admin/posts/page";
import {
  getFindInSiteTexts,
  getThemeSwitchTexts,
} from "@/texts/components/nav";
import { UserPostsAdminPageTexts } from "@/app/[locale]/admin/users/[id]/posts/page";
import { AdminUsersPageTexts } from "@/app/[locale]/admin/users/page";
import { UserPageTexts } from "@/app/[locale]/(main)/(user)/users/single/[id]/page-content";
import { AdminIngredientsCreatePageTexts } from "@/app/[locale]/admin/ingredients/create/page";
import { AdminIngredientsPageTexts } from "@/app/[locale]/admin/ingredients/page";
import { AdminPageUpdateIngredientTexts } from "@/app/[locale]/admin/ingredients/update/[id]/page";
import { IngredientsPageTexts } from "@/app/[locale]/trainer/ingredients/page";
import { SingleIngredientPageTexts } from "@/app/[locale]/trainer/ingredients/single/[id]/page-content";
import {
  getAllRelativeItemTexts,
  getDailySalesTexts,
  getGeographyChartTexts,
  getIngredientPieChartTexts,
  getMonthlySalesTexts,
  getRelativeItemsSummaryTexts,
  getTopPlansTexts,
  getTopTrainersTexts,
  getTopUsersTexts,
  getTopViewedPostsTexts,
} from "@/texts/components/charts";
import { UserRecipesPageTexts } from "@/app/[locale]/trainer/user/[id]/recipes/page";
import { AdminRecipesPageTexts } from "@/app/[locale]/admin/recipes/page";
import { UserRecipesAdminPageTexts } from "@/app/[locale]/admin/users/[id]/recipes/page";
import { SingleRecipePageTexts } from "@/app/[locale]/trainer/recipes/single/[id]/page-content";
import { AdminPlansPageTexts } from "@/app/[locale]/admin/plans/page";
import { UserPlansPageTexts } from "@/app/[locale]/trainer/user/[id]/plans/page";
import { UserPlansAdminPageTexts } from "@/app/[locale]/admin/users/[id]/plans/page";
import { SingleTrainerPlanPageTexts } from "@/app/[locale]/trainer/plans/single/[id]/page-content";
import { ApprovedPlansTexts } from "@/app/[locale]/(main)/(user)/plans/approved/page-content";
import {
  dietTypes,
  getActivitiesTexts,
  getAdminAICreatePostSchemaTexts,
  getCalculatorSchemaTexts,
  getGenderTexts,
} from "@/types/forms";
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
import { AdminCountriesTexts } from "@/app/[locale]/admin/countries/page";
import { AdminDailySalesTexts } from "@/app/[locale]/admin/dailySales/page";
import { UserMonthlySalesPageTexts } from "@/app/[locale]/trainer/user/[id]/plans/monthlySales/page";
import { UserDailySalesPageTexts } from "@/app/[locale]/trainer/user/[id]/plans/dailySales/page";
import { UserAdminMonthlySalesPageTexts } from "@/app/[locale]/admin/users/[id]/monthlySales/page";
import { UserAdminDailySalesPageTexts } from "@/app/[locale]/admin/users/[id]/dailySales/page";
import { OrderCompletePageTexts } from "@/app/[locale]/(main)/(user)/orderComplete/page";
import { HomeTexts } from "@/app/[locale]/(main)/page";
import {
  getHomeAboutTexts,
  getHomeCardsText,
  getHomeHeaderTexts,
  getHomeHeroTexts,
  getHomeMapTexts,
  getHomeTestimonialsTexts,
  getHomeTimelineTexts,
} from "@/texts/components/home";
import { AdminEmailPageTexts } from "@/app/[locale]/admin/email/page";
import { AdminKanbanTexts } from "@/app/[locale]/admin/kanban/page";
import { getKanbanBoardTexts } from "@/texts/components/kanban";
import { KanbanPageTexts } from "@/app/[locale]/(main)/(user)/kanban/page";
import {
  CalculatorPageTexts,
  IntakeTitle,
  ItemTexts,
} from "@/app/[locale]/(main)/calculator/page";
import { UserDaysPageTexts } from "@/app/[locale]/trainer/user/[id]/days/page";
import { getDaysListTexts, getSingleDayTexts } from "@/texts/components/days";
import { planObjectives } from "@/types/dto";
import { getSidebarMenuTexts } from "@/texts/components/sidebar";
import { AdminUserPageTexts } from "@/app/[locale]/admin/users/[id]/page";
import { AdminPostPageTexts } from "@/app/[locale]/admin/posts/single/[id]/page";
import { AdminRecipePageTexts } from "@/app/[locale]/admin/recipes/single/[id]/page";
import { AdminPlanPageTexts } from "@/app/[locale]/admin/plans/single/[id]/page";
import { AdminIngredientPageTexts } from "@/app/[locale]/admin/ingredients/single/[id]/page";
import { AdminOrderPageTexts } from "@/app/[locale]/admin/orders/single/[id]/page";
import {
  adminGroupLabels,
  adminLabels,
  adminSubLabels,
  trainerGroupLabels,
  trainerLabels,
  trainerSubLabels,
} from "@/components/sidebar/menu-list";
import { UserPostsPageTexts } from "@/app/[locale]/trainer/user/[id]/posts/page";
import { CreatePostPageTexts } from "@/app/[locale]/trainer/posts/create/page";
import { UpdatePostPageTexts } from "@/app/[locale]/trainer/posts/update/[id]/page";
import { TrainerSingleIngredientPageTexts } from "@/app/[locale]/trainer/ingredients/single/[id]/page";
import { TrainerPostPageTexts } from "@/app/[locale]/trainer/posts/single/[id]/page";
import { CreateRecipePageTexts } from "@/app/[locale]/trainer/recipes/create/page";
import { UpdateRecipePageTexts } from "@/app/[locale]/trainer/recipes/update/[id]/page";
import { TrainerSingleRecipePageTexts } from "@/app/[locale]/trainer/recipes/single/[id]/page";
import { CreateDayPageTexts } from "@/app/[locale]/trainer/days/create/page";
import { UpdateDayPageTexts } from "@/app/[locale]/trainer/days/update/[id]/page";
import { SingleDayTrainerPageTexts } from "@/app/[locale]/trainer/days/single/[id]/page";
import { CreatePlanPageTexts } from "@/app/[locale]/trainer/plans/create/page";
import { TrainerPlanPageTexts } from "@/app/[locale]/trainer/plans/single/[id]/page";
import { TrainerKanbanPageTexts } from "@/app/[locale]/trainer/kanban/page";
import { TermsOfServiceTexts } from "@/app/[locale]/(main)/termsOfService/page";
import { UserDuplicatePostPage } from "@/app/[locale]/trainer/posts/duplicate/[id]/page";
import { DuplicateRecipePageTexts } from "@/app/[locale]/trainer/recipes/duplicate/[id]/page";
import { AdminPageDuplicateIngredientTexts } from "@/app/[locale]/admin/ingredients/duplicate/[id]/page";
import { AdminAIPostsCreateTexts } from "@/app/[locale]/admin/posts/aiCreate/page";
import {
  getPlanRecommendationListTexts,
  getPostRecommendationListTexts,
} from "@/texts/components/recommandation";
import { ChatPageTexts } from "@/app/[locale]/(main)/(user)/chat/page-content";
import { getChatRoomsTexts } from "@/texts/components/chat";
import { getDaysCalendarCTATexts } from "@/texts/components/day-calendar";
import { AdminArchiveQueuesPageTexts } from "@/app/[locale]/admin/archiveQueues/page";

export async function getSinglePostPageTexts(): Promise<SinglePostPageTexts> {
  const [
    elementHeaderTexts,
    t,
    postCommentsTexts,
    postRecommendationListTexts,
    answerFromBodyFormTexts,
  ] = await Promise.all([
    getElementHeaderTexts(),
    getTranslations("pages.posts.SinglePageTexts"),
    getPostCommentsTexts(),
    getPostRecommendationListTexts(),
    getAnswerFromBodyFormTexts(),
  ]);
  return {
    elementHeaderTexts,
    updateButton: t("updateButton"),
    numberOfReads: t("numberOfReads"),
    postCommentsTexts,
    postRecommendationListTexts,
    answerFromBodyFormTexts,
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
    likedLabel: t("likedLabel"),
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

export async function getUserPostsPageTexts(): Promise<UserPostsPageTexts> {
  const [
    userPostsPageContentTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
    topViewedPostsTexts,
  ] = await Promise.all([
    getUserPostsPageContentTexts(),
    getThemeSwitchTexts(),
    getSidebarMenuTexts(
      "trainer",
      trainerGroupLabels,
      trainerLabels,
      trainerSubLabels,
    ),
    getFindInSiteTexts(),
    getTopViewedPostsTexts(),
  ]);
  return {
    userPostsPageContentTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
    topViewedPostsTexts,
  };
}

export async function getCreatePostPageTexts(): Promise<CreatePostPageTexts> {
  const [postFormTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getPostFormTexts("create"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    postFormTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}

export async function getCreatePlanPageTexts(): Promise<CreatePlanPageTexts> {
  const [planFormTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getPlanFormTexts("create"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    planFormTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}

export async function getCreateDayPageTexts(): Promise<CreateDayPageTexts> {
  const [dayFormTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getDayFromTexts("create"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    dayFormTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}

export async function getSingleRecipePageTexts(): Promise<SingleRecipePageTexts> {
  const [
    elementHeaderTexts,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    answerFromBodyFormTexts,
    t,
  ] = await Promise.all([
    getElementHeaderTexts(),
    getNutritionalTableTexts(),
    getIngredientPieChartTexts(),
    getAnswerFromBodyFormTexts(),
    getTranslations("pages.recipes.SingleRecipePageTexts"),
  ]);
  return {
    elementHeaderTexts,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    answerFromBodyFormTexts,
    showIngredients: t("showIngredients"),
  };
}

export async function getCreateRecipePageTexts(): Promise<CreateRecipePageTexts> {
  const [recipeFormTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getRecipeFormTexts("create"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    recipeFormTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}

export async function getUpdatePostPageTexts(): Promise<UpdatePostPageTexts> {
  const [postFormTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getPostFormTexts("update"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    postFormTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}

export async function getUserDuplicatePostPage(): Promise<UserDuplicatePostPage> {
  const [postFormTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getPostFormTexts("create"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    postFormTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}

export async function getUpdatePlanPageTexts() {
  const [planFormTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getPlanFormTexts("update"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    planFormTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}
export async function getDuplicatePlanPageTexts() {
  const [planFormTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getPlanFormTexts("create"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    planFormTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}

export async function getUpdateDayPageTexts(): Promise<UpdateDayPageTexts> {
  const [dayFormTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getDayFromTexts("update"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    dayFormTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}

export async function getUpdateRecipePageTexts(): Promise<UpdateRecipePageTexts> {
  const [recipeFormTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getRecipeFormTexts("update"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    recipeFormTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}
export async function getDuplicateRecipePageTexts(): Promise<DuplicateRecipePageTexts> {
  const [recipeFormTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getRecipeFormTexts("create"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    recipeFormTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}

export async function getTrainerSingleRecipePageTexts(): Promise<TrainerSingleRecipePageTexts> {
  const [
    singleRecipePageTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
    t,
  ] = await Promise.all([
    getSingleRecipePageTexts(),
    getThemeSwitchTexts(),
    getSidebarMenuTexts(
      "trainer",
      trainerGroupLabels,
      trainerLabels,
      trainerSubLabels,
    ),
    getFindInSiteTexts(),
    getTranslations("pages.trainer.TrainerSingleRecipePageTexts"),
  ]);
  return {
    singleRecipePageTexts,
    themeSwitchTexts,
    menuTexts,
    title: t("title"),
    findInSiteTexts,
  };
}

export async function getAdminPostsPageTexts(): Promise<AdminPostsPageTexts> {
  const [
    t,
    postTableTexts,
    sortingPostsSortingOptions,
    themeSwitchTexts,
    menuTexts,
    archivePostsTexts,
    archiveCommentsTexts,
    findInSiteTexts,
    topViewedPostsTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminPostsPageTexts"),
    getPostTableTexts(),
    getSortingItemSortingOptions("posts", sortingPostsSortingOptionsKeys),
    getThemeSwitchTexts(),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getArchiveQueueCardsTexts("post"),
    getArchiveQueueCardsTexts("comment"),
    getFindInSiteTexts(),
    getTopViewedPostsTexts(),
  ]);

  return {
    postTableTexts,
    title: t("title"),
    header: t("header"),
    sortingPostsSortingOptions,
    themeSwitchTexts,
    menuTexts,
    archivePostsTexts,
    archiveCommentsTexts,
    findInSiteTexts,
    topViewedPostsTexts,
  };
}

export async function getAdminRecipesPageTexts(): Promise<AdminRecipesPageTexts> {
  const [
    t,
    recipeTableTexts,
    sortingRecipesSortingOptions,
    themeSwitchTexts,
    menuTexts,
    archiveRecipesTexts,
    archiveMealsTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminRecipesPageTexts"),
    getRecipeTableTexts(),
    getSortingItemSortingOptions("recipes", sortingRecipesSortingOptionsKeys),
    getThemeSwitchTexts(),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getArchiveQueueCardsTexts("recipe"),
    getArchiveQueueCardsTexts("meal"),
    getFindInSiteTexts(),
  ]);

  return {
    recipeTableTexts,
    title: t("title"),
    header: t("header"),
    sortingRecipesSortingOptions,
    themeSwitchTexts,
    menuTexts,
    archiveRecipesTexts,
    archiveMealsTexts,
    findInSiteTexts,
  };
}

export async function getUserPostsAdminPageTexts(): Promise<UserPostsAdminPageTexts> {
  const [
    t,
    postTableTexts,
    sortingPostsSortingOptions,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.UserPostsAdminPageTexts"),
    getPostTableTexts(),
    getSortingItemSortingOptions("posts", sortingPostsSortingOptionsKeys),
    getThemeSwitchTexts(),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getFindInSiteTexts(),
  ]);

  return {
    postTableTexts,
    title: t("title"),
    header: t("header"),
    sortingPostsSortingOptions,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}

export async function getAdminUsersPageTexts(): Promise<AdminUsersPageTexts> {
  const [
    userTableTexts,
    themeSwitchTexts,
    userSortingOptionsTexts,
    t,
    menuTexts,
    archiveUsersTexts,
    topUsersTexts,
    topTrainersTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getUserTableTexts(),
    getThemeSwitchTexts(),
    getSortingItemSortingOptions("users", sortingUsersSortingOptionsKeys),
    getTranslations("pages.admin.AdminUsersPageTexts"),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getArchiveQueueCardsTexts("user"),
    getTopUsersTexts(),
    getTopTrainersTexts(),
    getFindInSiteTexts(),
  ]);
  return {
    userTableTexts,
    themeSwitchTexts,
    userSortingOptionsTexts,
    header: t("header"),
    title: t("title"),
    menuTexts,
    archiveUsersTexts,
    topUsersTexts,
    topTrainersTexts,
    findInSiteTexts,
  };
}

export async function getAdminUserPageTexts(): Promise<AdminUserPageTexts> {
  const [userPageTexts, themeSwitchTexts, t, menuTexts, findInSiteTexts] =
    await Promise.all([
      getUserPageTexts(),
      getThemeSwitchTexts(),
      getTranslations("pages.admin.AdminUserPageTexts"),
      getSidebarMenuTexts(
        "admin",
        adminGroupLabels,
        adminLabels,
        adminSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    userPageTexts,
    themeSwitchTexts,
    header: t("header"),
    title: t("title"),
    menuTexts,
    findInSiteTexts,
  };
}

export async function getAdminPostPageTexts(): Promise<AdminPostPageTexts> {
  const [t, themeSwitchTexts, menuTexts, singlePostPageTexts, findInSiteTexts] =
    await Promise.all([
      getTranslations("pages.admin.AdminPostPageTexts"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "admin",
        adminGroupLabels,
        adminLabels,
        adminSubLabels,
      ),
      getSinglePostPageTexts(),
      getFindInSiteTexts(),
    ]);
  return {
    themeSwitchTexts,
    header: t("header"),
    title: t("title"),
    menuTexts,
    singlePostPageTexts,
    findInSiteTexts,
  };
}

export async function getTrainerPostPageTexts(): Promise<TrainerPostPageTexts> {
  const [themeSwitchTexts, t, menuTexts, singlePostPageTexts, findInSiteTexts] =
    await Promise.all([
      getThemeSwitchTexts(),
      getTranslations("pages.trainer.TrainerPostPageTexts"),
      getSidebarMenuTexts(
        "admin",
        adminGroupLabels,
        adminLabels,
        adminSubLabels,
      ),
      getSinglePostPageTexts(),
      getFindInSiteTexts(),
    ]);
  return {
    themeSwitchTexts,
    title: t("title"),
    menuTexts,
    singlePostPageTexts,
    findInSiteTexts,
  };
}

export async function getTrainerPlanPageTexts(): Promise<TrainerPlanPageTexts> {
  const [
    singleTrainerPlanPageTexts,
    t,
    menuTexts,
    themeSwitchTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getSingleTrainerPlanPageTexts(),
    getTranslations("pages.trainer.TrainerPlanPageTexts"),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getThemeSwitchTexts(),
    getFindInSiteTexts(),
  ]);
  return {
    singleTrainerPlanPageTexts,
    title: t("title"),
    menuTexts,
    themeSwitchTexts,
    findInSiteTexts,
  };
}

export async function getSingleDayTrainerPageTexts(): Promise<SingleDayTrainerPageTexts> {
  const [singleDayTexts, t, menuTexts, themeSwitchTexts, findInSiteTexts] =
    await Promise.all([
      getSingleDayTexts(),
      getTranslations("pages.trainer.SingleDayTrainerPageTexts"),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getThemeSwitchTexts(),
      getFindInSiteTexts(),
    ]);
  return {
    singleDayTexts,
    title: t("title"),
    menuTexts,
    themeSwitchTexts,
    findInSiteTexts,
  };
}

export async function getAdminIngredientPageTexts(): Promise<AdminIngredientPageTexts> {
  const [
    themeSwitchTexts,
    t,
    menuTexts,
    singleIngredientPageTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getThemeSwitchTexts(),
    getTranslations("pages.admin.AdminIngredientPageTexts"),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getSingleIngredientPageTexts(),
    getFindInSiteTexts(),
  ]);
  return {
    themeSwitchTexts,
    header: t("header"),
    title: t("title"),
    menuTexts,
    singleIngredientPageTexts,
    findInSiteTexts,
  };
}

export async function getTrainerSingleIngredientPageTexts(): Promise<TrainerSingleIngredientPageTexts> {
  const [
    themeSwitchTexts,
    t,
    menuTexts,
    singleIngredientPageTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getThemeSwitchTexts(),
    getTranslations("pages.trainer.TrainerSingleIngredientPageTexts"),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getSingleIngredientPageTexts(),
    getFindInSiteTexts(),
  ]);
  return {
    themeSwitchTexts,
    title: t("title"),
    menuTexts,
    singleIngredientPageTexts,
    findInSiteTexts,
  };
}

export async function getAdminPlanPageTexts(): Promise<AdminPlanPageTexts> {
  const [
    t,
    themeSwitchTexts,
    menuTexts,
    singleTrainerPlanPageTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminPlanPageTexts"),
    getThemeSwitchTexts(),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getSingleTrainerPlanPageTexts(),
    getFindInSiteTexts(),
  ]);
  return {
    themeSwitchTexts,
    header: t("header"),
    title: t("title"),
    menuTexts,
    singleTrainerPlanPageTexts,
    findInSiteTexts,
  };
}

export async function getAdminRecipePageTexts(): Promise<AdminRecipePageTexts> {
  const [
    themeSwitchTexts,
    t,
    menuTexts,
    singleRecipePageTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getThemeSwitchTexts(),
    getTranslations("pages.admin.AdminRecipePageTexts"),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getSingleRecipePageTexts(),
    getFindInSiteTexts(),
  ]);
  return {
    themeSwitchTexts,
    header: t("header"),
    title: t("title"),
    menuTexts,
    singleRecipePageTexts,
    findInSiteTexts,
  };
}

export async function getAdminOrderPageTexts(): Promise<AdminOrderPageTexts> {
  const [
    themeSwitchTexts,
    t,
    menuTexts,
    singleOrderPageContentTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getThemeSwitchTexts(),
    getTranslations("pages.admin.AdminOrderPageTexts"),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getSingleOrderPageContentTexts(),
    getFindInSiteTexts(),
  ]);
  return {
    themeSwitchTexts,
    header: t("header"),
    title: t("title"),
    menuTexts,
    singleOrderPageContentTexts,
    findInSiteTexts,
  };
}

export const getUserPageTexts: () => Promise<UserPageTexts> = async () => {
  const [updateProfileTexts, dayCalendarCTATexts, t] = await Promise.all([
    getUpdateProfileTexts(),
    getDaysCalendarCTATexts(),
    getTranslations("pages.users.UserPageTexts"),
  ]);
  return {
    updateProfileTexts,
    dayCalendarCTATexts,
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
    toastSuccess: t("toastSuccess"),
  };
};

export async function getAdminIngredientsCreatePageTexts(): Promise<AdminIngredientsCreatePageTexts> {
  const [ingredientForm, t, menuTexts, findInSiteTexts] = await Promise.all([
    getIngredientFormTexts("create"),
    getTranslations("pages.admin.AdminIngredientsCreatePage"),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getFindInSiteTexts(),
  ]);

  return {
    ingredientForm,
    title: t("title"),
    menuTexts,
    findInSiteTexts,
  };
}
export async function getAdminAIPostsCreate(): Promise<AdminAIPostsCreateTexts> {
  const [t, menuTexts, schemaTexts, buttonSubmitTexts, findInSiteTexts] =
    await Promise.all([
      getTranslations("pages.admin.AdminAIPostsCreatePage"),
      getSidebarMenuTexts(
        "admin",
        adminGroupLabels,
        adminLabels,
        adminSubLabels,
      ),
      getAdminAICreatePostSchemaTexts(),
      getButtonSubmitTexts(),
      getFindInSiteTexts(),
    ]);

  return {
    title: t("title"),
    menuTexts,
    schemaTexts,
    buttonSubmitTexts,
    createdPosts: t("createdPosts"),
    descriptionDescription: t("descriptionDescription"),
    descriptionLabel: t("descriptionLabel"),
    descriptionPlaceholder: t("descriptionPlaceholder"),
    englishError: t("englishError"),
    error: t("error"),
    negativePromptDescription: t("negativePromptDescription"),
    negativePromptLabel: t("negativePromptLabel"),
    negativePromptPlaceholder: t("negativePromptPlaceholder"),
    promptDescription: t("promptDescription"),
    promptLabel: t("promptLabel"),
    promptPlaceholder: t("promptPlaceholder"),
    toxicError: t("toxicError"),
    numberOfPostsLabel: t("numberOfPostsLabel"),
    uploadingState: {
      description: t("uploadingState.description"),
      title: t("uploadingState.title"),
      images: t("uploadingState.images"),
      submitted: t("uploadingState.submitted"),
    },
    singlePostLabel: t("singlePostLabel"),
    findInSiteTexts,
  };
}

export async function getAdminPageUpdateIngredientTexts(): Promise<AdminPageUpdateIngredientTexts> {
  const [themeSwitchTexts, ingredientFormTexts, t, menuTexts, findInSiteTexts] =
    await Promise.all([
      getThemeSwitchTexts(),
      getIngredientFormTexts("update"),
      getTranslations("pages.admin.AdminPageUpdateIngredientTexts"),
      getSidebarMenuTexts(
        "admin",
        adminGroupLabels,
        adminLabels,
        adminSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    themeSwitchTexts,
    ingredientFormTexts,
    title: t("title"),
    menuTexts,
    findInSiteTexts,
  };
}
export async function getAdminPageDuplicateIngredientTexts(): Promise<AdminPageDuplicateIngredientTexts> {
  const [themeSwitchTexts, ingredientFormTexts, t, menuTexts, findInSiteTexts] =
    await Promise.all([
      getThemeSwitchTexts(),
      getIngredientFormTexts("create"),
      getTranslations("pages.admin.AdminPageUpdateIngredientTexts"),
      getSidebarMenuTexts(
        "admin",
        adminGroupLabels,
        adminLabels,
        adminSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    themeSwitchTexts,
    ingredientFormTexts,
    title: t("title"),
    menuTexts,
    findInSiteTexts,
  };
}

export async function getAdminIngredientsPageTexts(): Promise<AdminIngredientsPageTexts> {
  const [
    ingredientTableTexts,
    sortingIngredientsSortingOptions,
    themeSwitchTexts,
    t,
    menuTexts,
    archiveIngredientsTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getIngredientTableTexts(),
    getSortingItemSortingOptions(
      "ingredients",
      sortingIngredientsSortingOptionsKeys,
    ),
    getThemeSwitchTexts(),
    getTranslations("pages.admin.AdminIngredientsPageTexts"),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getArchiveQueueCardsTexts("ingredient"),
    getFindInSiteTexts(),
  ]);

  return {
    ingredientTableTexts,
    sortingIngredientsSortingOptions,
    themeSwitchTexts,
    title: t("title"),
    header: t("header"),
    menuTexts,
    archiveIngredientsTexts,
    findInSiteTexts,
  };
}

export async function getIngredientsPageTexts(): Promise<IngredientsPageTexts> {
  const [
    ingredientTableTexts,
    sortingIngredientsSortingOptions,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
    t,
  ] = await Promise.all([
    getIngredientTableTexts(),
    getSortingItemSortingOptions(
      "ingredients",
      sortingIngredientsSortingOptionsKeys,
    ),
    getThemeSwitchTexts(),
    getSidebarMenuTexts(
      "trainer",
      trainerGroupLabels,
      trainerLabels,
      trainerSubLabels,
    ),
    getFindInSiteTexts(),
    getTranslations("pages.ingredients.IngredientsPageTexts"),
  ]);

  return {
    ingredientTableTexts,
    sortingIngredientsSortingOptions,
    themeSwitchTexts,
    menuTexts,
    title: t("title"),
    header: t("header"),
    findInSiteTexts,
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
  const [
    recipesTableTexts,
    sortingRecipesSortingOptions,
    t,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getRecipeTableTexts(),
    getSortingItemSortingOptions("recipes", sortingRecipesSortingOptionsKeys),
    getTranslations("pages.recipes.UserRecipesPageContentTexts"),
    getThemeSwitchTexts(),
    getSidebarMenuTexts(
      "trainer",
      trainerGroupLabels,
      trainerLabels,
      trainerSubLabels,
    ),
    getFindInSiteTexts(),
  ]);
  return {
    recipesTableTexts,
    sortingRecipesSortingOptions,
    header: t("header"),
    title: t("title"),
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
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
    findInSiteTexts,
  ] = await Promise.all([
    getRecipeTableTexts(),
    getSortingItemSortingOptions("recipes", sortingRecipesSortingOptionsKeys),
    getThemeSwitchTexts(),
    getTranslations("pages.admin.UserRecipesAdminPageTexts"),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getFindInSiteTexts(),
  ]);
  return {
    recipesTableTexts,
    sortingRecipesSortingOptions,
    themeSwitchTexts,
    header: t("header"),
    title: t("title"),
    menuTexts,
    findInSiteTexts,
  };
}

export async function getUserPlansAdminPageTexts(): Promise<UserPlansAdminPageTexts> {
  const [
    plansTableTexts,
    sortingPlansSortingOptions,
    themeSwitchTexts,
    t,
    menuTexts,
    topPlansTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getPlanTableTexts(),
    getSortingItemSortingOptions("plans", sortingPlansSortingOptionsKeys),
    getThemeSwitchTexts(),
    getTranslations("pages.admin.UserPlansAdminPageTexts"),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getTopPlansTexts(),
    getFindInSiteTexts(),
  ]);
  return {
    plansTableTexts,
    sortingPlansSortingOptions,
    themeSwitchTexts,
    topPlansTexts,
    title: t("title"),
    header: t("header"),
    menuTexts,
    findInSiteTexts,
  };
}

export async function getUserOrdersAdminPageTexts(): Promise<UserOrdersAdminPageTexts> {
  const [
    orderTableTexts,
    sortingOrdersSortingOptions,
    themeSwitchTexts,
    t,
    menuTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getOrderTableTexts(),
    getSortingItemSortingOptions("orders", sortingOrdersSortingOptionsKeys),
    getThemeSwitchTexts(),
    getTranslations("pages.admin.UserOrdersAdminPageTexts"),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getFindInSiteTexts(),
  ]);
  return {
    orderTableTexts,
    sortingOrdersSortingOptions,
    themeSwitchTexts,
    title: t("title"),
    header: t("header"),
    menuTexts,
    findInSiteTexts,
  };
}

export async function getAdminPlansPageTexts(): Promise<AdminPlansPageTexts> {
  const [
    t,
    planTableTexts,
    sortingPlansSortingOptions,
    themeSwitchTexts,
    menuTexts,
    archivePlansTexts,
    archiveDayTexts,
    topPlansTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminPlansPageTexts"),
    getPlanTableTexts(),
    getSortingItemSortingOptions("plans", sortingPlansSortingOptionsKeys),
    getThemeSwitchTexts(),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getArchiveQueueCardsTexts("plan"),
    getArchiveQueueCardsTexts("day"),
    getTopPlansTexts(),
    getFindInSiteTexts(),
  ]);

  return {
    planTableTexts,
    title: t("title"),
    header: t("header"),
    sortingPlansSortingOptions,
    themeSwitchTexts,
    menuTexts,
    archivePlansTexts,
    archiveDayTexts,
    topPlansTexts,
    findInSiteTexts,
  };
}

export async function getUserDaysPageTexts(): Promise<UserDaysPageTexts> {
  const [
    dayTableTexts,
    sortingDaysSortingOptions,
    t,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getDayTableTexts(),
    getSortingItemSortingOptions("days", sortingDaysSortingOptionsKeys),
    getTranslations("pages.days.UserDaysPageTexts"),
    getThemeSwitchTexts(),
    getSidebarMenuTexts(
      "trainer",
      trainerGroupLabels,
      trainerLabels,
      trainerSubLabels,
    ),
    getFindInSiteTexts(),
  ]);
  return {
    dayTableTexts,
    sortingDaysSortingOptions,
    header: t("header"),
    title: t("title"),
    menuTexts,
    themeSwitchTexts,
    findInSiteTexts,
  };
}

export async function getUserPlansPageTexts(): Promise<UserPlansPageTexts> {
  const [
    planTableTexts,
    sortingPlansSortingOptions,
    t,
    themeSwitchTexts,
    menuTexts,
    topPlansTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getPlanTableTexts(),
    getSortingItemSortingOptions("plans", sortingPlansSortingOptionsKeys),
    getTranslations("pages.plans.UserPlansPageTexts"),
    getThemeSwitchTexts(),
    getSidebarMenuTexts(
      "trainer",
      trainerGroupLabels,
      trainerLabels,
      trainerSubLabels,
    ),
    getTopPlansTexts(),
    getFindInSiteTexts(),
  ]);
  return {
    planTableTexts,
    sortingPlansSortingOptions,
    header: t("header"),
    title: t("title"),
    themeSwitchTexts,
    menuTexts,
    topPlansTexts,
    findInSiteTexts,
  };
}

export async function getSingleTrainerPlanPageTexts(): Promise<SingleTrainerPlanPageTexts> {
  const [
    elementHeaderTexts,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    singleDayTexts,
    daysListTexts,
    answerFromBodyFormTexts,
    t,
  ] = await Promise.all([
    getElementHeaderTexts(),
    getNutritionalTableTexts(),
    getIngredientPieChartTexts(),
    getSingleDayTexts(),
    getDaysListTexts(),
    getAnswerFromBodyFormTexts(),
    getTranslations("pages.plans.SingleTrainerPlanPageTexts"),
  ]);

  return {
    elementHeaderTexts,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    singleDayTexts,
    daysListTexts,
    displayed: t("displayed"),
    notDisplayed: t("notDisplayed"),
    answerFromBodyFormTexts,
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
    objectiveDropDownTexts,
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
    getUseFilterDropdownTexts(
      "UseObjectiveDropdownTexts",
      planObjectives as unknown as string[],
    ),
    getTranslations("pages.plans.ApprovedPlansTexts"),
  ]);
  return {
    gridListTexts,
    dietDropdownTexts,
    useApprovedFilterTexts,
    displayFilterTexts,
    objectiveDropDownTexts,
    title: t("title"),
    header: t("header"),
    buyableLabel: t("buyableLabel"),
    sortingPlansSortingOptions,
    addToCartBtnTexts,
  };
}

export async function getUserPlanPageContentTexts(): Promise<UserPlanPageContentTexts> {
  const [
    elementHeaderTexts,
    addToCartBtnTexts,
    planRecommendationListTexts,
    t,
  ] = await Promise.all([
    getElementHeaderTexts(),
    getAddToCartBtnTexts(),
    getPlanRecommendationListTexts(),
    getTranslations("pages.plans.UserPlanPageContentTexts"),
  ]);
  return {
    elementHeaderTexts,
    addToCartBtnTexts,
    planRecommendationListTexts,
    price: t("price"),
    buyPrompt: t("buyPrompt"),
    numberDays: t("numberDays"),
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
  const [
    gridListTexts,
    sortingPlansSortingOptions,
    dietDropdownTexts,
    objectiveDropDownTexts,
    dayCalendarCTATexts,
    t,
  ] = await Promise.all([
    getGridListTexts(),
    getSortingItemSortingOptions("plans", sortingPlansSortingOptionsKeys),
    getUseFilterDropdownTexts(
      "UseDietDropdownTexts",
      dietTypes as unknown as string[],
    ),
    getUseFilterDropdownTexts(
      "UseObjectiveDropdownTexts",
      planObjectives as unknown as string[],
    ),
    getDaysCalendarCTATexts(),
    getTranslations("pages.subscriptions.SubscriptionsPageContentTexts"),
  ]);
  return {
    gridListTexts,
    sortingPlansSortingOptions,
    dietDropdownTexts,
    objectiveDropDownTexts,
    title: t("title"),
    header: t("header"),
    dayCalendarCTATexts,
  };
}

export async function getSingleSubscriptionTexts(): Promise<SingleSubscriptionTexts> {
  const [
    elementHeaderTexts,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    singleDayTexts,
    daysListTexts,
    planRecommendationListTexts,
    answerFromBodyFormTexts,
    addDayToCalendarTexts,
  ] = await Promise.all([
    getElementHeaderTexts(),
    getNutritionalTableTexts(),
    getIngredientPieChartTexts(),
    getSingleDayTexts(),
    getDaysListTexts(),
    getPlanRecommendationListTexts(),
    getAnswerFromBodyFormTexts(),
    getAddDayToCalendarTexts(),
  ]);

  return {
    elementHeaderTexts,
    nutritionalTableTexts,
    ingredientPieChartTexts,
    singleDayTexts,
    daysListTexts,
    planRecommendationListTexts,
    answerFromBodyFormTexts,
    addDayToCalendarTexts,
  };
}

export async function getAdminOrdersPageTexts(): Promise<AdminOrdersPageTexts> {
  const [
    t,
    orderTableTexts,
    sortingOrdersSortingOptions,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminOrdersPageTexts"),
    getOrderTableTexts(),
    getSortingItemSortingOptions("orders", sortingOrdersSortingOptionsKeys),
    getThemeSwitchTexts(),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getFindInSiteTexts(),
  ]);

  return {
    orderTableTexts,
    title: t("title"),
    header: t("header"),
    sortingOrdersSortingOptions,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
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

export async function getAdminArchiveQueuesPageTexts(): Promise<AdminArchiveQueuesPageTexts> {
  const [
    t,
    themeSwitchTexts,
    menuTexts,
    archiveTexts,
    archiveQueueTableTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminArchiveQueuesPageTexts"),
    getThemeSwitchTexts(),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getAllArchiveQueueCardsTexts(),
    getArchiveQueuesTableTexts(),
    getFindInSiteTexts(),
  ]);
  return {
    themeSwitchTexts,
    menuTexts,
    archiveTexts,
    archiveQueueTableTexts,
    findInSiteTexts,
    title: t("title"),
    header: t("header"),
    archiveTitle: t("archiveTitle"),
    archiveTableTitle: t("archiveTableTitle"),
    selectItems: {
      comment: t("selectItems.comment"),
      day: t("selectItems.day"),
      ingredient: t("selectItems.ingredient"),
      meal: t("selectItems.meal"),
      plan: t("selectItems.plan"),
      post: t("selectItems.post"),
      recipe: t("selectItems.recipe"),
      user: t("selectItems.user"),
      all: t("selectItems.all"),
    },
  };
}

export async function getAdminDashboardPageTexts(): Promise<AdminDashboardPageTexts> {
  const [
    t,
    relativeItemTexts,
    themeSwitchTexts,
    relativeItemsSummaryTexts,
    menuTexts,
    archiveTexts,
    topUsersTexts,
    topPlansTexts,
    topTrainersTexts,
    findInSiteTexts,
    topViewedPostsTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminDashboardPageTexts"),
    getAllRelativeItemTexts(),
    getThemeSwitchTexts(),
    getRelativeItemsSummaryTexts(),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getAllArchiveQueueCardsTexts(),
    getTopUsersTexts(),
    getTopPlansTexts(),
    getTopTrainersTexts(),
    getFindInSiteTexts(),
    getTopViewedPostsTexts(),
  ]);

  return {
    title: t("title"),
    header: t("header"),
    relativeItemTexts,
    themeSwitchTexts,
    relativeItemsSummaryTexts,
    menuTexts,
    archiveTexts,
    topUsersTexts,
    topPlansTexts,
    topTrainersTexts,
    findInSiteTexts,
    topViewedPostsTexts,
    archiveTitle: t("archiveTitle"),
    selectItems: {
      comment: t("selectItems.comment"),
      day: t("selectItems.day"),
      ingredient: t("selectItems.ingredient"),
      meal: t("selectItems.meal"),
      plan: t("selectItems.plan"),
      post: t("selectItems.post"),
      recipe: t("selectItems.recipe"),
      user: t("selectItems.user"),
      all: t("selectItems.all"),
    },
  };
}

export async function getAdminMonthlySalesTexts(): Promise<AdminMonthlySalesTexts> {
  const [
    t,
    monthlySalesTexts,
    plansMonthlySales,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminMonthlySalesTexts"),
    getMonthlySalesTexts("orders"),
    getMonthlySalesTexts("plans"),
    getThemeSwitchTexts(),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getFindInSiteTexts(),
  ]);
  return {
    title: t("title"),
    header: t("header"),
    plansTitle: t("plansTitle"),
    monthlySalesTexts,
    themeSwitchTexts,
    menuTexts,
    plansMonthlySales,
    findInSiteTexts,
  };
}

export async function getAdminCountriesTexts(): Promise<AdminCountriesTexts> {
  const [t, themeSwitchTexts, geographyChartTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getTranslations("pages.admin.AdminCountriesTexts"),
      getThemeSwitchTexts(),
      getGeographyChartTexts(),
      getSidebarMenuTexts(
        "admin",
        adminGroupLabels,
        adminLabels,
        adminSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    title: t("title"),
    header: t("header"),
    themeSwitchTexts,
    geographyChartTexts,
    menuTexts,
    findInSiteTexts,
  };
}

export async function getAdminDailySalesTexts(): Promise<AdminDailySalesTexts> {
  const [
    t,
    dailySalesTexts,
    plansDailySalesTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  ] = await Promise.all([
    getTranslations("pages.admin.AdminDailySalesTexts"),
    getDailySalesTexts("orders"),
    getDailySalesTexts("plans"),
    getThemeSwitchTexts(),
    getSidebarMenuTexts("admin", adminGroupLabels, adminLabels, adminSubLabels),
    getFindInSiteTexts(),
  ]);
  return {
    title: t("title"),
    header: t("header"),
    plansTitle: t("plansTitle"),
    dailySalesTexts,
    themeSwitchTexts,
    menuTexts,
    plansDailySalesTexts,
    findInSiteTexts,
  };
}

export async function getUserMonthlySalesPageTexts(): Promise<UserMonthlySalesPageTexts> {
  const [t, monthlySalesTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getTranslations("pages.plans.UserMonthlySalesPageTexts"),
      getMonthlySalesTexts("plans"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    title: t("title"),
    header: t("header"),
    monthlySalesTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}

export async function getUserDailySalesPageTexts(): Promise<UserDailySalesPageTexts> {
  const [t, dailySalesTexts, themeSwitchTexts, menuTexts, findInSiteTexts] =
    await Promise.all([
      getTranslations("pages.plans.UserDailySalesPageTexts"),
      getDailySalesTexts("plans"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getFindInSiteTexts(),
    ]);
  return {
    title: t("title"),
    header: t("header"),
    dailySalesTexts,
    themeSwitchTexts,
    menuTexts,
    findInSiteTexts,
  };
}

export async function getUserAdminMonthlySalesPageTexts(): Promise<UserAdminMonthlySalesPageTexts> {
  const [t, monthlySalesTexts, menuTexts, themeSwitchTexts, findInSiteTexts] =
    await Promise.all([
      getTranslations("pages.admin.UserAdminMonthlySalesPageTexts"),
      getMonthlySalesTexts("plans"),
      getSidebarMenuTexts(
        "admin",
        adminGroupLabels,
        adminLabels,
        adminSubLabels,
      ),
      getThemeSwitchTexts(),
      getFindInSiteTexts(),
    ]);
  return {
    title: t("title"),
    header: t("header"),
    monthlySalesTexts,
    menuTexts,
    themeSwitchTexts,
    findInSiteTexts,
  };
}

export async function getUserAdminDailySalesPageTexts(): Promise<UserAdminDailySalesPageTexts> {
  const [t, dailySalesTexts, menuTexts, themeSwitchTexts, findInSiteTexts] =
    await Promise.all([
      getTranslations("pages.admin.UserAdminDailySalesPageTexts"),
      getDailySalesTexts("plans"),
      getSidebarMenuTexts(
        "admin",
        adminGroupLabels,
        adminLabels,
        adminSubLabels,
      ),
      getThemeSwitchTexts(),
      getFindInSiteTexts(),
    ]);
  return {
    title: t("title"),
    header: t("header"),
    dailySalesTexts,
    menuTexts,
    themeSwitchTexts,
    findInSiteTexts,
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
  const [
    homeHeaderTexts,
    homeCardsTexts,
    homeAboutTexts,
    homeTimelineTexts,
    homeHeroTexts,
    homeTestimonialsTexts,
    homeMpaTexts,
  ] = await Promise.all([
    getHomeHeaderTexts(),
    getHomeCardsText(),
    getHomeAboutTexts(),
    getHomeTimelineTexts(),
    getHomeHeroTexts(),
    getHomeTestimonialsTexts(),
    getHomeMapTexts(),
  ]);
  return {
    homeHeaderTexts,
    homeCardsTexts,
    homeAboutTexts,
    homeTimelineTexts,
    homeHeroTexts,
    homeTestimonialsTexts,
    homeMapTexts: homeMpaTexts,
  };
}

export async function getAdminEmailPageTexts(): Promise<AdminEmailPageTexts> {
  const [adminEmail, themeSwitchTexts, menuTexts, findInSiteTexts, t] =
    await Promise.all([
      getAdminEmailTexts(),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "admin",
        adminGroupLabels,
        adminLabels,
        adminSubLabels,
      ),
      getFindInSiteTexts(),
      getTranslations("pages.admin.AdminEmailPageTexts"),
    ]);

  return {
    adminEmail,
    themeSwitchTexts,
    title: t("title"),
    header: t("header"),
    menuTexts,
    findInSiteTexts,
  };
}

export async function getAdminKanbanTexts(): Promise<AdminKanbanTexts> {
  const [t, themeSwitchTexts, menuTexts, kanbanBoardTexts, findInSiteTexts] =
    await Promise.all([
      getTranslations("pages.admin.AdminKanbanTexts"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "admin",
        adminGroupLabels,
        adminLabels,
        adminSubLabels,
      ),
      getKanbanBoardTexts(),
      getFindInSiteTexts(),
    ]);
  return {
    title: t("title"),
    header: t("header"),
    themeSwitchTexts,
    menuTexts,
    kanbanBoardTexts,
    findInSiteTexts,
  };
}

export async function getTrainerKanbanPageTexts(): Promise<TrainerKanbanPageTexts> {
  const [t, themeSwitchTexts, menuTexts, kanbanBoardTexts, findInSiteTexts] =
    await Promise.all([
      getTranslations("pages.trainer.TrainerKanbanPageTexts"),
      getThemeSwitchTexts(),
      getSidebarMenuTexts(
        "trainer",
        trainerGroupLabels,
        trainerLabels,
        trainerSubLabels,
      ),
      getKanbanBoardTexts(),
      getFindInSiteTexts(),
    ]);
  return {
    title: t("title"),
    header: t("header"),
    themeSwitchTexts,
    menuTexts,
    kanbanBoardTexts,
    findInSiteTexts,
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

export async function getErrorPageTexts() {
  const t = await getTranslations("ErrorPage");
  return {
    description: t("description"),
    statusCode: t("statusCode", { statusCode: 404 }),
    title: t("title"),
  };
}

export const intakeTitles = [
  "Maintain weight",
  "Mild weight loss",
  "Weight loss",
  "Extreme weight loss",
  "Mild weight gain",
  "Weight gain",
  "Fast Weight gain",
] as const;

export async function getCalculatorPageTexts(): Promise<CalculatorPageTexts> {
  const [activitiesTexts, genderText, calculatorSchemaTexts, t] =
    await Promise.all([
      getActivitiesTexts(),
      getGenderTexts(),
      getCalculatorSchemaTexts(),
      getTranslations("pages.calculator.CalculatorPageTexts"),
    ]);

  return {
    activitiesTexts,
    genderText,
    calculatorSchemaTexts,
    imperial: t("imperial"),
    metric: t("metric"),
    header: t("header"),
    title: t("title"),
    button: t("button"),
    message1: t("message1"),
    message2: t("message2"),
    itemsTexts: {
      age: {
        label: t(`itemsTexts.age.label`),
        placeholder: t(`itemsTexts.age.placeholder`),
        description: t(`itemsTexts.age.description`),
      },
      ...["gender", "activity", "height", "weight", "intake"].reduce(
        (acc, key) => ({
          ...acc,
          [key]: {
            label: t(`itemsTexts.${key}.label`),
            placeholder: t(`itemsTexts.${key}.placeholder`),
          },
        }),
        {} as Record<
          "activity" | "gender" | "height" | "weight" | "intake",
          ItemTexts
        >,
      ),
    },
    week: t("week"),
    intakeTitles: intakeTitles.reduce(
      (acc, title) => ({ ...acc, [title]: t(`intakeTitles.${title}`) }),
      {} as IntakeTitle,
    ),
  };
}

export async function getSignOutPageTexts() {
  const t = await getTranslations("auth.SignOutPageText");
  return {
    buttonSignIn: t("buttonSignIn"),
    buttonSignOut: t("buttonSignOut"),
    questionText: t("questionText"),
  };
}

export const terms = [
  "terms1",
  "terms2",
  "terms3",
  "terms4",
  "terms5",
] as const;

export async function getTermsOfServiceTextsBase(path: string) {
  const t = await getTranslations(path);
  return {
    title: t("title"),
    terms: terms.reduce(
      (acc, key) => ({
        ...acc,
        [key]: {
          title: t(`terms.${key}.title`),
          body: t(`terms.${key}.body`),
        },
      }),
      {} as Record<(typeof terms)[number], { title: string; body: string }>,
    ),
  };
}

export async function getTermsOfServiceTexts(): Promise<TermsOfServiceTexts> {
  return getTermsOfServiceTextsBase(
    "pages.terms-of-service.TermsOfServiceTexts",
  );
}
export async function getPrivacyPolicyTexts() {
  return getTermsOfServiceTextsBase("pages.privacy-policy.PrivacyPolicyTexts");
}

export async function getDisclaimerTexts() {
  return getTermsOfServiceTextsBase("pages.disclaimer.DisclaimerTexts");
}

export async function getChatPageTexts(): Promise<ChatPageTexts> {
  const [chatRoomsTexts, t] = await Promise.all([
    getChatRoomsTexts(),
    getTranslations("pages.chat.ChatPageTexts"),
  ]);
  return {
    chatRoomsTexts,
    noChatSelectedText: t("noChatSelectedText"),
    headingText: t("headingText"),
  };
}
