"use client";

import { WithUser } from "@/lib/user";
import { CustomEntityModel, RecipeResponse } from "@/types/dto";
import LoadingSpinner from "@/components/common/loading-spinner";
import {
  checkApprovePrivilege,
  checkOwnerOrAdmin,
  cn,
  isSuccessCheckReturn,
} from "@/lib/utils";
import React, { useCallback } from "react";
import ElementHeader, {
  ElementHeaderTexts,
} from "@/components/common/element-header";
import { fetchStream } from "@/hoooks/fetchStream";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import ProseText from "@/components/common/prose-text";
import AuthorProfile from "@/components/common/author-profile";
import CustomVideoCarousel from "@/components/common/custom-videos-crousel";
import NutritionalTable, {
  NutritionalTableTexts,
} from "@/components/common/nutritional-table";
import IngredientMacrosPieChart, {
  IngredientPieChartTexts,
} from "@/components/charts/ingredient-macros-pie-chart";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { Badge } from "@/components/ui/badge";
import useGetRecipeWithIngredients from "@/hoooks/recipes/useGetRecipeWithIngredients";
import RecipeMacros from "@/components/recipes/recipe-macros";
import useClientNotFound from "@/hoooks/useClientNotFound";
import LikesDislikes from "@/components/common/likes-dislikes";
import DietBadge from "@/components/common/diet-badge";

export interface SingleRecipePageTexts {
  elementHeaderTexts: ElementHeaderTexts;
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  showIngredients: string;
}

interface Props extends WithUser, SingleRecipePageTexts {
  id: string;
}
export default function SingeRecipePageContent({
  authUser,
  id,
  elementHeaderTexts,
  nutritionalTableTexts,
  ingredientPieChartTexts,
  showIngredients,
}: Props) {
  const {
    recipeState,
    setRecipeState,
    messages,
    recipeError,
    recipe,
    user,
    router,
    recipeIsFinished,
    isLiked,
    isDisliked,
    IQMessage,
    IQError,
    IQIsFinished,
  } = useGetRecipeWithIngredients(id, authUser);

  const { navigateToNotFound } = useClientNotFound();

  if (!recipeIsFinished || !IQIsFinished)
    return (
      <section className="w-full min-h-[calc(100vh-4rem)] flex items-center justify-center transition-all">
        <LoadingSpinner />
      </section>
    );

  if (recipeError || IQError) {
    return navigateToNotFound();
  }

  if (!recipeState) return null;

  const colorMap = {
    VEGAN: "success",
    OMNIVORE: "default",
    VEGETARIAN: "accent",
  };

  const ownerReturn = checkOwnerOrAdmin(
    authUser,
    recipeState,
    navigateToNotFound,
  );

  if (React.isValidElement(ownerReturn)) {
    return ownerReturn;
  }

  if (!isSuccessCheckReturn(ownerReturn)) {
    return navigateToNotFound();
  }

  return (
    <section className="w-full mx-auto max-w-[1500px] min-h-[calc(100vh-4rem)] flex-col items-center justify-center transition-all px-6 py-10 relative ">
      {/*<ElementHeader*/}
      {/*  elementState={recipeState}*/}
      {/*  react={react}*/}
      {/*  isLiked={isLiked}*/}
      {/*  isDisliked={isDisliked}*/}
      {/*  {...elementHeaderTexts}*/}
      {/*/>*/}
      <div className="w-3/4 mx-auto flex flex-col md:flex-row items-center justify-between gap-10 md:gap-20 mb-2 ">
        <div className="order-1 flex items-center justify-center gap-3">
          <div className="flex flex-row md:flex-col items-center justify-center gap-4 flex-1">
            <div className="text-xl lg:text-2xl font-bold tracking-tight">
              {recipe?.approved ? (
                <p className="text-success">{elementHeaderTexts.approved}</p>
              ) : (
                <p className="text-destructive">
                  {elementHeaderTexts.notApproved}
                </p>
              )}
            </div>{" "}
            <div className="flex items-center justify-center gap-4">
              <LikesDislikes
                likes={recipeState.userDislikes}
                dislikes={recipeState.userDislikes}
                isLiked={isLiked || false}
                isDisliked={isDisliked || false}
                disabled={true}
              />
            </div>
          </div>
        </div>
        <div className=" flex items-center justify-center order-0 md:order-1 flex-1 ">
          <h1
            className={cn("text-5xl tracking-tighter font-bold text-center  ")}
          >
            {recipeState.title}
          </h1>
        </div>
        <div className="order-3">
          <DietBadge dietType={recipeState.type} />
        </div>
      </div>
      {recipe?.images.length > 0 && (
        <div className="mt-10">
          <CustomImageCarousel images={recipe?.images} />
        </div>
      )}
      <div className="mt-20 px-14">
        <ProseText html={recipe?.body} />
        <AuthorProfile author={user} />
      </div>
      {recipe?.videos.length > 0 && (
        <div className="mt-20">
          <CustomVideoCarousel videos={recipe?.videos} />
        </div>
      )}

      <div className="mt-20">
        <RecipeMacros
          ingredients={IQMessage}
          nutritionalTableTexts={nutritionalTableTexts}
          ingredientPieChartTexts={ingredientPieChartTexts}
        />
      </div>
      {IQMessage.length > 0 && (
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
                        <Badge
                          variant={unit === "GRAM" ? "secondary" : "default"}
                        >
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
      )}
    </section>
  );
}
