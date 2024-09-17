"use client";

import { WithUser } from "@/lib/user";
import IngredientForm, {
  IngredientFormTexts,
} from "@/components/forms/ingredient-form";

interface Props extends WithUser {
  texts: IngredientFormTexts;
}

export default function AdminIngredientsCreatePageContent({
  authUser,
  texts,
}: Props) {
  return (
    <IngredientForm authUser={authUser} path="/ingredients/create" {...texts} />
  );
}
