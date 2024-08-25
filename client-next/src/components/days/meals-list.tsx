"use client";

import { MealResponse } from "@/types/dto";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import {
  MealRecipeList,
  MealRecipeProps,
} from "@/components/days/meal-recipes";
import React, { memo } from "react";
import { Clock } from "lucide-react";
export interface MealListProps extends Omit<MealRecipeProps, "recipeIds"> {
  meals: MealResponse[];
  recipeBasePath?: string;
}

const MealsList = memo(({ meals, authUser, ...rest }: MealListProps) => {
  return (
    <Accordion type={"multiple"} className="w-full">
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
            <MealItem meal={meal} authUser={authUser} {...rest} />
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
      <MealRecipeList authUser={authUser} recipeIds={meal.recipes} {...rest} />
    </div>
  );
}
