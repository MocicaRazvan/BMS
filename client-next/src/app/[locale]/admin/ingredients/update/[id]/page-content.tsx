"use client";
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
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

interface Props {
  id: string;
  ingredientFormTexts: IngredientFormTexts;
}

export default function AdminIngredientsPageContent({
  id,
  ingredientFormTexts,
}: Props) {
  const { authUser } = useAuthUserMinRole();

  const { messages, error, isFinished } = useFetchStream<
    CustomEntityModel<IngredientNutritionalFactResponse>,
    BaseError
  >({
    path: `/ingredients/${id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
  });

  const { navigateToNotFound } = useClientNotFound();

  if (!isFinished) return <LoadingSpinner />;

  if (error || !messages[0]?.content) {
    console.log("HERE");
    return navigateToNotFound();
  }
  console.log("MESSAGES", messages);

  return (
    <div>
      <IngredientForm
        path={`/ingredients/update/${id}`}
        authUser={authUser}
        {...ingredientFormTexts}
        ingredient={messages[0].content.ingredient}
        nutritionalFact={messages[0].content.nutritionalFact}
      />
    </div>
  );
}
