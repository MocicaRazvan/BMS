"use client";

import RecipeForm, { RecipeFormProps } from "@/components/forms/recipe-form";
import useFetchStream from "@/hoooks/useFetchStream";
import {
  CustomEntityModel,
  IngredientNutritionalFactResponseWithCount,
  RecipeResponse,
} from "@/types/dto";
import LoadingSpinner from "@/components/common/loading-spinner";
import { checkOwner } from "@/lib/utils";
import React, { useMemo } from "react";
import { v4 as uuidv4 } from "uuid";
import { Option } from "@/components/ui/multiple-selector";
import useClientNotFound from "@/hoooks/useClientNotFound";

interface Props extends RecipeFormProps {
  id: string;
}
export default function UpdateRecipePageContent({
  id,
  authUser,
  ...props
}: Props) {
  const {
    messages: recipeMessage,
    error: recipeError,
    isFinished: recipeIsFinished,
  } = useFetchStream<CustomEntityModel<RecipeResponse>>({
    path: `/recipes/${id}`,
    method: "GET",
    authToken: true,
    // useAbortController: false,
  });

  const {
    messages: IQMessage,
    error: IQError,
    isFinished: IQIsFinished,
  } = useFetchStream<IngredientNutritionalFactResponseWithCount>({
    path: `/recipes/ingredients/${id}`,
    method: "GET",
    authToken: true,
    // useAbortController: false,
  });

  const { navigateToNotFound } = useClientNotFound();

  console.log("HERE", recipeIsFinished, recipeMessage, recipeError);
  console.log("HEREIQ", IQIsFinished, IQMessage, IQError, IQMessage.length);

  // const ingredients: RecipeSchemaType["ingredients"] = useMemo(
  //   () =>
  //     IQMessage?.map(({ ingredient: { id }, count }) => ({
  //       id,
  //       quantity: count,
  //     })),
  //   [IQMessage],
  // );
  // const childIds = useMemo(
  //   () => (ingredients.length === 0 ? [] : ingredients.map((_) => uuidv4())),
  //   [ingredients],
  // );

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
