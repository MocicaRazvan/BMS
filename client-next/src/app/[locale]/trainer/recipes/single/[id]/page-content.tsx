"use client";

import { WithUser } from "@/lib/user";
import LoadingSpinner from "@/components/common/loading-spinner";
import { checkOwnerOrAdmin, cn, isSuccessCheckReturn } from "@/lib/utils";
import React from "react";
import { ElementHeaderTexts } from "@/components/common/element-header";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import ProseText from "@/components/common/prose-text";
import AuthorProfile from "@/components/common/author-profile";
import CustomVideoCarousel from "@/components/common/custom-videos-crousel";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { IngredientPieChartTexts } from "@/components/charts/ingredient-macros-pie-chart";
import useGetRecipeWithIngredients from "@/hoooks/recipes/useGetRecipeWithIngredients";
import RecipeMacros from "@/components/recipes/recipe-macros";
import useClientNotFound from "@/hoooks/useClientNotFound";
import LikesDislikes from "@/components/common/likes-dislikes";
import DietBadge from "@/components/common/diet-badge";
import RecipeIngredients from "@/components/recipes/recipe-ingredients";

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
    <section className="w-full mx-auto max-w-[1500px] min-h-[calc(100vh-4rem)] flex-col items-center justify-center transition-all px-1 md:px-6 py-10 relative ">
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
            className={cn(
              "text-2xl md:text-4xl text-balance tracking-tighter font-bold text-center  ",
            )}
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
        <RecipeIngredients
          showIngredients={showIngredients}
          IQMessage={IQMessage}
        />
      )}
    </section>
  );
}
