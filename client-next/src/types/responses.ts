import {
  ArchiveQueue,
  ArchiveQueuePrefix,
  IngredientNutritionalFactResponse,
} from "@/types/dto";

export interface BaseError {
  message: string;
  timestamp: string;
  error: string;
  status: number;
  path: string;
}

export const isBaseError = (error: unknown): error is BaseError =>
  typeof error === "object" &&
  error !== null &&
  "message" in error &&
  typeof (error as any).message === "string" &&
  "timestamp" in error &&
  typeof (error as any).timestamp === "string" &&
  "error" in error &&
  typeof (error as any).error === "string" &&
  "status" in error &&
  typeof (error as any).status === "number" &&
  "path" in error &&
  typeof (error as any).path === "string";

export enum NutritionalConversionFactors {
  PROTEIN = 3.47,
  FAT = 8.37,
  CARBOHYDRATES = 4.07,
}
export const Macros = ["protein", "fat", "carbohydrates"] as const;
export type Macro = "protein" | "fat" | "carbohydrates";
export function getNutritionalConversionFactorByName(string: Macro) {
  return NutritionalConversionFactors[
    string.toUpperCase() as keyof typeof NutritionalConversionFactors
  ];
}

export function getCalories({
  nutritionalFact: { fat, protein, carbohydrates },
}: IngredientNutritionalFactResponse) {
  return Math.ceil(
    fat * NutritionalConversionFactors.FAT +
      protein * NutritionalConversionFactors.PROTEIN +
      carbohydrates * NutritionalConversionFactors.CARBOHYDRATES,
  );
}

export function calculateRecipeCaloriesPer100(
  recipe: {
    fat: number;
    protein: number;
    carbohydrates: number;
    quantity: number;
  }[],
): number {
  const totalCalories = recipe.reduce((total, ingredient) => {
    const ingredientCalories = getCalories({
      nutritionalFact: {
        fat: ingredient.fat,
        protein: ingredient.protein,
        carbohydrates: ingredient.carbohydrates,
      },
    } as IngredientNutritionalFactResponse);
    const ingredientCaloriesForQuantity =
      (ingredientCalories / 100) * ingredient.quantity;
    return total + ingredientCaloriesForQuantity;
  }, 0);

  const totalQuantity = recipe.reduce(
    (total, ingredient) => total + ingredient.quantity,
    0,
  );

  const caloriesPer100Grams = (totalCalories / totalQuantity) * 100;

  return Math.ceil(caloriesPer100Grams);
}

export function getArchiveQueuesNameByPrefix(prefix: ArchiveQueuePrefix) {
  return Object.values(ArchiveQueue).filter((q) => q.startsWith(prefix));
}
