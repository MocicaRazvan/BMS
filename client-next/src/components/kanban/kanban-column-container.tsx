"use client";

import { KanbanColumn, KanbanTask } from "@/components/kanban/kanban-board";
import { useCallback, useMemo, useRef, useState } from "react";
import { SortableContext, useSortable } from "@dnd-kit/sortable";
import { CSS } from "@dnd-kit/utilities";
import { Input } from "@/components/ui/input";
import KanbanTaskCard, {
  KanbanTaskCardTexts,
} from "@/components/kanban/kanban-task-card";
import { cn } from "@/lib/utils";
import DialogKanbanTask, {
  DialogKanbanTaskTexts,
} from "@/components/dialogs/kanban/dialog-kanban-task";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { KanbanTaskType } from "@/types/dto";
import DeleteKanbanItem, {
  DeleteKanbanItemTexts,
} from "@/components/dialogs/kanban/delete-kanban-item";
import { cva } from "class-variance-authority";

export interface KanbanColumnContainerTexts {
  kanbanCardTaskTexts: KanbanTaskCardTexts;
  deleteKanbanItemTexts: DeleteKanbanItemTexts;
  dialogKanbanTaskTexts: DialogKanbanTaskTexts;
  tasksLabel: string;
}

interface Props extends KanbanColumnContainerTexts {
  column: KanbanColumn;
  deleteColumn: (column: KanbanColumn) => Promise<void>;
  updateColumn: (column: KanbanColumn, title: string) => Promise<void>;
  createTask: (
    column: KanbanColumn,
    content: string,
    type: KanbanTaskType,
  ) => Promise<void>;
  tasks: KanbanTask[];
  deleteTask: (task: KanbanTask) => Promise<void>;
  updateTask: (
    task: KanbanTask,
    content: string,
    type: KanbanTaskType,
  ) => Promise<void>;
  isOverlay?: boolean;
}

function KanbanColumnContainer({
  column,
  deleteColumn,
  updateColumn,
  createTask,
  tasks,
  deleteTask,
  updateTask,
  isOverlay = false,
  kanbanCardTaskTexts,
  deleteKanbanItemTexts,
  dialogKanbanTaskTexts,
  tasksLabel,
}: Props) {
  const [editMode, setEditMode] = useState(false);
  const initialTitle = useRef(column.title);
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const taskIds = useMemo(() => tasks.map((task) => task.id), [tasks]);

  const [newTitle, setNewTitle] = useState(column.title);

  const {
    setNodeRef,
    attributes,
    listeners,
    transform,
    transition,
    isDragging,
  } = useSortable({
    id: column.dndId,
    data: {
      type: "Column",
      column,
    },
    disabled: editMode || isDialogOpen,
  });

  const style = {
    transition,
    transform: CSS.Transform.toString(transform),
  };

  const handleCreateTask = useCallback(
    async (content: string, type: KanbanTaskType) => {
      if (content.trim() !== "") {
        await createTask(column, content, type);
      }
    },
    [column, createTask],
  );

  const variants = cva(
    "w-full h-[660px] rounded-xl   flex flex-col  shadow-xl bg-primary-foreground border-2 border-transparent snap-center",
    {
      variants: {
        dragging: {
          default: "border-4 border-transparent",
          over: "ring-2 opacity-30 ",
          overlay: "ring-2 ring-primary/75 ",
        },
      },
    },
  );

  return (
    <Card
      ref={setNodeRef}
      style={style}
      className={variants({
        dragging: isOverlay ? "overlay" : isDragging ? "over" : undefined,
      })}
    >
      <CardHeader
        className="flex gap-2 font-semibold cursor-grab min-h-[60px] items-center justify-between  flex-row py-3   px-2"
        {...attributes}
        {...listeners}
      >
        <div
          className={cn(
            "flex items-center  hover:bg-background  rounded cursor-pointer",
            !editMode && "p-2",
          )}
          onClick={() => setEditMode(true)}
        >
          {!editMode && (
            <CardTitle className=" font-bold text-lg lg:text-xl hover:scale-[1.03] ">
              {column.title}
            </CardTitle>
          )}
          {editMode && (
            <Input
              autoFocus
              value={newTitle}
              onChange={(e) => {
                setNewTitle(e.target.value);
              }}
              onBlur={() => setEditMode(false)}
              onKeyDown={async (e) => {
                if (e.key === "Enter") {
                  setEditMode(false);
                  if (newTitle.trim() === "") {
                    await updateColumn(column, initialTitle.current);
                    setNewTitle(initialTitle.current);
                  } else {
                    await updateColumn(column, newTitle);
                    initialTitle.current = newTitle;
                  }
                }
              }}
            />
          )}
        </div>
        <div className="flex items-center justify-center gap-1">
          <div className="bg-muted rounded-md px-2 py-1 text-xs text-muted-foreground">
            {tasks.length} {tasksLabel}
          </div>
          <DeleteKanbanItem
            successCallback={() => deleteColumn(column)}
            trashIconClassName={"h-6 w-6"}
            {...deleteKanbanItemTexts}
            setIsDialogOpen={setIsDialogOpen}
          />
        </div>
      </CardHeader>
      <hr className="border" />
      <CardContent className="flex flex-grow flex-col gap-4 p-3 overflow-x-hidden overflow-y-auto">
        <SortableContext items={taskIds}>
          {tasks.map((task) => (
            <KanbanTaskCard
              key={task.id}
              task={task}
              deleteTask={deleteTask}
              updateTask={updateTask}
              {...kanbanCardTaskTexts}
            />
          ))}
        </SortableContext>
      </CardContent>
      <DialogKanbanTask
        successCallback={handleCreateTask}
        {...dialogKanbanTaskTexts}
      />
    </Card>
  );
}

export default KanbanColumnContainer;
