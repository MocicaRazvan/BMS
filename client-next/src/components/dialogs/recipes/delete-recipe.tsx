import { AlertDialogDelete } from "@/components/dialogs/delete-model";
import { RecipeResponse } from "@/types/dto";
import { forwardRef } from "react";

interface Props {
  recipe: RecipeResponse;
  token: string | undefined;
  callBack: () => void;
  title: string;
}

const AlertDialogDeleteRecipe = forwardRef<HTMLDivElement, Props>(
  ({ recipe, token, callBack, title }, ref) => {
    return (
      <div ref={ref}>
        <AlertDialogDelete
          callBack={callBack}
          model={recipe}
          token={token}
          path="recipes"
          title={title}
        />
      </div>
    );
  },
);
AlertDialogDeleteRecipe.displayName = "AlertDialogDeleteRecipe";
export default AlertDialogDeleteRecipe;
