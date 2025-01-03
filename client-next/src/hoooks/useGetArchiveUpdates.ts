"use client";
import useWebSocket from "react-use-websocket";
import { useCallback, useEffect, useRef, useState } from "react";
import {
  ArchiveQueue,
  ContainerAction,
  NotifyBatchUpdate,
  NotifyContainerAction,
} from "@/types/dto";

interface ArchiveUpdatesArgs {
  authToken: string;
}
const initialBatchUpdate = Object.values(ArchiveQueue).reduce(
  (acc, curr) => {
    acc[curr as ArchiveQueue] = {
      count: 0,
      finished: false,
    };
    return acc;
  },
  {} as Record<
    ArchiveQueue,
    {
      count: number;
      finished: boolean;
    }
  >,
);

const initialActions = Object.values(ArchiveQueue).reduce(
  (acc, curr) => {
    acc[curr as ArchiveQueue] = undefined;
    return acc;
  },
  {} as Record<ArchiveQueue, ContainerAction | undefined>,
);

function isNotifyBatchUpdate(obj: unknown): obj is NotifyBatchUpdate {
  return (
    typeof obj === "object" &&
    obj !== null &&
    "queueName" in obj &&
    "timestamp" in obj &&
    "id" in obj &&
    "finished" in obj &&
    "numberProcessed" in obj &&
    Object.values(ArchiveQueue).includes((obj as NotifyBatchUpdate).queueName)
  );
}
function isNotifyContainerAction(obj: unknown): obj is NotifyContainerAction {
  return (
    typeof obj === "object" &&
    obj !== null &&
    "action" in obj &&
    "timestamp" in obj &&
    "id" in obj &&
    "queueName" in obj &&
    Object.values(ContainerAction).includes(
      (obj as NotifyContainerAction).action,
    )
  );
}

export default function useGetArchiveUpdates({
  authToken,
}: ArchiveUpdatesArgs) {
  const wsUrl = `${process.env.NEXT_PUBLIC_SPRING_CLIENT_WEBSOCKET}/archive/queue/batch/update`;
  const { lastMessage, readyState } = useWebSocket(wsUrl, {
    shouldReconnect: (_) => true,
    queryParams: {
      authToken,
    },
    share: true,
  });
  const lastProcessedMessageId = useRef<string | null>(null);
  const [batchUpdateMessages, setBatchUpdateMessages] = useState<
    Record<
      ArchiveQueue,
      {
        count: number;
        finished: boolean;
      }
    >
  >(initialBatchUpdate);
  const [actions, setActions] =
    useState<Record<ArchiveQueue, ContainerAction | undefined>>(initialActions);

  const handleLastMessageUpdate = useCallback((msg: unknown) => {
    if (isNotifyBatchUpdate(msg)) {
      setBatchUpdateMessages((prev) => ({
        ...prev,
        [msg.queueName]: {
          count: msg.numberProcessed,
          finished: msg.finished,
        },
      }));
    } else if (isNotifyContainerAction(msg)) {
      setActions((prev) => ({
        ...prev,
        [msg.queueName]: msg.action,
      }));
      setBatchUpdateMessages((prev) => ({
        ...prev,
        [msg.queueName]: {
          count: 0,
          finished: false,
        },
      }));
    }
  }, []);

  useEffect(() => {
    if (lastMessage) {
      const parsedMessage = JSON.parse(lastMessage.data);
      if (parsedMessage.id !== lastProcessedMessageId.current) {
        lastProcessedMessageId.current = parsedMessage.id;
        handleLastMessageUpdate(parsedMessage);
      }
    }
  }, [lastMessage, handleLastMessageUpdate]);

  const getBatchUpdates = useCallback(
    (q: ArchiveQueue) => batchUpdateMessages[q],
    [batchUpdateMessages],
  );

  const getAction = useCallback((q: ArchiveQueue) => actions[q], [actions]);

  return {
    lastMessage,
    readyState,
    batchUpdateMessages,
    actions,
    getBatchUpdates,
    getAction,
  };
}
