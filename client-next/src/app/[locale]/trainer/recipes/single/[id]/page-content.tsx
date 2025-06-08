"use client";

import LoadingSpinner from "@/components/common/loading-spinner";
import { checkOwnerOrAdmin, cn, isSuccessCheckReturn } from "@/lib/utils";
import React from "react";
import { ElementHeaderTexts } from "@/components/common/element-header";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import AuthorProfile from "@/components/common/author-profile";
import CustomVideoCarousel from "@/components/common/custom-videos-crousel";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { IngredientPieChartTexts } from "@/components/charts/ingredient-macros-pie-chart";
import useGetRecipeWithIngredients from "@/hoooks/recipes/useGetRecipeWithIngredients";
import useClientNotFound from "@/hoooks/useClientNotFound";
import LikesDislikes from "@/components/common/likes-dislikes";
import DietBadge from "@/components/common/diet-badge";
import RecipeIngredients from "@/components/recipes/recipe-ingredients";
import { AnswerFromBodyFormTexts } from "@/components/forms/answer-from-body-form";
import ItemBodyQa from "@/components/common/item-body-qa";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import dynamic from "next/dynamic";
import { Skeleton } from "@/components/ui/skeleton";

export interface SingleRecipePageTexts {
  elementHeaderTexts: ElementHeaderTexts;
  nutritionalTableTexts: NutritionalTableTexts;
  ingredientPieChartTexts: IngredientPieChartTexts;
  showIngredients: string;
  answerFromBodyFormTexts: AnswerFromBodyFormTexts;
}

interface Props extends SingleRecipePageTexts {
  id: string;
}

const DynamicRecipeMacros = dynamic(
  () => import("@/components/recipes/recipe-macros"),
  {
    ssr: false,
    loading: () => (
      <div className="h-[350px] lg:h-[400px]">
        <Skeleton className="size-full rounded-xl" />
      </div>
    ),
  },
);

export default function SingeRecipePageContent({
  id,
  elementHeaderTexts,
  nutritionalTableTexts,
  ingredientPieChartTexts,
  showIngredients,
  answerFromBodyFormTexts,
}: Props) {
  const { authUser } = useAuthUserMinRole();

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
            </div>
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
        <ItemBodyQa
          html={recipe?.body}
          formProps={{ body: recipe?.body, texts: answerFromBodyFormTexts }}
        />
        <AuthorProfile author={user} />
      </div>
      {recipe?.videos.length > 0 && (
        <div className="mt-20">
          <CustomVideoCarousel videos={recipe?.videos} />
        </div>
      )}

      <div className="mt-20">
        <DynamicRecipeMacros
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
