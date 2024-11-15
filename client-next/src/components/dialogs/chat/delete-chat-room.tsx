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
}

export default function DeleteChatRoomDialog({
  anchor,
  handleDelete,
  receiverEmail,
}: Props) {
  const [dialogDeleteTexts, setDialogDeleteTexts] =
    useState<DeleteDialogTexts | null>(null);
  useEffect(() => {
    getDeleteChatRoomDialogTexts(receiverEmail).then(setDialogDeleteTexts);
  }, [receiverEmail]);

  if (!dialogDeleteTexts) {
    if (anchor) return anchor;
    else return null;
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
          <AlertDialogTitle>{dialogDeleteTexts.title}</AlertDialogTitle>
          <AlertDialogDescription>
            {dialogDeleteTexts.description}
          </AlertDialogDescription>
        </AlertDialogHeader>
        <AlertDialogFooter>
          <AlertDialogCancel>{dialogDeleteTexts.cancel}</AlertDialogCancel>
          <AlertDialogAction asChild onClick={() => handleDelete()}>
            <Button variant="destructive">{dialogDeleteTexts.confirm}</Button>
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
