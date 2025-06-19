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
import { ReactNode } from "react";
import { getAlertDialogDeleteTexts } from "@/texts/components/dialog";
import LoadingDialogAnchor from "@/components/dialogs/loading-dialog-anchor";
import { useClientLRUStore } from "@/lib/client-lru-store";
import { useLocale } from "next-intl";

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
  anchor?: ReactNode;
}
const AlertDialogDelete = ({
  model,
  token,
  callBack,
  path,
  title,
  anchor,
}: Props) => {
  const locale = useLocale();

  const dialogDeleteTexts = useClientLRUStore({
    setter: () => getAlertDialogDeleteTexts(title),
    args: [`alertDialogDeleteTexts-${title}-${path}`, locale],
  });
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
  if (!dialogDeleteTexts) return <LoadingDialogAnchor className="w-full" />;

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
          <AlertDialogDescription asChild>
            <div>{dialogDeleteTexts.description}</div>
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
};

AlertDialogDelete.displayName = "AlertDialogDelete";
export { AlertDialogDelete };
