import useFetchStream from "@/hoooks/useFetchStream";
import {
  CustomEntityModel,
  IngredientNutritionalFactResponseWithCount,
  RecipeResponse,
} from "@/types/dto";
import { useGetRecipeChildrenOptions } from "@/hoooks/recipes/useGetRecipeChildrenOptions";

export function useGetRecipeWithNF(id: string) {
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

  const children = useGetRecipeChildrenOptions(IQMessage);
  return {
    recipeMessage,
    recipeError,
    recipeIsFinished,
    IQMessage,
    IQError,
    IQIsFinished,
    children,
  };
}
