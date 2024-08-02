"use client";

import NutritionalTable, {
  NutritionalTableTexts,
} from "@/components/common/nutritional-table";
import {
  IngredientNutritionalFactResponse,
  IngredientNutritionalFactResponseWithCount,
  NutritionalFactResponse,
} from "@/types/dto";
import IngredientMacrosPieChart, {
  IngredientPieChartTexts,
} from "@/components/charts/ingredient-macros-pie-chart";
import { useMemo } from "react";
interface Props {
  ingredients: IngredientNutritionalFactResponseWithCount[];
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
}
export default function RecipeMacros({
  ingredients,
  ingredientPieChartTexts,
  nutritionalTableTexts,
}: Props) {
  const aggegatedNF: NutritionalFactResponse | null = useMemo(
    () =>
      ingredients.length === 0
        ? null
        : ingredients
            .map((i) => i.nutritionalFact)
            .reduce<NutritionalFactResponse>((acc, cur) => {
              if (!acc?.fat) return cur;
              return {
                ...acc,
                fat: acc.fat + cur.fat,
                saturatedFat: acc.saturatedFat + cur.saturatedFat,
                carbohydrates: acc.carbohydrates + cur.carbohydrates,
                sugar: acc.sugar + cur.sugar,
                protein: acc.protein + cur.protein,
                salt: acc.salt + cur.salt,
              };
            }, {} as NutritionalFactResponse),
    [ingredients],
  );
  if (!aggegatedNF) return null;
  return (
    <div className="px-0 lg:px-16space-y-8">
      <NutritionalTable
        ing={
          {
            nutritionalFact: aggegatedNF,
          } as IngredientNutritionalFactResponse
        }
        {...nutritionalTableTexts}
        tableClassName={"w-[80%] mx-auto"}
        showUnit={false}
      />
      <div className="h-[350px] lg:h-[400px]">
        <IngredientMacrosPieChart
          innerRadius={85}
          // items={[
          //   { macro: "protein", value: nutritionalFact.protein },
          //   { macro: "fat", value: nutritionalFact.fat },
          //   {
          //     macro: "carbohydrates",
          //     value: nutritionalFact.carbohydrates,
          //   },
          //   { macro: "salt", value: nutritionalFact.salt },
          // ]}
          items={[
            {
              macro: "protein",
              value: aggegatedNF.protein,
            },
            {
              macro: "fat",
              value: aggegatedNF.fat,
            },
            {
              macro: "carbohydrates",
              value: aggegatedNF.carbohydrates,
            },
            {
              macro: "salt",
              value: aggegatedNF.salt,
            },
          ]}
          texts={ingredientPieChartTexts}
        />
      </div>
    </div>
  );
}
