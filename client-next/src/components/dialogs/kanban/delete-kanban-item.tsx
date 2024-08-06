"use client";
import {
  Dialog,
  DialogTrigger,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog";
import { Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import { ReactNode, useCallback, useState } from "react";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import ErrorMessage from "@/components/forms/error-message";

export interface DeleteKanbanItemTexts {
  title: string;
  description: string | ReactNode;
  buttonSubmitTexts: ButtonSubmitTexts;
  error: string;
  cancel: string;
  confirm: string;
}
interface Props extends DeleteKanbanItemTexts {
  successCallback: () => Promise<void>;
  trashIconClassName?: string;
  setIsDialogOpen: (isOpen: boolean) => void;
}

export default function DeleteKanbanItem({
  successCallback,
  trashIconClassName,
  error,
  description,
  title,
  buttonSubmitTexts,
  cancel,
  confirm,
  setIsDialogOpen,
}: Props) {
  const { setErrorMsg, errorMsg, router, isLoading, setIsLoading } =
    useLoadingErrorState();

  const handleSubmit = useCallback(async () => {
    setErrorMsg("");
    setIsLoading(true);
    successCallback()
      .then(() => {
        setErrorMsg("");
      })
      .catch(() => setErrorMsg(error))
      .finally(() => setIsLoading(false));
  }, [error, setErrorMsg, setIsLoading, successCallback]);

  return (
    <Dialog onOpenChange={setIsDialogOpen}>
      <DialogTrigger asChild>
        <Button
          size={"icon"}
          variant={"ghost"}
          onClick={(e) => {
            e.stopPropagation();
          }}
        >
          <Trash2 className={cn("h-4 w-4", trashIconClassName)} />
        </Button>
      </DialogTrigger>
      <DialogContent
        className="sm:max-w-md"
        onMouseMove={(e) => {
          e.stopPropagation();
        }}
      >
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>

        <ErrorMessage message={errorMsg} show={!!errorMsg} />

        <DialogFooter className="sm:justify-end gap-3">
          <DialogClose asChild>
            <Button type="button" variant="secondary" className="mt-2">
              {cancel}
            </Button>
          </DialogClose>
          <DialogClose asChild>
            <ButtonSubmit
              isLoading={isLoading}
              disable={false}
              size={"default"}
              buttonSubmitTexts={{ ...buttonSubmitTexts, submitText: confirm }}
              onClick={handleSubmit}
            />
          </DialogClose>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
