"use client";
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
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { TitleBodyUserDto } from "@/types/dto";
import { toast } from "@/components/ui/use-toast";
import { memo, ReactNode, useEffect, useState } from "react";
import {
  DeleteDialogTexts,
  getAlertDialogDeleteTexts,
} from "@/texts/components/dialog";
import LoadingDialogAnchor from "@/components/dialogs/loading-dialog-anchor";
import { isDeepEqual } from "@/lib/utils";

export interface BaseDialogTexts {
  anchor: string;
  title: string;
  description: string | ReactNode;
  cancel: string;
  confirm: string;
}

interface Props {
  model: TitleBodyUserDto;
  token: string | undefined;
  path: string;
  title: string;
  callBack: () => void;
  anchor?: React.ReactNode;
}

const AlertDialogDelete = memo(
  ({ model, token, callBack, path, title, anchor }: Props) => {
    const [dialogDeleteTexts, setDialogDeleteTexts] =
      useState<DeleteDialogTexts | null>(null);

    useEffect(() => {
      getAlertDialogDeleteTexts(title).then(setDialogDeleteTexts);
    }, [title]);
    const deleteModel = async () => {
      if (token === undefined) return;
      try {
        const resp = await fetchStream({
          path: `/${path}/delete/${model.id}`,
          method: "DELETE",
          token,
        });

        if (resp.error) {
          console.log(resp.error);
        } else {
          toast({
            title: model.title,
            description: "Deleted",
            variant: "destructive",
          });
          callBack?.();
        }
      } catch (error) {
        console.log(error);
      }
    };
    if (!dialogDeleteTexts) return <LoadingDialogAnchor />;

    return (
      <AlertDialog>
        <AlertDialogTrigger asChild>
          {anchor ? (
            anchor
          ) : (
            <Button
              variant="outline"
              className="border-destructive text-destructive w-full"
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
  ({ callBack: pc, ...prevProps }, { callBack: nc, ...nextProps }) =>
    isDeepEqual(prevProps, nextProps),
);

AlertDialogDelete.displayName = "AlertDialogDelete";
export { AlertDialogDelete };
