"use server";

import { DataTablePaginationTexts } from "@/components/table/data-table-pagination";
import { getTranslations } from "next-intl/server";
import { DataTableTexts } from "@/components/table/data-table";
import {
  PostTableColumnsTexts,
  PostTableTexts,
} from "@/components/table/posts-table";
import {
  getUseApprovedFilterTexts,
  getUseBinaryEmailVerifiedTexts,
  getUseBinaryTexts,
  getUseFilterDropdownTexts,
  getUseProviderFilterDropdownTexts,
  getUseRoleFilterTexts,
  getUseTagsExtraCriteriaTexts,
} from "@/texts/components/list";
import { getRadioSortTexts } from "@/texts/components/common";
import {
  dayColumnActions,
  dayTableColumns,
  ingredientColumnActions,
  ingredientTableColumns,
  orderColumnActions,
  orderTableColumns,
  planColumnActions,
  planTableColumns,
  postColumnActions,
  postTableColumns,
  recipeColumnActions,
  recipeTableColumns,
  userColumnActions,
  userTableColumns,
} from "@/lib/constants";
import {
  UserTableColumnsTexts,
  UserTableTexts,
} from "@/components/table/users-table";
import {
  IngredientTableColumnTexts,
  IngredientTableTexts,
} from "@/components/table/ingredients-table";
import { display } from "html2canvas/dist/types/css/property-descriptors/display";
import { dietTypes } from "@/types/forms";
import {
  RecipeTableColumnsTexts,
  RecipeTableTexts,
} from "@/components/table/recipes-table";
import {
  PlanTableColumnsTexts,
  PlanTableTexts,
} from "@/components/table/plans-table";
import {
  OrderTableColumnsTexts,
  OrderTableTexts,
} from "@/components/table/orders-table";
import {
  DayTableColumnsTexts,
  DayTableTexts,
} from "@/components/table/day-table";
import { dayTypes, planObjectives } from "@/types/dto";

export async function getDataTablePaginationTexts(): Promise<DataTablePaginationTexts> {
  const t = await getTranslations("components.table.DataTablePaginationTexts");
  return {
    of: t("of"),
    lastPage: t("lastPage"),
    firstPage: t("firstPage"),
    nextPage: t("nextPage"),
    previousPage: t("previousPage"),
    pageSize: t("pageSize"),
    page: t("page"),
  };
}

export async function getDataTableTexts(): Promise<DataTableTexts> {
  const [dataTablePaginationTexts, radioSortTexts, t] = await Promise.all([
    getDataTablePaginationTexts(),
    getRadioSortTexts(),
    getTranslations("components.table.DataTableTexts"),
  ]);
  return {
    dataTablePaginationTexts,
    columnsLabel: t("columnsLabel"),
    noResults: t("noResults"),
    radioSortTexts,
    exportLabel: t("exportLabel"),
  };
}

export type ColumnActionsTexts<A extends readonly string[]> = {
  [key in A[number]]: string;
};

export type PostColumnActionsTexts = {
  [key in (typeof postColumnActions)[number]]: string;
};

export async function getColumnActionsTexts<A extends readonly string[]>(
  key: string,
  arr: A,
): Promise<ColumnActionsTexts<typeof arr>> {
  const t = await getTranslations(`components.table.columns.${key}.actions`);
  return arr.reduce<ColumnActionsTexts<A>>(
    (acc, cur) => ({ ...acc, [cur]: t(cur) }),
    {} as ColumnActionsTexts<typeof arr>,
  );
}

export async function getPostTableColumnsTexts(): Promise<PostTableColumnsTexts> {
  const [t, actions] = await Promise.all([
    getTranslations("components.table.columns.posts"),
    getColumnActionsTexts<typeof postColumnActions>("posts", postColumnActions),
  ]);
  return {
    approved: {
      header: t("approved.header"),
      false: t("approved.false"),
      true: t("approved.true"),
    },
    ...postTableColumns.slice(0, -2).reduce(
      (acc, cur) => ({
        ...acc,
        [cur]: t(cur),
      }),
      {} as Omit<PostTableColumnsTexts, "approved">,
    ),
    actions,
  };
}

