"use client";

import { ReactNode } from "react";
import { WithUser } from "@/lib/user";
import { IngredientNutritionalFactResponse } from "@/types/dto";
import { BaseDialogTexts } from "@/components/dialogs/delete-model";
import ToggleDisplayDialog from "@/components/dialogs/toggle-display";

export interface AlertDialogToggleDisplayTexts extends BaseDialogTexts {
  toast: string | ReactNode;
}

interface Props extends WithUser {
  model: IngredientNutritionalFactResponse;
  callBack: () => void;
}

export default function ToggleDisplayIngredient({
  callBack,
  model,
  authUser,
}: Props) {
  return (
    <ToggleDisplayDialog
      model={model.ingredient}
      path={`/ingredients/alterDisplay/${model.ingredient.id}`}
      callBack={callBack}
      authUser={authUser}
    />
  );
}
