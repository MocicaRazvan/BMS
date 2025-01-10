import { useGetTitleBodyUser } from "@/hoooks/useGetTitleBodyUser";
import {
  IngredientNutritionalFactResponseWithCount,
  RecipeResponse,
} from "@/types/dto";
import useFetchStream from "@/hoooks/useFetchStream";
import { Session } from "next-auth";

export default function useGetRecipeWithIngredients(
  recipeId: number | string,
  authUser: NonNullable<Session["user"]>,
  recipeBasePath?: string,
  ingredientPath?: string,
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
  } = useGetTitleBodyUser<RecipeResponse>({
    authUser,
    basePath: recipeBasePath || `/recipes/withUser`,
    itemId: recipeId,
  });

  const {
    messages: IQMessage,
    error: IQError,
    isFinished: IQIsFinished,
  } = useFetchStream<IngredientNutritionalFactResponseWithCount>({
    path: ingredientPath || `/recipes/ingredients/${recipeId}`,
    method: "GET",
    authToken: true,
    // useAbortController: false,
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
  };
}
