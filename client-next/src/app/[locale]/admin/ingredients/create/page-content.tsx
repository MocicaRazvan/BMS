"use client";

import IngredientForm, {
  IngredientFormTexts,
} from "@/components/forms/ingredient-form";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

interface Props {
  texts: IngredientFormTexts;
}

export default function AdminIngredientsCreatePageContent({ texts }: Props) {
  const { authUser } = useAuthUserMinRole();

  return (
    <IngredientForm authUser={authUser} path="/ingredients/create" {...texts} />
  );
}
