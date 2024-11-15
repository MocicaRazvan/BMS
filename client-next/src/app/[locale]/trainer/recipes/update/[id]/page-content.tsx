"use client";

import RecipeForm, { RecipeFormProps } from "@/components/forms/recipe-form";
import LoadingSpinner from "@/components/common/loading-spinner";
import { checkOwner } from "@/lib/utils";
import React from "react";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { useGetRecipeWithNF } from "@/hoooks/recipes/useGetRecipeWithNF";

interface Props extends RecipeFormProps {
  id: string;
}
export default function UpdateRecipePageContent({
  id,
  authUser,
  ...props
}: Props) {
  // const {
  //   messages: recipeMessage,
  //   error: recipeError,
  //   isFinished: recipeIsFinished,
  // } = useFetchStream<CustomEntityModel<RecipeResponse>>({
  //   path: `/recipes/${id}`,
  //   method: "GET",
  //   authToken: true,
  //   // useAbortController: false,
  // });
  //
  // const {
  //   messages: IQMessage,
  //   error: IQError,
  //   isFinished: IQIsFinished,
  // } = useFetchStream<IngredientNutritionalFactResponseWithCount>({
  //   path: `/recipes/ingredients/${id}`,
  //   method: "GET",
  //   authToken: true,
  //   // useAbortController: false,
  // });
  const {
    recipeIsFinished,
    recipeMessage,
    recipeError,
    IQIsFinished,
    IQMessage,
    IQError,
    children,
  } = useGetRecipeWithNF(id);
  const { navigateToNotFound } = useClientNotFound();

  console.log("HERE", recipeIsFinished, recipeMessage, recipeError);
  console.log("HEREIQ", IQIsFinished, IQMessage, IQError, IQMessage.length);

  // const children: Record<string, Option & { quantity: number }> =
  //   useGetRecipeChildrenOptions(IQMessage);

  if (!recipeIsFinished || !IQIsFinished) return <LoadingSpinner />;

  if (recipeError?.status || IQError?.status) {
    return navigateToNotFound();
  }
  if (recipeIsFinished && !recipeMessage[0]) {
    return <LoadingSpinner />;
  }
  if (IQIsFinished && !(IQMessage.length > 0)) {
    return <LoadingSpinner />;
  }

  const recipe = recipeMessage[0].content;

  const ownerReturn = checkOwner(authUser, recipe, navigateToNotFound);

  if (React.isValidElement(ownerReturn)) {
    return ownerReturn;
  }

  return (
    <RecipeForm
      authUser={authUser}
      {...props}
      title={recipe.title}
      body={recipe.body}
      images={recipe.images}
      videos={recipe.videos}
      initialChildren={children}
    />
  );
}
