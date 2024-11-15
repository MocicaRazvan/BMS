"use client";

import { memo, ReactNode, useEffect, useState } from "react";
import { WithUser } from "@/lib/user";
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
import { cn, isDeepEqual } from "@/lib/utils";
import LoadingDialogAnchor from "@/components/dialogs/loading-dialog-anchor";

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

const ToggleDisplayDialog = memo(
  ({ callBack, model, path, authUser }: Props) => {
    const [texts, setTexts] = useState<AlertDialogToggleDisplayTexts | null>(
      null,
    );

    useEffect(() => {
      getAlertDialogToggleDisplayTexts(
        model.name,
        model.display.toString(),
      ).then(setTexts);
    }, [model.display, model.name]);

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

    if (!texts) return <LoadingDialogAnchor />;

    return (
      <AlertDialog>
        <AlertDialogTrigger asChild>
          <Button
            variant="outline"
            className={cn(
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
            <AlertDialogDescription>{texts.description}</AlertDialogDescription>
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
  },
  ({ callBack: pc, ...prevProps }, { callBack: nc, ...nextProps }) =>
    isDeepEqual(prevProps, nextProps),
);

ToggleDisplayDialog.displayName = "ToggleDisplayDialog";

export default ToggleDisplayDialog;
