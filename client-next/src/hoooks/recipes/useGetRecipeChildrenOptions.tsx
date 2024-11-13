import { IngredientNutritionalFactResponseWithCount } from "@/types/dto";
import { useMemo } from "react";
import { Option } from "@/components/ui/multiple-selector";
import { v4 as uuidv4 } from "uuid";

export const useGetRecipeChildrenOptions = (
  IQMessage: IngredientNutritionalFactResponseWithCount[],
) => {
  const children: Record<string, Option & { quantity: number }> = useMemo(
    () =>
      IQMessage?.length === 0
        ? {}
        : IQMessage?.reduce(
            (
              acc,
              {
                count,
                ingredient: { id, name, type },
                nutritionalFact: {
                  unit,
                  fat,
                  saturatedFat,
                  protein,
                  carbohydrates,
                  salt,
                  sugar,
                },
              },
            ) => {
              const childId = uuidv4();
              return {
                ...acc,
                [childId]: {
                  value: id.toString(),
                  label: name,
                  disable: true,
                  childId,
                  type,
                  unit,
                  fat: fat.toString(),
                  saturatedFat: saturatedFat.toString(),
                  protein: protein.toString(),
                  carbohydrates: carbohydrates.toString(),
                  salt: salt.toString(),
                  sugar: sugar.toString(),
                  quantity: count,
                },
              };
            },
            {},
          ),

    [IQMessage],
  );

  return children;
};
