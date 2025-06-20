import { CommentResponse } from "@/types/dto";
import { ReactNode } from "react";
import { getAlertDialogDeleteTexts } from "@/texts/components/dialog";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { toast } from "@/components/ui/use-toast";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { useClientLRUStore } from "@/lib/client-lru-store";
import { useLocale } from "next-intl";

interface Props {
  comment: CommentResponse;
  token: string | undefined;
  callBack: () => void;
  anchor?: ReactNode;
  title: string;
}

export default function AlertDialogDeleteComment({
  comment,
  token,
  callBack,
  anchor,
  title,
}: Props) {
  const locale = useLocale();

  const dialogDeleteTexts = useClientLRUStore({
    setter: () => getAlertDialogDeleteTexts(title),
    args: [`alertDialogDeleteTexts-${title}-comments`, locale],
  });
  const deleteModel = async () => {
    if (token === undefined) return;
    try {
      const resp = await fetchStream({
        path: `/comments/delete/${comment.id}`,
        method: "DELETE",
        token,
      });

      if (resp.error) {
        console.log(resp.error);
      } else {
        toast({
          // title: model.title,
          description: "Deleted",
          variant: "destructive",
        });
        callBack?.();
      }
    } catch (error) {
      console.log(error);
    }
  };
  if (!dialogDeleteTexts) {
    if (anchor) {
      return anchor;
    }
    return;
  }

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
          <AlertDialogTitle>{title}</AlertDialogTitle>
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
