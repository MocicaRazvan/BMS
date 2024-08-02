"use client";

import { ReactNode, useEffect, useState } from "react";
import { WithUser } from "@/lib/user";
import { IngredientNutritionalFactResponse } from "@/types/dto";
import { BaseDialogTexts } from "@/components/dialogs/delete-model";
import { getAlertDialogToggleDisplayTexts } from "@/texts/components/dialog";
import { fetchStream } from "@/hoooks/fetchStream";
import { toast } from "@/components/ui/use-toast";
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
import { cn } from "@/lib/utils";
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

  // const [texts, setTexts] =
  //   useState<AlertDialogToggleDisplayTexts | null>(null);
  //
  // useEffect(() => {
  //   getAlertDialogToggleDisplayTexts(
  //     model.ingredient.name,
  //     model.ingredient.display.toString(),
  //   ).then(setTexts);
  // }, [model.ingredient.display, model.ingredient.name]);
  //
  // const toggle = async () => {
  //   const resp = await fetchStream({
  //     path: `/ingredients/alterDisplay/${model.ingredient.id}`,
  //     method: "PATCH",
  //     token: authUser.token,
  //     queryParams: { display: (!model.ingredient.display).toString() },
  //   });
  //   if (resp.error) {
  //     //todo better error handling
  //     console.log(resp.error);
  //   } else {
  //     callBack();
  //     toast({
  //       title: model.ingredient.name,
  //       description: texts?.toast,
  //       variant: model.ingredient.display ? "destructive" : "success",
  //     });
  //   }
  // };
  //
  // if (!texts) return null;
  //
  // return (
  //   <AlertDialog>
  //     <AlertDialogTrigger asChild>
  //       <Button
  //         variant="outline"
  //         className={cn(
  //           !model.ingredient.display
  //             ? "border-success text-success"
  //             : "border-destructive text-destructive",
  //         )}
  //       >
  //         {texts.anchor}
  //       </Button>
  //     </AlertDialogTrigger>
  //     <AlertDialogContent>
  //       <AlertDialogHeader>
  //         <AlertDialogTitle>{texts.title}</AlertDialogTitle>
  //         <AlertDialogDescription>{texts.description}</AlertDialogDescription>
  //       </AlertDialogHeader>
  //       <AlertDialogFooter>
  //         <AlertDialogCancel>{texts.cancel}</AlertDialogCancel>
  //         <AlertDialogAction asChild onClick={toggle}>
  //           <Button variant="destructive">{texts.confirm}</Button>
  //         </AlertDialogAction>
  //       </AlertDialogFooter>
  //     </AlertDialogContent>
  //   </AlertDialog>
  // );
}