export async function getUserTableColumnsTexts(): Promise<UserTableColumnsTexts> {
  const [t, actions] = await Promise.all([
    getTranslations("components.table.columns.users"),
    getColumnActionsTexts<typeof userColumnActions>("users", userColumnActions),
  ]);
  return {
    ...userTableColumns.slice(0, -2).reduce(
      (acc, cur) => ({
        ...acc,
        [cur]: t(cur),
      }),
      {} as Omit<UserTableColumnsTexts, "actions">,
    ),
    actions,
    emailVerified: {
      header: t("emailVerified.header"),
      trueText: t("emailVerified.trueText"),
      falseText: t("emailVerified.falseText"),
    },
  };
}
export async function getIngredientTableColumnTexts(): Promise<IngredientTableColumnTexts> {
  const [t, actions] = await Promise.all([
    getTranslations("components.table.columns.ingredients"),
    getColumnActionsTexts<typeof ingredientColumnActions>(
      "ingredients",
      ingredientColumnActions,
    ),
  ]);
  return {
    ...ingredientTableColumns.slice(0, -2).reduce(
      (acc, cur) => ({
        ...acc,
        [cur]: t(cur),
      }),
      {} as Omit<IngredientTableColumnTexts, "actions" | "display">,
    ),
    actions,
    display: {
      header: t("display.header"),
      true: t("display.true"),
      false: t("display.false"),
    },
  };
}

export async function getDayTableColumnsTexts(): Promise<DayTableColumnsTexts> {
  const [t, actions] = await Promise.all([
    getTranslations("components.table.columns.days"),
    getColumnActionsTexts<typeof dayColumnActions>("days", dayColumnActions),
  ]);
  return {
    ...dayTableColumns.slice(0, -1).reduce(
      (acc, cur) => ({
        ...acc,
        [cur]: t(cur),
      }),
      {} as Omit<DayTableColumnsTexts, "actions">,
    ),
    actions,
  };
}

export async function getPlanTableColumnsTexts(): Promise<PlanTableColumnsTexts> {
  const [t, actions] = await Promise.all([
    getTranslations("components.table.columns.plans"),
    getColumnActionsTexts<typeof planColumnActions>("plans", planColumnActions),
  ]);
  return {
    ...planTableColumns.slice(0, -3).reduce(
      (acc, cur) => ({
        ...acc,
        [cur]: t(cur),
      }),
      {} as Omit<PlanTableColumnsTexts, "actions" | "display" | "approved">,
    ),
    actions,
    display: {
      header: t("display.header"),
      true: t("display.true"),
      false: t("display.false"),
    },
    approved: {
      header: t("approved.header"),
      false: t("approved.false"),
      true: t("approved.true"),
    },
  };
}
export async function getRecipeTableColumnsTexts(): Promise<RecipeTableColumnsTexts> {
  const [t, actions] = await Promise.all([
    getTranslations("components.table.columns.recipes"),
    getColumnActionsTexts<typeof recipeColumnActions>(
      "recipes",
      recipeColumnActions,
    ),
  ]);
  return {
    approved: {
      header: t("approved.header"),
      false: t("approved.false"),
      true: t("approved.true"),
    },
    ...recipeTableColumns.slice(0, -2).reduce(
      (acc, cur) => ({
        ...acc,
        [cur]: t(cur),
      }),
      {} as Omit<RecipeTableColumnsTexts, "approved">,
    ),
    actions,
  };
}

export async function getPostTableTexts(): Promise<PostTableTexts> {
  const [
    dataTableTexts,
    useTagsExtraCriteriaTexts,
    useApprovedFilterTexts,
    postTableColumnsTexts,

    t,
  ] = await Promise.all([
    getDataTableTexts(),
    getUseTagsExtraCriteriaTexts(),
    getUseApprovedFilterTexts(),
    getPostTableColumnsTexts(),
    getTranslations("components.table.PostTableTexts"),
  ]);
  return {
    dataTableTexts,
    useTagsExtraCriteriaTexts,
    useApprovedFilterTexts,
    postTableColumnsTexts,
    search: t("search"),
  };
}

export async function getUserTableTexts(): Promise<UserTableTexts> {
  const [
    dataTableTexts,
    userTableColumnsTexts,
    useProviderFilterDropdownTexts,
    useRoleFilterTexts,
    useBinaryEmailVerifiedTexts,
    t,
  ] = await Promise.all([
    getDataTableTexts(),
    getUserTableColumnsTexts(),
    getUseProviderFilterDropdownTexts(),
    getUseRoleFilterTexts(),
    getUseBinaryEmailVerifiedTexts(),
    getTranslations("components.table.UserTableTexts"),
  ]);
  return {
    dataTableTexts,
    userTableColumnsTexts,
    useProviderFilterDropdownTexts,
    useRoleFilterTexts,
    useBinaryEmailVerifiedTexts,
    search: t("search"),
  };
}

