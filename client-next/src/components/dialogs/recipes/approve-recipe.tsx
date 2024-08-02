import { AlertDialogApprove } from "@/components/dialogs/approve-model";

import { RecipeResponse } from "@/types/dto";
import { WithUser } from "@/lib/user";
import { approveRecipeNotificationName } from "@/context/recipe-approve-notification-context";

interface Props extends WithUser {
  recipe: RecipeResponse;
  callBack: () => void;
}

export default function AlertDialogApproveRecipes({
  recipe,
  authUser,
  callBack,
}: Props) {
  return (
    <AlertDialogApprove
      callBack={callBack}
      model={recipe}
      authUser={authUser}
      path="recipes"
      title={recipe.title}
      approved={!recipe.approved}
      notificationName={approveRecipeNotificationName}
      stompExtraLink={`/recipes/single/${recipe.id}`}
    />
  );
}
