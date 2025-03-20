import { IngredientNutritionalFactResponse } from "@/types/dto";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { memo, useEffect, useState } from "react";
import {
  DeleteDialogTexts,
  getAlertDialogDeleteTexts,
} from "@/texts/components/dialog";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { toast } from "@/components/ui/use-toast";
import { isDeepEqual } from "@/lib/utils";

interface Props {
  ingredientNutritionalFactResponse: IngredientNutritionalFactResponse;
  token: string;
  callBack: () => void;
  anchor?: React.ReactNode;
}

const AlertDialogDeleteIngredient = memo(
  ({ ingredientNutritionalFactResponse, token, callBack, anchor }: Props) => {
    const [dialogDeleteTexts, setDialogDeleteTexts] =
      useState<DeleteDialogTexts | null>(null);
    useEffect(() => {
      getAlertDialogDeleteTexts(
        ingredientNutritionalFactResponse.ingredient.name,
      ).then(setDialogDeleteTexts);
    }, [ingredientNutritionalFactResponse.ingredient.name]);

    const deleteModel = async () => {
      try {
        const resp = await fetchStream({
          path: `/ingredients/delete/${ingredientNutritionalFactResponse.ingredient.id}`,
          method: "DELETE",
          token,
        });
        if (resp.error) {
          console.log(resp.error);
        } else {
          toast({
            title: ingredientNutritionalFactResponse.ingredient.name,
            description: "Deleted",
            variant: "destructive",
          });
          callBack?.();
        }
      } catch (error) {
        console.log(error);
      }
    };

    if (!dialogDeleteTexts) return;

    return (
      <AlertDialog>
        <AlertDialogTrigger asChild>
          {anchor ? (
            anchor
          ) : (
            <Button
              variant="outline"
              className="border-destructive text-destructive"
            >
              {dialogDeleteTexts.anchor}
            </Button>
          )}
        </AlertDialogTrigger>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{dialogDeleteTexts.title}</AlertDialogTitle>
            <AlertDialogDescription>
              {dialogDeleteTexts.description}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>{dialogDeleteTexts.cancel}</AlertDialogCancel>
            <AlertDialogAction asChild onClick={deleteModel}>
              <Button variant="destructive">{dialogDeleteTexts.confirm}</Button>
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    );
  },
  (
    { callBack: prevCallBack, ...prevProps },
    { callBack: nextCallBack, ...nextProps },
  ) => isDeepEqual(prevProps, nextProps),
);

AlertDialogDeleteIngredient.displayName = "AlertDialogDeleteIngredient";

export default AlertDialogDeleteIngredient;
