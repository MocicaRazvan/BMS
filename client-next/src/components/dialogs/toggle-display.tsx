"use client";

import { WithUser } from "@/lib/user";
import { BaseDialogTexts } from "@/components/dialogs/delete-model";
import { getAlertDialogToggleDisplayTexts } from "@/texts/components/dialog";
import { fetchStream } from "@/lib/fetchers/fetchStream";
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
import LoadingDialogAnchor from "@/components/dialogs/loading-dialog-anchor";
import { useClientLRUStore } from "@/lib/client-lru-store";
import { useLocale } from "next-intl";
import { ReactNode } from "react";

export interface AlertDialogToggleDisplayTexts extends BaseDialogTexts {
  toast: string | ReactNode;
}

interface Props extends WithUser {
  model: {
    name: string;
    display: boolean;
    id: number;
  };
  path: string;
  callBack: () => void;
}

const ToggleDisplayDialog = ({ callBack, model, path, authUser }: Props) => {
  const locale = useLocale();

  const texts = useClientLRUStore({
    setter: () =>
      getAlertDialogToggleDisplayTexts(model.name, model.display.toString()),
    args: [
      `alertDialogToggleDisplayTexts-${model.name}-${model.display}`,
      locale,
    ],
  });

  const toggle = async () => {
    const resp = await fetchStream({
      path,
      method: "PATCH",
      token: authUser.token,
      queryParams: { display: (!model.display).toString() },
    });
    if (resp.error) {
      //todo better error handling
      console.log(resp.error);
    } else {
      callBack();
      toast({
        title: model.name,
        description: texts?.toast,
        variant: model.display ? "destructive" : "success",
      });
    }
  };

  if (!texts) return <LoadingDialogAnchor className="w-full h-full" />;

  return (
    <AlertDialog>
      <AlertDialogTrigger asChild>
        <Button
          variant="outline"
          className={cn(
            "w-full",
            !model.display
              ? "border-success text-success"
              : "border-destructive text-destructive",
          )}
        >
          {texts.anchor}
        </Button>
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{texts.title}</AlertDialogTitle>
          <AlertDialogDescription asChild>
            <div>{texts.description}</div>
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>{texts.cancel}</AlertDialogCancel>
          <AlertDialogAction asChild onClick={toggle}>
            <Button variant="destructive">{texts.confirm}</Button>
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
};

export default ToggleDisplayDialog;
