"use client";

import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { useCallback, useState } from "react";
import { CirclePlus } from "lucide-react";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import ErrorMessage from "@/components/forms/error-message";

export interface AddKanbanColumnTexts {
  addColumn: string;
  title: string;
  description: string;
  titleLabel: string;
  titlePlaceholder: string;
  buttonSubmitTexts: ButtonSubmitTexts;
  error: string;
}

interface Props extends AddKanbanColumnTexts {
  successCallback: (title: string) => Promise<void>;
}

export default function AddKanbanColumn({
  successCallback,
  addColumn,
  error,
  description,
  titleLabel,
  titlePlaceholder,
  title,
  buttonSubmitTexts,
}: Props) {
  const [columnTitle, setColumnTitle] = useState("");
  const [isOpen, setIsOpen] = useState(false);
  const { setErrorMsg, errorMsg, router, isLoading, setIsLoading } =
    useLoadingErrorState();

  const handleSubmit = useCallback(
    (title: string) => {
      setErrorMsg("");
      setIsLoading(true);
      if (!title) return;
      successCallback(title)
        .then(() => {
          setIsOpen(false);
          setErrorMsg("");
          setColumnTitle("");
        })
        .catch(() => setErrorMsg(error))
        .finally(() => setIsLoading(false));
    },
    [error, setErrorMsg, setIsLoading, successCallback],
  );

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        <Button size="lg" className="flex items-center justify-center gap-2">
          <CirclePlus /> {addColumn}
        </Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[570px]">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="title" className="text-right">
              {titleLabel}
            </Label>
            <Input
              id="title"
              value={columnTitle}
              className="col-span-3"
              placeholder={titlePlaceholder}
              onChange={(e) => setColumnTitle(e.target.value)}
            />
          </div>
        </div>

        <DialogFooter className="space-y-5">
          <ErrorMessage message={errorMsg} show={!!errorMsg} />
          <ButtonSubmit
            isLoading={isLoading}
            disable={columnTitle.trim() === ""}
            onClick={() => handleSubmit(columnTitle.trim())}
            buttonSubmitTexts={buttonSubmitTexts}
          />
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
