"use client";
import useFetchStream from "@/lib/fetchers/useFetchStream";
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

export default function AdminPageDuplicateIngredientContent({
  id,
  ingredientFormTexts,
}: Props) {
  const { authUser } = useAuthUserMinRole();

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
