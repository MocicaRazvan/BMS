"use client";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { cn } from "@/lib/utils";
import { Badge } from "@/components/ui/badge";
import React from "react";
import { IngredientNutritionalFactResponseWithCount } from "@/types/dto";

interface RecipeIngredientsProps {
  showIngredients: string;
  IQMessage: IngredientNutritionalFactResponseWithCount[];
}
const colorMap = {
  VEGAN: "success",
  OMNIVORE: "default",
  VEGETARIAN: "accent",
};
export default function RecipeIngredients({
  showIngredients,
  IQMessage,
}: RecipeIngredientsProps) {
  return (
    <Accordion
      type={"single"}
      collapsible
      className="w-full md:w-2/3 mx-auto mt-20 lg:mt-25"
    >
      <AccordionItem value={"item"}>
        <AccordionTrigger className="text-lg md:text-xl">
          {showIngredients}
        </AccordionTrigger>
        <AccordionContent className="space-y-10 mt-5">
          {IQMessage.map(
            (
              {
                ingredient: { name, type, id },
                nutritionalFact: { unit },
                count,
              },
              i,
            ) => (
              <div
                key={id}
                className={cn(
                  "w-full gap-10 md:gap-0 flex flex-col md:flex-row items-center justify-between px-0 lg:px-6",
                  i !== IQMessage.length - 1 && "border-b pb-4",
                )}
              >
                <div className="flex w-full items-center justify-around md:justify-between flex-1 md:flex-2 gap-1.5 md:gap-3.5">
                  <h3 className="text-lg ">{name}</h3>
                  <div className=" flex items-center justify-center gap-2">
                    <span className={"font-semibold"}>{count}</span>
                    <Badge variant={unit === "GRAM" ? "secondary" : "default"}>
                      {unit}
                    </Badge>
                  </div>
                </div>

                <div className={"flex-1 flex justify-end"}>
                  <p
                    className={cn(
                      `px-3 py-1 bg-${colorMap[type]} text-${colorMap[type]}-foreground rounded-full font-bold`,
                    )}
                  >
                    {type}
                  </p>
                </div>
              </div>
            ),
          )}
        </AccordionContent>
      </AccordionItem>
    </Accordion>
  );
}
