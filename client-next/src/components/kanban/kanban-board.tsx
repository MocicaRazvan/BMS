"use client";

import { ReactNode, useCallback, useEffect, useMemo, useState } from "react";
import KanbanColumnContainer, {
  KanbanColumnContainerTexts,
} from "@/components/kanban/kanban-column-container";
import { AnimatePresence, motion } from "framer-motion";
import {
  DndContext,
  DragEndEvent,
  DragOverEvent,
  DragOverlay,
  DragStartEvent,
  PointerSensor,
  TouchSensor,
  useSensor,
  useSensors,
} from "@dnd-kit/core";
import { arrayMove, SortableContext } from "@dnd-kit/sortable";
import { createPortal } from "react-dom";
import KanbanTaskCard, {
  KanbanTaskCardTexts,
} from "@/components/kanban/kanban-task-card";
import {
  CustomEntityModel,
  KanbanColumnBody,
  KanbanColumnResponse,
  KanbanTaskBody,
  KanbanTaskResponse,
  KanbanTaskType,
} from "@/types/dto";
import { WithUser } from "@/lib/user";
import AddKanbanColumn, {
  AddKanbanColumnTexts,
} from "@/components/dialogs/kanban/add-kanban-column";
import { fetchStream } from "@/hoooks/fetchStream";
import {
  createDndIdColumn,
  createDndIdTask,
} from "@/components/kanban/kanban-board-wrapper";
import { useDebounce } from "@/components/ui/multiple-selector";
import { usePathname } from "@/navigation";
import {
  ReindexState,
  useKanbanRouteChange,
} from "@/context/kanban-route-change-context";

export interface KanbanTask extends KanbanTaskResponse {
  dndId: string;
}
export interface KanbanColumn extends KanbanColumnResponse {
  dndId: string;
}

let counter = 0;

export type GroupedKanbanTasks = Record<number, KanbanTask[]>;

export interface KanbanBoardTexts {
  kanbanCardTaskTexts: KanbanTaskCardTexts;
  kanbanColumnContainerTexts: KanbanColumnContainerTexts;
  addKanbanColumnTexts: AddKanbanColumnTexts;
}

