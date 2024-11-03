import { AlertDialogDelete } from "@/components/dialogs/delete-model";
import { RecipeResponse } from "@/types/dto";

interface Props {
  recipe: RecipeResponse;
  token: string | undefined;
  callBack: () => void;
  title: string;
}

export default function AlertDialogDeleteRecipe({
  recipe,
  token,
  callBack,
  title,
}: Props) {
  return (
    <AlertDialogDelete
      callBack={callBack}
      model={recipe}
      token={token}
      path="recipes"
      title={title}
    />
  );
}
