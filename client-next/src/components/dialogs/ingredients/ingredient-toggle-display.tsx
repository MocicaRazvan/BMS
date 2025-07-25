"use client";

import { forwardRef, ReactNode } from "react";
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

const ToggleDisplayIngredient = forwardRef<HTMLDivElement, Props>(
  ({ callBack, model, authUser }, ref) => {
    return (
      <div ref={ref}>
        <ToggleDisplayDialog
          model={model.ingredient}
          path={`/ingredients/alterDisplay/${model.ingredient.id}`}
          callBack={callBack}
          authUser={authUser}
        />
      </div>
    );
  },
);
ToggleDisplayIngredient.displayName = "ToggleDisplayIngredient";
export default ToggleDisplayIngredient;
