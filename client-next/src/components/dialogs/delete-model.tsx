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
import { fetchStream } from "@/hoooks/fetchStream";
import { TitleBodyUserDto } from "@/types/dto";
import { toast } from "@/components/ui/use-toast";
import { ReactNode, useEffect, useState } from "react";
import {
  DeleteDialogTexts,
  getAlertDialogDeleteTexts,
} from "@/texts/components/dialog";

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

export function AlertDialogDelete({
  model,
  token,
  callBack,
  path,
  title,
  anchor,
}: Props) {
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
}
