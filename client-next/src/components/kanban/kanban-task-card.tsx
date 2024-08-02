import { memo } from "react";

import { KanbanTask } from "@/components/kanban/kanban-board";
import { useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { cn, isDeepEqual } from "@/lib/utils";
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
  });
  const style = {
    transition,
    transform: CSS.Transform.toString(transform),
  };

  // if (isDragging) {
  //   return (
  //     <div
  //       ref={setNodeRef}
  //       style={style}
  //       className="min-h-[100px] rounded  flex flex-col  shadow bg-accent bg-opacity-60 border-2 border-destructive/75
  //     "
  //     ></div>
  //   );
  // }
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
          />

          <DeleteKanbanItem
            successCallback={() => deleteTask(task)}
            {...deleteKanbanItemTexts}
          />
        </div>
      </div>
    </Card>
  );
}
export default memo(
  KanbanTaskCard,
  (prevProps, nextProps) =>
    prevProps.deleteTask === nextProps.deleteTask &&
    prevProps.updateTask === prevProps.updateTask &&
    isDeepEqual(prevProps.task, nextProps.task),
);
