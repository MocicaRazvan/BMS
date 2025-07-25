import { useState } from "react";

import { KanbanTask } from "@/components/kanban/kanban-board";
import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { cn } from "@/lib/utils";
import { Card } from "@/components/ui/card";
import { KanbanTaskType } from "@/types/dto";
import { Badge, BadgeVariants } from "@/components/ui/badge";
import DialogKanbanTask, {
  DialogKanbanTaskTexts,
} from "@/components/dialogs/kanban/dialog-kanban-task";
import DeleteKanbanItem, {
  DeleteKanbanItemTexts,
} from "@/components/dialogs/kanban/delete-kanban-item";
import { cva } from "class-variance-authority";

const typeMap: Record<KanbanTaskType, BadgeVariants> = {
  LOW: "default",
  NORMAL: "success",
  URGENT: "destructive",
};

export interface KanbanTaskCardTexts {
  types: Record<KanbanTaskType, string>;
  dialogKanbanTaskTexts: DialogKanbanTaskTexts;
  deleteKanbanItemTexts: DeleteKanbanItemTexts;
}

interface Props extends KanbanTaskCardTexts {
  task: KanbanTask;
  deleteTask: (task: KanbanTask) => Promise<void>;
  updateTask: (
    task: KanbanTask,
    content: string,
    type: KanbanTaskType,
  ) => Promise<void>;
  isOverlay?: boolean;
}

function KanbanTaskCard({
  task,
  deleteTask,
  updateTask,
  isOverlay,
  dialogKanbanTaskTexts,
  deleteKanbanItemTexts,
  types,
}: Props) {
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const {
    setNodeRef,
    attributes,
    listeners,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: task.dndId,
    data: {
      type: "Task",
      task,
    },
    disabled: isDialogOpen,
  });
  const style = {
    transition,
    transform: CSS.Transform.toString(transform),
  };

  const variants = cva(
    `min-h-[120px] flex flex-col gap-2 justify-between  border-2 cursor-grab relative shadow
  p-2 text-left rounded-xl  overflow-y-auto  bg-background hover:scale-[1.02] transition-transform `,
    {
      variants: {
        dragging: {
          over: "ring-2 opacity-35 ",
          overlay: "ring-2 ring-primary/75 ",
        },
      },
    },
  );

  return (
    <Card
      ref={setNodeRef}
      style={style}
      {...attributes}
      {...listeners}
      className={cn(
        task.type === "NORMAL"
          ? "border-success/40"
          : task.type === "URGENT"
            ? "border-destructive/40"
            : "border-border",
        variants({
          dragging: isOverlay ? "overlay" : isDragging ? "over" : undefined,
        }),
      )}
    >
      <p className="whitespace-pre-wrap ">{task.content}</p>
      <div className="flex w-full items-center justify-between border-t pt-1 ">
        <Badge variant={typeMap[task.type]}>{types[task.type]}</Badge>
        <div className="space-x-2 px-2">
          <DialogKanbanTask
            task={task}
            successCallback={(c, t) => updateTask(task, c, t)}
            {...dialogKanbanTaskTexts}
            setOuterOpen={setIsDialogOpen}
          />

          <DeleteKanbanItem
            successCallback={() => deleteTask(task)}
            {...deleteKanbanItemTexts}
            setIsDialogOpen={setIsDialogOpen}
          />
        </div>
      </div>
    </Card>
  );
}
export default KanbanTaskCard;
