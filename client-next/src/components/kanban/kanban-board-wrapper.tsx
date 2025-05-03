"use client";

import { WithUser } from "@/lib/user";
import useFetchStream from "@/hoooks/useFetchStream";
import { BaseError } from "@/types/responses";
import {
  CustomEntityModel,
  KanbanColumnResponse,
  KanbanTaskResponse,
} from "@/types/dto";
import { useEffect, useMemo, useState } from "react";
import KanbanBoard, {
  GroupedKanbanTasks,
  KanbanBoardTexts,
  KanbanColumn,
} from "@/components/kanban/kanban-board";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import LoadingSpinner from "@/components/common/loading-spinner";
import { motion } from "framer-motion";

interface Props extends WithUser, KanbanBoardTexts {}
export const createDndIdColumn = (id: number) => `column-${id}`;
export const createDndIdTask = (id: number) => `task-${id}`;

export default function KanbanBoardWrapper({ authUser, ...props }: Props) {
  const [columnsIds, setColumnsIds] = useState<number[]>([]);
  const [groupedTasks, setGroupedTasks] = useState<GroupedKanbanTasks>({});
  const [groupedTasksFinished, setGroupedTasksFinished] = useState<
    Record<number, boolean>
  >({});

  const { messages, error, isFinished, isAbsoluteFinished } = useFetchStream<
    CustomEntityModel<KanbanColumnResponse>,
    BaseError
  >({
    path: "/kanban/column/byUserId",
    method: "GET",
    authToken: true,
    refetchOnFocus: false,
  });

  const columns: KanbanColumn[] = useMemo(
    () =>
      messages.map(({ content }) => ({
        ...content,
        dndId: createDndIdColumn(content.id),
      })),
    [JSON.stringify(messages)],
  );

  useEffect(() => {
    setColumnsIds(messages.map(({ content: { id } }) => id));
  }, [JSON.stringify(messages)]);

  useEffect(() => {
    columnsIds.forEach((cId) => {
      if (groupedTasks[cId]) return;
      groupedTasks[cId] = [];
      fetchStream<CustomEntityModel<KanbanTaskResponse>>({
        path: `/kanban/task/byColumnId/${cId}`,
        method: "GET",
        token: authUser.token,
        successCallback: ({ content }) => {
          setGroupedTasksFinished((prev) => ({ ...prev, [cId]: false }));
          setGroupedTasks((prev) => ({
            ...prev,
            [cId]: [
              ...(prev[cId] || []),
              { ...content, dndId: createDndIdTask(content.id) },
            ],
          }));
        },
      })
        .then(({ isFinished }) =>
          setGroupedTasksFinished((prev) => ({ ...prev, [cId]: isFinished })),
        )
        .catch((e) => console.log("WRAPPER ERROR USE", e));
    });
  }, [JSON.stringify(columnsIds)]);

  return isAbsoluteFinished ? (
    <KanbanBoard
      initialColumns={columns}
      initialGroupedTasks={groupedTasks}
      authUser={authUser}
      {...props}
    />
  ) : (
    <motion.div
      className="flex items-center justify-center size-full p-2"
      initial={{ opacity: 0, scale: 0.8 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{ duration: 0.2, delay: 2 }}
    >
      <LoadingSpinner sectionClassName="min-h-[calc(100vh-24rem)]" />
    </motion.div>
  );
}