export async function getIngredientTableTexts(): Promise<IngredientTableTexts> {
  const [
    dataTableTexts,
    ingredientTableColumnTexts,
    displayFilterTexts,
    dietDropdownTexts,
    t,
  ] = await Promise.all([
    getDataTableTexts(),
    getIngredientTableColumnTexts(),
    getUseBinaryTexts("UseDisplayFilterTexts"),
    getUseFilterDropdownTexts(
      "UseDietDropdownTexts",
      dietTypes as unknown as string[],
    ),
    getTranslations("components.table.IngredientTableTexts"),
  ]);

  return {
    dataTableTexts,
    ingredientTableColumnTexts,
    displayFilterTexts,
    dietDropdownTexts,
    search: t("search"),
  };
}

export async function getRecipeTableTexts(): Promise<RecipeTableTexts> {
  const [
    dataTableTexts,
    recipeTableColumnsTexts,
    useApprovedFilterTexts,
    dietDropdownTexts,
    t,
  ] = await Promise.all([
    getDataTableTexts(),
    getRecipeTableColumnsTexts(),
    getUseApprovedFilterTexts(),
    getUseFilterDropdownTexts(
      "UseDietDropdownTexts",
      dietTypes as unknown as string[],
    ),
    getTranslations("components.table.RecipeTableTexts"),
  ]);
  return {
    recipeTableColumnsTexts,
    dataTableTexts,
    dietDropdownTexts,
    useApprovedFilterTexts,
    search: t("search"),
  };
}

export async function getDayTableTexts(): Promise<DayTableTexts> {
  const [dataTableTexts, dayTableColumnTexts, typeDropdownTexts, t] =
    await Promise.all([
      getDataTableTexts(),
      getDayTableColumnsTexts(),
      getUseFilterDropdownTexts(
        "UseTypeDropdownTexts",
        dayTypes as unknown as string[],
      ),
      getTranslations("components.table.DayTableTexts"),
    ]);

  return {
    dataTableTexts,
    dayTableColumnTexts,
    typeDropdownTexts,
    search: t("search"),
  };
}

export async function getPlanTableTexts(): Promise<PlanTableTexts> {
  const [
    dataTableTexts,
    useApprovedFilterTexts,
    displayFilterTexts,
    dietDropdownTexts,
    objectiveDropDownTexts,
    planTableColumnsTexts,
    t,
  ] = await Promise.all([
    getDataTableTexts(),
    getUseApprovedFilterTexts(),
    getUseBinaryTexts("UseDisplayFilterTexts"),
    getUseFilterDropdownTexts(
      "UseDietDropdownTexts",
      dietTypes as unknown as string[],
    ),
    getUseFilterDropdownTexts(
      "UseObjectiveDropdownTexts",
      planObjectives as unknown as string[],
    ),
    getPlanTableColumnsTexts(),
    getTranslations("components.table.PlanTableTexts"),
  ]);

  return {
    dataTableTexts,
    useApprovedFilterTexts,
    displayFilterTexts,
    dietDropdownTexts,
    planTableColumnsTexts,
    objectiveDropDownTexts,
    search: t("search"),
  };
}

export async function getOrderTableColumnsTexts(): Promise<OrderTableColumnsTexts> {
  const [t, actions] = await Promise.all([
    getTranslations("components.table.columns.orders"),
    getColumnActionsTexts<typeof orderColumnActions>(
      "orders",
      orderColumnActions,
    ),
  ]);

  return {
    ...orderTableColumns.slice(0, -1).reduce(
      (acc, cur) => ({
        ...acc,
        [cur]: t(cur),
      }),
      {} as Omit<OrderTableColumnsTexts, "actions">,
    ),
    actions,
  };
}

export async function getOrderTableTexts(): Promise<OrderTableTexts> {
  const [dataTableTexts, orderTableColumnsTexts, t] = await Promise.all([
    getDataTableTexts(),
    getOrderTableColumnsTexts(),
    getTranslations("components.table.OrderTableTexts"),
  ]);

  return {
    dataTableTexts,
    orderTableColumnsTexts,
    search: t("search"),
    searchKeyLabel: {
      city: t("searchKeyLabel.city"),
      country: t("searchKeyLabel.country"),
      state: t("searchKeyLabel.state"),
    },
  };
}
