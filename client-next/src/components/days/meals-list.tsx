"use client";

import { MealResponse } from "@/types/dto";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import {
  MealRecipeProps,
  OpenedRecipeItemState,
  SetRecipeOpenType,
} from "@/components/days/meal-recipes";
import { memo, useCallback, useState } from "react";
import { Clock } from "lucide-react";
import dynamicWithPreload from "@/lib/dynamic-with-preload";
import { Skeleton } from "@/components/ui/skeleton";
import usePreloadDynamicComponents from "@/hoooks/use-prelod-dynamic-components";

const DynamicMealRecipeList = dynamicWithPreload(
  () => import("@/components/days/meal-recipes").then((m) => m.MealRecipeList),
  {
    loading: () => <Skeleton className="size-full min-h-[40vh]" />,
  },
);

export interface MealListProps
  extends Omit<
    MealRecipeProps,
    "recipeIds" | "setRecipeOpen" | "openedRecipes"
  > {
  meals: MealResponse[];
  recipeBasePath?: string;
}

const MealsList = memo(({ meals, authUser, ...rest }: MealListProps) => {
  const [openedRecipes, setOpenedRecipes] = useState<OpenedRecipeItemState>(
    () =>
      meals.reduce((acc, meal) => {
        meal.recipes.forEach((recipeId) => {
          acc[recipeId] = {
            triggered: false,
          };
        });
        return acc;
      }, {} as OpenedRecipeItemState),
  );

  const setRecipeOpen: SetRecipeOpenType = useCallback(
    (rId, recipe, iqItems) => {
      setOpenedRecipes((prev) => ({
        ...prev,
        [rId]: {
          triggered: true,
          recipe,
          iqItems,
        },
      }));
    },
    [],
  );

  usePreloadDynamicComponents(DynamicMealRecipeList);
  return (
    <Accordion type="multiple" className="w-full">
      {meals.map((meal, i) => (
        <AccordionItem
          key={meal.id + "_" + i}
          value={meal.id.toString()}
          className="my-6"
        >
          <AccordionTrigger>
            <div className="flex items-center justify-start gap-10">
              <Clock size={32} />
              <p className="text-xl font-semibold">{meal.period}</p>
            </div>
          </AccordionTrigger>
          <AccordionContent>
            <MealItem
              meal={meal}
              authUser={authUser}
              setRecipeOpen={setRecipeOpen}
              openedRecipes={openedRecipes}
              {...rest}
            />
          </AccordionContent>
        </AccordionItem>
      ))}
    </Accordion>
  );
});
MealsList.displayName = "MealsList";
export default MealsList;

interface MealItemProps extends Omit<MealRecipeProps, "recipeIds"> {
  meal: MealResponse;
}

function MealItem({ meal, authUser, ...rest }: MealItemProps) {
  return (
    <div>
      <DynamicMealRecipeList
        authUser={authUser}
        recipeIds={meal.recipes}
        {...rest}
      />
    </div>
  );
}