interface Props extends WithUser, KanbanBoardTexts {
  initialColumns: KanbanColumn[];
  initialGroupedTasks: GroupedKanbanTasks;
}
export default function KanbanBoard({
  authUser,
  initialGroupedTasks,
  initialColumns,
  addKanbanColumnTexts,
  kanbanColumnContainerTexts,
  kanbanCardTaskTexts,
}: Props) {
  const [columns, setColumns] = useState<KanbanColumn[]>([]);
  const [groupedTasks, setGroupedTasks] = useState<GroupedKanbanTasks>([]);

  const handleUpdate = useCallback(
    (rs: ReindexState) => {
      if (rs.task > 0) {
        fetchStream({
          path: "/kanban/task/reindex",
          method: "POST",
          token: authUser.token,
          body: { groupedTasks },
        }).catch((e) => console.log("REINDEX TASK ERROR", e));
      }

      if (rs.column > 0) {
        fetchStream({
          path: "/kanban/column/reindex",
          method: "POST",
          token: authUser.token,
          body: { columns },
        }).catch((e) => console.log("REINDEX COLUMN ERROR", e));
      }
      console.log("REINDEX FUNCTION CALL1", rs);
    },
    [authUser.token, JSON.stringify(columns), JSON.stringify(groupedTasks)],
  );

  const { reindexState, setReindexState } = useKanbanRouteChange(handleUpdate);
  const debounceReindex = useDebounce(reindexState, 1000);

  const [activeColumn, setActiveColumn] = useState<KanbanColumn | null>(null);
  const [activeTask, setActiveTask] = useState<KanbanTask | null>(null);

  const [isDndActive, setIsDndActive] = useState(false);

  console.log("counting re-renders", (counter += 1));
  console.log("REINDEX STATE", reindexState);

  useEffect(() => {
    setColumns(initialColumns);
  }, [initialColumns]);

  useEffect(() => {
    setGroupedTasks(initialGroupedTasks);
  }, [initialGroupedTasks]);

  const sensors = useSensors(
    useSensor(PointerSensor, {
      activationConstraint: {
        distance: 30,
      },
    }),
    useSensor(TouchSensor, {
      activationConstraint: {
        distance: 30,
      },
    }),
  );

  console.log(columns);

  const setColumnsOrdered = useCallback(
    (updater: (prev: typeof columns) => typeof columns) => {
      setColumns((prev) =>
        updater(prev).map((column, index) => ({
          ...column,
          orderIndex: index,
        })),
      );

      setReindexState((prev) => ({ ...prev, column: new Date().getTime() }));
    },
    [],
  );

  const setGroupedTasksOrdered = useCallback(
    (updater: (prev: typeof groupedTasks) => typeof groupedTasks) => {
      setGroupedTasks((prev) => {
        const updatedTasks = updater(prev);

        return Object.keys(updatedTasks).reduce(
          (acc, key) => {
            console.log("TASKS KEY", key, typeof Number(key));
            if (updatedTasks[Number(key)]?.length == 0) return acc;
            acc[Number(key)] = updatedTasks[Number(key)].map((task, index) => ({
              ...task,
              orderIndex: index,
            }));
            return acc;
          },
          {} as Record<number, KanbanTask[]>,
        );
      });

      setReindexState((prev) => ({ ...prev, task: new Date().getTime() }));
    },
    [],
  );

  console.log("GROUPED TASKS", groupedTasks);
  console.log("GROUPED COLUMNS", columns);

  const columnIds = useMemo(
    () => columns.map((column) => column.id),
    [columns],
  );

  const createNewColumn = useCallback(
    async (title: string) => {
      if (!title) return;

      const body: KanbanColumnBody = {
        orderIndex: columns.length,
        title,
      };

      try {
        const { messages, error } = await fetchStream<
          CustomEntityModel<KanbanColumnResponse>
        >({
          path: "/kanban/column/create",
          token: authUser.token,
          method: "POST",
          body,
        });

        if (messages.length > 0) {
          setColumns((prev) => [
            ...prev,
            {
              ...messages[0].content,
              dndId: createDndIdColumn(messages[0].content.id),
            },
          ]);
        }
        if (error) {
          console.log("Error", error);
        }
      } catch (e) {
        console.log("Error", e);
      }
    },
    [authUser.token, columns.length],
  );

  const deleteColumn = useCallback(
    async (column: KanbanColumn) => {
      try {
        const { messages, error } = await fetchStream<
          CustomEntityModel<KanbanColumnResponse>
        >({
          path: `/kanban/column/delete/${column.id}`,
          token: authUser.token,
          method: "DELETE",
        });

        if (messages.length > 0) {
          setColumnsOrdered((prev) => prev.filter((c) => c.id !== column.id));
          setGroupedTasks((prev) => {
            const copy = { ...prev };
            delete copy[column.id];
            return copy;
          });
        }

        if (error) {
          console.log("Error", error);
        }
      } catch (e) {
        console.log("Error", e);
      }
    },
    [authUser.token, setColumnsOrdered],
  );

  const updateColumn = useCallback(
    async (column: KanbanColumn, title: string) => {
      if (!title) return;
      const body: KanbanColumnBody = {
        title,
        orderIndex: column.orderIndex,
      };
      try {
        const { messages, error } = await fetchStream<
          CustomEntityModel<KanbanColumnResponse>
        >({
          path: `/kanban/column/update/${column.id}`,
          body,
          method: "PUT",
          token: authUser.token,
        });

        if (messages.length > 0) {
          setColumns((prev) =>
            prev.map((c) =>
              c.id === column.id
                ? {
                    ...c,
                    title: messages[0].content.title,
                    updatedAt: messages[0].content.updatedAt,
                  }
                : c,
            ),
          );
        }

        if (error) {
          console.log("Error", error);
        }
      } catch (e) {
        console.log("Error", e);
      }
    },
    [authUser.token],
  );

  const createTask = useCallback(
    async (column: KanbanColumn, content: string, type: KanbanTaskType) => {
      const task: KanbanTaskBody = {
        type,
        content,
        columnId: column.id,
        orderIndex: groupedTasks[column.id]?.length || 0,
      };
      try {
        const { messages, error } = await fetchStream<
          CustomEntityModel<KanbanTaskResponse>
        >({
          path: "/kanban/task/create",
          token: authUser.token,
          method: "POST",
          body: task,
        });

        if (messages.length > 0) {
          setGroupedTasks((prev) => ({
            ...prev,
            [column.id]: [
              ...(prev[column.id] || []),
              {
                ...messages[0].content,
                dndId: createDndIdTask(messages[0].content.id),
              },
            ],
          }));
        }
        if (error) {
          console.log("Error", error);
        }
      } catch (e) {
        console.log("Error", e);
      }
    },
    [authUser.token, groupedTasks],
  );

  const deleteTask = useCallback(
    async (task: KanbanTask) => {
      try {
        const { messages, error } = await fetchStream<
          CustomEntityModel<KanbanTaskResponse>
        >({
          path: `/kanban/task/delete/${task.id}`,
          method: "DELETE",
          token: authUser.token,
        });

        if (messages.length > 0) {
          setGroupedTasksOrdered((prev) => ({
            ...prev,
            [messages[0].content.columnId]: prev[
              messages[0].content.columnId
            ].filter((t) => t.id !== messages[0].content.id),
          }));
        }

        if (error) {
          console.log("Error", error);
        }
      } catch (e) {
        console.log("Error", e);
      }
    },
    [authUser.token, setGroupedTasksOrdered],
  );

  const updateTask = useCallback(
    async (task: KanbanTask, content: string, type: KanbanTaskType) => {
      try {
        const body: KanbanTaskBody = {
          type,
          content,
          columnId: task.columnId,
          orderIndex: task.orderIndex,
        };

        const { messages, error } = await fetchStream<
          CustomEntityModel<KanbanTaskResponse>
        >({
          path: `/kanban/task/update/${task.id}`,
          method: "PUT",
          token: authUser.token,
          body,
        });

        if (messages.length > 0) {
          setGroupedTasksOrdered((prev) => ({
            ...prev,
            [messages[0].content.columnId]: prev[
              messages[0].content.columnId
            ].map((t) =>
              t.id === messages[0].content.id
                ? { ...t, ...messages[0].content }
                : t,
            ),
          }));
        }
      } catch (e) {
        console.log("Error", e);
      }
    },
    [authUser.token, setGroupedTasksOrdered],
  );

  const onDragStart = useCallback((e: DragStartEvent) => {
    setIsDndActive(true);
    if (e.active.data.current?.type === "Column") {
      setActiveColumn(e.active.data.current.column);
    } else if (e.active.data.current?.type === "Task") {
      setActiveTask(e.active.data.current.task);
    }
  }, []);

  const onDragEnd = useCallback(
    (e: DragEndEvent) => {
      setActiveColumn(null);
      setActiveTask(null);
      const { active, over } = e;
      if (!over) return;

      if (active.data.current?.type === "Task") return;

      const activeColumnId = active.id;
      const overColumnId = over.id;

      if (activeColumnId === overColumnId) return;

      const activeColumn = active.data?.current?.column;
      const overColumn = over.data.current?.column;

      setColumnsOrdered((prev) => {
        const activeColumnIndex = prev.findIndex(
          (c) => c.id === activeColumn.id,
        );
        const overColumnIndex = prev.findIndex((c) => c.id === overColumn.id);
        return arrayMove(prev, activeColumnIndex, overColumnIndex);
      });

      setIsDndActive(false);
    },
    [setColumnsOrdered],
  );

  const onDragOver = useCallback(
    (e: DragOverEvent) => {
      const { active, over } = e;
      if (!over) return;

      const activeId = active.id;
      const overId = over.id;

      if (activeId === overId) return;

      const isActiveATask = active.data?.current?.type === "Task";
      const isOverATask = over.data?.current?.type === "Task";

      if (!isActiveATask) return;

      const activeTask = active.data.current?.task as KanbanTask;
      const overTask = over.data.current?.task as KanbanTask;

      if (isActiveATask && isOverATask) {
        setGroupedTasksOrdered((prev) => {
          if (activeTask.id === overTask.id) return prev;
          if (activeTask.columnId === overTask.columnId) {
            const activeTaskIndex = prev[activeTask.columnId].findIndex(
              (t) => t.id === activeTask.id,
            );
            const overTaskIndex = prev[overTask.columnId].findIndex(
              (t) => t.id === overTask.id,
            );
            return {
              ...prev,
              [activeTask.columnId]: arrayMove(
                prev[activeTask.columnId] || [],
                activeTaskIndex,
                overTaskIndex,
              ),
            };
          } else {
            const overIndex = prev[overTask.columnId].findIndex(
              (t) => t.id === overTask.id,
            );
            return {
              ...prev,
              [activeTask.columnId]: prev[activeTask.columnId].filter(
                (t) => t.id !== activeTask.id,
              ),
              [overTask.columnId]: [
                ...prev[overTask.columnId].slice(0, overIndex),
                { ...activeTask, columnId: overTask.columnId },
                ...prev[overTask.columnId].slice(overIndex),
              ],
            };
          }
        });
      }

      const isOverAColumn = over.data?.current?.type === "Column";

      if (isActiveATask && isOverAColumn) {
        const overColumn = over.data.current?.column as KanbanColumn;
        if (activeTask.columnId === overColumn.id) return;

        setGroupedTasksOrdered((prev) => ({
          ...prev,
          [activeTask.columnId]: prev[activeTask.columnId].filter(
            (t) => t.id !== activeTask.id,
          ),
          [overColumn.id]: [
            ...(prev[overColumn.id] || []),
            { ...activeTask, columnId: overColumn.id },
          ],
        }));
      }
    },
    [setGroupedTasksOrdered],
  );

  const sortableCols: ReactNode = useMemo(
    () =>
      columns.map((column) => (
        <motion.div
          className="col-span-1 w-full z-1"
          key={column.id + "col"}
          initial={{ opacity: 0, scale: 0 }}
          animate={{ opacity: 1, scale: 1 }}
          layout={!isDndActive}
          exit={{
            opacity: 0,
            scale: 0,
            transition: {
              duration: 0.35,
            },
          }}
          transition={{
            type: "spring",
            stiffness: 82.5,
            duration: 0.4,
            damping: 12,
          }}
        >
          <KanbanColumnContainer
            column={column}
            deleteColumn={deleteColumn}
            updateColumn={updateColumn}
            createTask={createTask}
            tasks={groupedTasks[column.id] || []}
            deleteTask={deleteTask}
            updateTask={updateTask}
            {...kanbanColumnContainerTexts}
          />
        </motion.div>
      )),
    [
      columns,
      createTask,
      deleteColumn,
      deleteTask,
      groupedTasks,
      isDndActive,
      kanbanColumnContainerTexts,
      updateColumn,
      updateTask,
    ],
  );

  return (
    <section className="mx-auto min-h-screen w-full px-12 pt-10">
      <div className="w-full flex justify-end">
        <AddKanbanColumn
          successCallback={createNewColumn}
          {...addKanbanColumnTexts}
        />
      </div>
      <DndContext
        onDragStart={onDragStart}
        onDragEnd={onDragEnd}
        sensors={sensors}
        onDragOver={onDragOver}
      >
        <div className="flex items-center mt-14">
          <div className="mx-auto flex gap-4 w-full max-w-[1500px]">
            <div className=" px-3 py-5 w-full grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-10 place-content-center ">
              <SortableContext items={columnIds}>
                <AnimatePresence>{sortableCols}</AnimatePresence>
              </SortableContext>
            </div>
          </div>
        </div>
        {typeof window !== "undefined" &&
          document?.body &&
          createPortal(
            <DragOverlay>
              {activeColumn && (
                <KanbanColumnContainer
                  column={activeColumn}
                  deleteColumn={deleteColumn}
                  updateColumn={updateColumn}
                  createTask={createTask}
                  tasks={groupedTasks[activeColumn.id] || []}
                  deleteTask={deleteTask}
                  updateTask={updateTask}
                  isOverlay
                  {...kanbanColumnContainerTexts}
                />
              )}
              {activeTask && (
                <KanbanTaskCard
                  task={activeTask}
                  deleteTask={deleteTask}
                  updateTask={updateTask}
                  isOverlay
                  {...kanbanCardTaskTexts}
                />
              )}
            </DragOverlay>,
            document.body,
          )}
      </DndContext>
    </section>
  );
}
