import { Option } from "@/components/ui/multiple-selector";
import { SortingOption } from "@/components/list/grid-list";

export const GOOGLE_STATE_COOKIE_NAME = "googleState";

export const tags = [
  "#wellness",
  "#fitness",
  "#nutrition",
  "#mentalhealth",
  "#yoga",
  "#meditation",
  "#mindfulness",
  "#selfcare",
] as const;
export const tagsOptions: Option[] = tags.map((tag) => ({
  label: tag,
  value: tag,
}));

export interface SortingOptionsTexts {
  [key: string]: {
    asc: string;
    desc: string;
  };
}

export const getSortingOptions = <T extends SortingOptionsTexts>(
  options: readonly string[],
  texts: T,
) =>
  options.reduce<SortingOption[]>((acc, cur) => {
    acc.push({
      property: cur,
      direction: "asc",
      text: texts[cur].asc,
    });
    acc.push({
      property: cur,
      direction: "desc",
      text: texts[cur].desc,
    });
    return acc;
  }, []);

export const postTableColumns = [
  "id",
  "title",
  "userLikes",
  "userDislikes",
  "createdAt",
  "updatedAt",
  "approved",
  "actions",
] as const;
export const recipeTableColumns = [
  "id",
  "title",
  "userLikes",
  "userDislikes",
  "createdAt",
  "updatedAt",
  "type",
  "count",
  "approved",
  "actions",
] as const;

export const userTableColumns = [
  "id",
  "email",
  "firstName",
  "lastName",
  "role",
  "provider",
  "createdAt",
  "emailVerified",
  "actions",
] as const;

export const ingredientTableColumns = [
  "id",
  "name",
  "count",
  "fat",
  "saturatedFat",
  "carbohydrates",
  "sugar",
  "protein",
  "salt",
  "calories",
  "unit",
  "type",
  "createdAt",
  "updatedAt",
  "display",
  "actions",
] as const;

export const dayTableColumns = [
  "id",
  "title",
  "count",
  "type",
  "userLikes",
  "userDislikes",
  "createdAt",
  "updatedAt",
  "actions",
] as const;

export const planTableColumns = [
  "id",
  "title",
  "userLikes",
  "userDislikes",
  "createdAt",
  "updatedAt",
  "type",
  "count",
  "price",
  "objective",
  "display",
  "approved",
  "actions",
] as const;
export const orderTableColumns = [
  "id",
  "date",
  "plans",
  "total",
  "address",
  "actions",
] as const;
export const postColumnActions = [
  "button",
  "label",
  "view",
  "viewOwner",
  "viewOwnerItems",
  "update",
  "approve",
  "disapprove",
  "duplicate",
] as const;
export const orderColumnActions = [
  "button",
  "label",
  "view",
  "viewOwner",
  "viewOwnerItems",
] as const;
export const recipeColumnActions = [
  "button",
  "label",
  "view",
  "viewOwner",
  "viewOwnerItems",
  "update",
  "approve",
  "disapprove",
  "duplicate",
] as const;
export const userColumnActions = [
  "button",
  "label",
  "copyEmail",
  "viewUser",
  "viewOrders",
  "viewPosts",
  "viewRecipes",
  "viewPlans",
  "startChat",
  "makeTrainer",
  "viewMonthlySales",
  "viewDailySales",
] as const;

export const ingredientColumnActions = [
  "button",
  "label",
  "viewIngredient",
  "updateIngredient",
  "deleteIngredient",
  "toggleDisplay",
  "duplicate",
] as const;

export const dayColumnActions = [
  "button",
  "label",
  "view",
  "update",
  "duplicate",
] as const;

export const planColumnActions = [
  "button",
  "label",
  "view",
  "viewOwner",
  "viewOwnerItems",
  "update",
  "approve",
  "disapprove",
  "display",
  "hide",
  "duplicate",
] as const;

export const NEXT_CSRF_COOKIES = [
  "__Host-next-auth.csrf-token",
  "next-auth.csrf-token",
] as const;

export const NEXT_CSRF_HEADER_TOKEN = "x-csrf-token" as const;

export const NEXT_CSRF_HEADER = "x-csrf-header" as const;
