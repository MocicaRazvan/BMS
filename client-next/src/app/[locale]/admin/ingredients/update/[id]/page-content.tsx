"use client";
import { WithUser } from "@/lib/user";
import useFetchStream from "@/hoooks/useFetchStream";
import { BaseError } from "@/types/responses";
import {
  CustomEntityModel,
  IngredientNutritionalFactResponse,
} from "@/types/dto";
import LoadingSpinner from "@/components/common/loading-spinner";
import { notFound } from "next/navigation";
import IngredientForm, {
  IngredientFormTexts,
} from "@/components/forms/ingredient-form";

interface Props extends WithUser {
  id: string;
  ingredientFormTexts: IngredientFormTexts;
}

export default function AdminIngredientsPageContent({
  id,
  authUser,
  ingredientFormTexts,
}: Props) {
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
    console.log("HERE");
    return notFound();
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
