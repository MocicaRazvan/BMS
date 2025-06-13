import { AlertDialogApprove } from "@/components/dialogs/approve-model";

import { RecipeResponse } from "@/types/dto";
import { WithUser } from "@/lib/user";
import { approveRecipeNotificationName } from "@/context/recipe-approve-notification-context";
import { forwardRef } from "react";

interface Props extends WithUser {
  recipe: RecipeResponse;
  callBack: () => void;
}

const AlertDialogApproveRecipes = forwardRef<HTMLDivElement, Props>(
  ({ recipe, authUser, callBack }, ref) => {
    return (
      <div ref={ref}>
        <AlertDialogApprove
          callBack={callBack}
          model={recipe}
          authUser={authUser}
          path="recipes"
          title={recipe.title}
          approved={!recipe.approved}
          notificationName={approveRecipeNotificationName}
          stompExtraLink={`/trainer/recipes/single/${recipe.id}`}
        />
      </div>
    );
  },
);
AlertDialogApproveRecipes.displayName = "AlertDialogApproveRecipes";
export default AlertDialogApproveRecipes;
