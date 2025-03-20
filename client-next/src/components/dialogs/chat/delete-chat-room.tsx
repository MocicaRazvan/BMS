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
import {
  DeleteDialogTexts,
  getDeleteChatRoomDialogTexts,
} from "@/texts/components/dialog";
import { useEffect, useState } from "react";

interface Props {
  anchor?: React.ReactNode;
  handleDelete: () => Promise<void>;
  receiverEmail: string;
  deleteChatDialogTexts: DeleteDialogTexts;
}

export default function DeleteChatRoomDialog({
  anchor,
  handleDelete,
  receiverEmail,
  deleteChatDialogTexts,
}: Props) {
  const [isOpen, setIsOpen] = useState(false);
  //todo dc nu merge aici si n altele merge nu stiu si nici nu voi afla, maretele sunt caile lui nextjs

  const [dialogTexts, setDialogDeleteTexts] =
    useState<DeleteDialogTexts | null>(null);
  useEffect(() => {
    getDeleteChatRoomDialogTexts(receiverEmail).then(setDialogDeleteTexts);
  }, [receiverEmail]);

  // console.log("DeleteChatRoomDialog", deleteChatDialogTexts);
  // let description = deleteChatDialogTexts.description;
  // if (typeof description === "string") {
  //   description = description.replace("place_holder", receiverEmail);
  // }
  // console.log(
  //   "deleteChatDialogTexts",
  //   dialogTexts,
  //   JSON.stringify(description),
  // );
  if (!dialogTexts) {
    // if (anchor) return anchor;
    // else return null;
    return;
  }
  return (
    <AlertDialog open={isOpen} onOpenChange={(v) => setIsOpen(v)}>
      <AlertDialogTrigger asChild onClick={(e) => e.stopPropagation()}>
        {anchor ? (
          anchor
        ) : (
          <Button
            variant="outline"
            className="border-destructive text-destructive"
          >
            {dialogTexts.anchor}
          </Button>
        )}
      </AlertDialogTrigger>
      <AlertDialogContent>
        <AlertDialogHeader>
          <AlertDialogTitle>{dialogTexts.title}</AlertDialogTitle>
          <AlertDialogDescription>
            {dialogTexts.description}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel
            onClick={(e) => {
              setIsOpen(false);
              e.stopPropagation();
            }}
          >
            {dialogTexts.cancel}
          </AlertDialogCancel>
          <AlertDialogAction
            asChild
            onClick={async () => {
              handleDelete().finally(() => setIsOpen(false));
            }}
          >
            <Button variant="destructive" onClick={(e) => e.stopPropagation()}>
              {deleteChatDialogTexts.confirm}
            </Button>
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
