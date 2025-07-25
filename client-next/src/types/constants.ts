import { Option } from "@/components/ui/multiple-selector";
import { SortingOption } from "@/components/list/grid-list";
import { ContainerAction, DayType } from "@/types/dto";
import { Role } from "@/types/fetch-utils";
import { RelativeItems } from "@/components/charts/relative-item-chart";

export const MX_SPRING_MESSAGE = "does not have a valid MX record" as const;
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
export const containerActionColors = {
  [ContainerAction.START_CRON]: "amber",
  [ContainerAction.STOP]: "destructive",
  [ContainerAction.START_MANUAL]: "success",
};
export const getColorsByDayType = (opacity = 1): Record<DayType, string> => ({
  LOW_CARB: `hsl(var(--chart-1)/${opacity})`,
  HIGH_CARB: `hsl(var(--chart-2)/${opacity})`,
  HIGH_PROTEIN: `hsl(var(--chart-8)/${opacity})`,
  LOW_FAT: `hsl(var(--chart-4)/${opacity})`,
  HIGH_FAT: `hsl(var(--chart-10)/${opacity})`,
  LOW_PROTEIN: `hsl(var(--chart-6)/${opacity})`,
  BALANCED: `hsl(var(--chart-9)/${opacity})`,
});
export const getColorByDayType = (dayType: DayType, opacity = 1) =>
  getColorsByDayType(opacity)?.[dayType];

export const HIERARCHY: Record<Role | "undefined", number> = {
  undefined: 0,
  ROLE_USER: 1,
  ROLE_TRAINER: 2,
  ROLE_ADMIN: 3,
};

export const relativeItems: RelativeItems[] = [
  "posts",
  "orders",
  "recipes",
  "plans",
  "comments",
] as const;
