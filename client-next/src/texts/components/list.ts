import { ItemCardTexts } from "@/components/list/item-card";
import { getTranslations } from "next-intl/server";
import { GridListTexts } from "@/components/list/grid-list";
import { getDataTablePaginationTexts } from "@/texts/components/table";
import { UseApprovedFilterTexts } from "@/components/list/useApprovedFilter";
import { UseTagsExtraCriteriaTexts } from "@/components/list/useTagsExtraCriteria";
import { getRadioSortTexts } from "@/texts/components/common";
import { SortingOptionsTexts } from "@/lib/constants";
import { UseFilterDropdownTexts } from "@/components/list/useFilterDropdown";
import { UseBinaryTexts } from "@/components/list/useBinaryFilter";
import { getDateRangePickerTexts } from "@/texts/components/ui";
import { CreationFilterTexts } from "@/components/list/creation-filter";

export const sortingPostsSortingOptionsKeys = [
  "title",
  "createdAt",
  "updatedAt",
  "userLikesLength",
  "userDislikesLength",
] as const;
export const sortingRecipesSortingOptionsKeys = [
  "title",
  "createdAt",
  "updatedAt",
  "userLikesLength",
  "userDislikesLength",
] as const;

export const sortingUsersSortingOptionsKeys = [
  "email",
  "firstName",
  "lastName",
  "createdAt",
] as const;

export const sortingIngredientsSortingOptionsKeys = [
  "name",
  "createdAt",
  "protein",
  "fat",
  "carbohydrates",
  "salt",
  "sugar",
  "saturatedFat",
  "calories",
] as const;

export const sortingDaysSortingOptionsKeys = [
  "title",
  "createdAt",
  "updatedAt",
  "userLikesLength",
  "userDislikesLength",
] as const;
export const sortingPlansSortingOptionsKeys = [
  "title",
  "createdAt",
  "updatedAt",
  "price",
  "userLikesLength",
  "userDislikesLength",
] as const;
export const sortingOrdersSortingOptionsKeys = ["createdAt", "total"] as const;
export async function getSortingItemSortingOptions(
  key: string,
  options: readonly string[],
): Promise<SortingOptionsTexts> {
  const t = await getTranslations(`pages.${key}.SortingOptions`);
  return options.reduce<SortingOptionsTexts>((acc, key) => {
    if (!acc[key]) acc[key] = {} as Record<"asc" | "desc", string>;
    acc[key]["asc"] = t(`${key}.asc`);
    acc[key]["desc"] = t(`${key}.desc`);
    return acc;
  }, {} as SortingOptionsTexts);
}

export async function getItemCardTexts(name: string): Promise<ItemCardTexts> {
  const t = await getTranslations("components.list.ItemCardTexts");
  return {
    author: t("author", { name }),
  };
}

export async function getCreationFilterTexts(): Promise<CreationFilterTexts> {
  const [dateRangePickerTexts, t] = await Promise.all([
    getDateRangePickerTexts(),
    getTranslations("components.list.CreationFilterTexts"),
  ]);

  return {
    dateRangePickerTexts,
    createdAtLabel: t("createdAtLabel"),
    updatedAtLabel: t("updatedAtLabel"),
  };
}

export async function getGridListTexts(): Promise<GridListTexts> {
  const [
    itemCardTexts,
    dataTablePaginationTexts,
    radioSortTexts,
    creationFilterTexts,
    t,
  ] = await Promise.all([
    getItemCardTexts("ItemCardTexts"),
    getDataTablePaginationTexts(),
    getRadioSortTexts(),
    getCreationFilterTexts(),
    getTranslations("components.list.GridListTexts"),
  ]);

  return {
    itemCardTexts,
    gettingMore: t("gettingMore"),
    noResults: t("noResults"),
    search: t("search"),
    creationFilterTexts,
    radioSortTexts,
    dataTablePaginationTexts,
  };
}

export async function getUseApprovedFilterTexts(): Promise<UseApprovedFilterTexts> {
  const t = await getTranslations("components.list.UseApprovedFilterTexts");
  return {
    approved: t("approved"),
    notApproved: t("notApproved"),
    all: t("all"),
  };
}

export async function getUseTagsExtraCriteriaTexts(): Promise<UseTagsExtraCriteriaTexts> {
  const t = await getTranslations("components.list.UseTagsExtraCriteriaTexts");
  return {
    tagsEmpty: t("tagsEmpty"),
    tagsPlaceholder: t("tagsPlaceholder"),
  };
}

export async function getUseFilterDropdownTexts(
  key: string,
  values: string[],
): Promise<UseFilterDropdownTexts> {
  const t = await getTranslations(`components.list.${key}`);

  return {
    noFilterLabel: t("noFilterLabel"),
    labels: values.reduce(
      (acc, cur) => ({
        ...acc,
        [cur]: t(`labels.${cur}`),
      }),
      {} as Record<string, string>,
    ),
  };
}

export async function getUseProviderFilterDropdownTexts(): Promise<UseFilterDropdownTexts> {
  return getUseFilterDropdownTexts("UseProviderFilterDropdownTexts", [
    "GOOGLE",
    "GITHUB",
    "LOCAL",
  ]);
}

export async function getUseRoleFilterTexts(): Promise<UseFilterDropdownTexts> {
  return getUseFilterDropdownTexts("UseRoleFilterTexts", [
    "ROLE_ADMIN",
    "ROLE_USER",
    "ROLE_TRAINER",
  ]);
}

export async function getUseBinaryTexts(key: string): Promise<UseBinaryTexts> {
  const t = await getTranslations(`components.list.${key}`);
  return {
    all: t("all"),
    falseText: t("falseText"),
    trueText: t("trueText"),
  };
}

export async function getUseBinaryEmailVerifiedTexts(): Promise<UseBinaryTexts> {
  return getUseBinaryTexts("UseBinaryEmailVerifiedTexts");
}
