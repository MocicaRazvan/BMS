"use client";

import {
  Dialog,
  DialogTrigger,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { memo, useCallback, useEffect, useRef, useState } from "react";
import { isDeepEqual } from "@/lib/utils";
import { CirclePlus, FilePen } from "lucide-react";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { KanbanTaskType } from "@/types/dto";
import { KanbanTask } from "@/components/kanban/kanban-board";
import { Textarea } from "@/components/ui/textarea";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import ErrorMessage from "@/components/forms/error-message";

export interface DialogKanbanTaskTexts {
  title: string;
  description: string;
  addTask: string;
  error: string;
  inputs: Record<
    "content" | "type",
    {
      label: string;
      placeholder: string;
    }
  >;
  types: Record<KanbanTaskType, string>;
  buttonSubmitTexts: ButtonSubmitTexts;
}
interface Props extends DialogKanbanTaskTexts {
  successCallback: (content: string, type: KanbanTaskType) => Promise<void>;
  task?: KanbanTask;
}

function DialogKanbanTask({
  successCallback,
  task,
  error,
  description,
  buttonSubmitTexts,
  inputs,
  addTask,
  title,
  types,
}: Props) {
  const [taskContent, setTaskContent] = useState(task?.content || "");
  const [isOpen, setIsOpen] = useState(false);
  const [taskType, setTaskType] = useState<KanbanTaskType>(task?.type || "LOW");
  const textAreaRef = useRef<HTMLTextAreaElement>(null);
  const { setErrorMsg, errorMsg, router, isLoading, setIsLoading } =
    useLoadingErrorState();

  const handleCleanup = useCallback(() => {
    if (task?.id) return;
    setTaskContent("");
    setTaskType("LOW");
  }, [task?.id]);

  const handleSubmit = useCallback(
    (content: string) => {
      setErrorMsg("");
      if (!content) return;
      setIsLoading(true);
      successCallback(content, taskType)
        .then(() => {
          setErrorMsg("");
          setIsOpen(false);
          handleCleanup();
        })
        .catch(() => setErrorMsg(error))
        .finally(() => setIsLoading(false));
    },
    [
      error,
      handleCleanup,
      setErrorMsg,
      setIsLoading,
      successCallback,
      taskType,
    ],
  );

  useEffect(() => {
    if (textAreaRef.current && task && task.content) {
      const length = task.content.length;
      textAreaRef.current.setSelectionRange(length, length);
      textAreaRef.current.focus();
    }
  }, [task, textAreaRef.current]);

  return (
    <Dialog open={isOpen} onOpenChange={setIsOpen}>
      <DialogTrigger asChild>
        {!task ? (
          <Button
            size="lg"
            className="flex items-center justify-center gap-2 w-2/3 mx-auto"
            variant="outline"
          >
            <CirclePlus /> {addTask}
          </Button>
        ) : (
          <Button size={"icon"} variant={"ghost"}>
            <FilePen className="h-4 w-4" />{" "}
          </Button>
        )}
      </DialogTrigger>
      <DialogContent className="sm:max-w-[570px]">
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description} </DialogDescription>
        </DialogHeader>
        <div className="grid gap-4 py-4">
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="content" className="text-right">
              {inputs.content.label}
            </Label>
            <Textarea
              ref={textAreaRef}
              id="content"
              value={taskContent}
              placeholder={inputs.content.placeholder}
              className="col-span-3 resize-none "
              onChange={(e) => {
                e.stopPropagation();
                setTaskContent(e.target.value);
              }}
            />
          </div>
          <div className="grid grid-cols-4 items-center gap-4">
            <Label htmlFor="type" className="text-right">
              {inputs.type.label}
            </Label>
            <Select
              value={taskType}
              onValueChange={(value) => setTaskType(value as KanbanTaskType)}
            >
              <SelectTrigger
                id={"type"}
                className="col-span-3"
                value={taskType}
              >
                <SelectValue />
              </SelectTrigger>
              <SelectContent id={"type"}>
                <SelectGroup>
                  <SelectItem className="cursor-pointer" value="LOW">
                    {types.LOW}
                  </SelectItem>
                  <SelectItem className="cursor-pointer" value="NORMAL">
                    {types.NORMAL}
                  </SelectItem>
                  <SelectItem className="cursor-pointer" value="URGENT">
                    {types.URGENT}
                  </SelectItem>
                </SelectGroup>
              </SelectContent>
            </Select>
          </div>
        </div>

        <DialogFooter>
          <ErrorMessage message={errorMsg} show={!!errorMsg} />
          <ButtonSubmit
            isLoading={isLoading}
            disable={taskContent.trim() === ""}
            onClick={() => handleSubmit(taskContent.trim())}
            buttonSubmitTexts={buttonSubmitTexts}
          />
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default memo(DialogKanbanTask, (p, n) => isDeepEqual(p, n));
