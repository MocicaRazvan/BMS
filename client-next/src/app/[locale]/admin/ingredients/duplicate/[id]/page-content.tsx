"use client";
import { WithUser } from "@/lib/user";
import useFetchStream from "@/hoooks/useFetchStream";
import { BaseError } from "@/types/responses";
import {
  CustomEntityModel,
  IngredientNutritionalFactResponse,
} from "@/types/dto";
import LoadingSpinner from "@/components/common/loading-spinner";
import IngredientForm, {
  IngredientFormTexts,
} from "@/components/forms/ingredient-form";
import useClientNotFound from "@/hoooks/useClientNotFound";

interface Props extends WithUser {
  id: string;
  ingredientFormTexts: IngredientFormTexts;
}

export default function AdminPageDuplicateIngredientContent({
  id,
  authUser,
  ingredientFormTexts,
}: Props) {
  const { navigateToNotFound } = useClientNotFound();
  const { messages, error, isFinished } = useFetchStream<
    CustomEntityModel<IngredientNutritionalFactResponse>,
    BaseError
  >({
    path: `/ingredients/${id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
  });

  if (!isFinished) return <LoadingSpinner />;

  if (error || !messages[0]?.content) {
    return navigateToNotFound();
  }

  return (
    <div>
      <IngredientForm
        path={"/ingredients/create"}
        authUser={authUser}
        {...ingredientFormTexts}
        ingredient={{
          ...messages[0].content.ingredient,
          name: "",
        }}
        nutritionalFact={messages[0].content.nutritionalFact}
      />
    </div>
  );
}
