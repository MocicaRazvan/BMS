import { useGetTitleBodyUser } from "@/hoooks/useGetTitleBodyUser";
import {
  IngredientNutritionalFactResponseWithCount,
  RecipeResponse,
} from "@/types/dto";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import { Session } from "next-auth";

export default function useGetRecipeWithIngredients(
  recipeId: number | string,
  authUser: NonNullable<Session["user"]>,
  recipeBasePath?: string,
  ingredientPath?: string,
  trigger = true,
) {
  const {
    itemState: recipeState,
    setItemState: setRecipeState,
    messages,
    error: recipeError,
    item: recipe,
    user,
    router,
    isFinished: recipeIsFinished,
    isLiked,
    isDisliked,
    isAbsoluteFinished: recipeIsAbsoluteFinished,
  } = useGetTitleBodyUser<RecipeResponse>({
    authUser,
    basePath: recipeBasePath || `/recipes/withUser`,
    itemId: recipeId,
    trigger,
  });

  const {
    messages: IQMessage,
    error: IQError,
    isFinished: IQIsFinished,
    isAbsoluteFinished: IQIsAbsoluteFinished,
  } = useFetchStream<IngredientNutritionalFactResponseWithCount>({
    path: ingredientPath || `/recipes/ingredients/${recipeId}`,
    method: "GET",
    authToken: true,
    trigger,
  });

  console.log("IQError", IQError);

  return {
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
    IQIsAbsoluteFinished,
    recipeIsAbsoluteFinished,
  };
}
