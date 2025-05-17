"use client";
import {
  ArchiveQueue,
  ArchiveQueuePrefix,
  ContainerAction,
  NotifyBatchUpdate,
  NotifyContainerAction,
} from "@/types/dto";
import {
  createContext,
  PropsWithChildren,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from "react";
import { Session } from "next-auth";
import useWebSocket, { ReadyState } from "react-use-websocket";
import { toast } from "@/components/ui/use-toast";

interface ItemCnt {
  count: number;
  finished: boolean;
}

interface ArchiveQueueUpdateContextType {
  lastMessage: MessageEvent<any> | null;
  readyState: ReadyState;
  batchUpdateMessages: Record<ArchiveQueue, ItemCnt>;
  actions: Record<ArchiveQueue, ContainerAction | undefined>;
  getBatchUpdates: (q: ArchiveQueue) => ItemCnt;
  getAction: (q: ArchiveQueue) => ContainerAction | undefined;
}

export type ArchiveQueueUpdateTexts = {
  titles: Record<ArchiveQueuePrefix, { delete: string; update: string }>;
  toastActions: Record<ContainerAction, string>;
};

const fromArchiveQueueToTitle = (
  q: ArchiveQueue,
  texts: ArchiveQueueUpdateTexts["titles"],
) => {
  console.log("fromArchiveQueueToTitle q", q, texts);
  const splitQ = q.split("-");
  const prefix = splitQ[0] as ArchiveQueuePrefix;
  const action = splitQ[1];
  return (action === "delete" ? texts[prefix]?.delete : texts[prefix]?.update)
    .split(" ")
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(" ");
};

const ArchiveQueueUpdateContext =
  createContext<ArchiveQueueUpdateContextType | null>(null);
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
export function isNotifyContainerAction(
  obj: unknown,
): obj is NotifyContainerAction {
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

interface Props {
  authUser: Session["user"];
  texts: ArchiveQueueUpdateTexts;
}
const wsUrl = `${process.env.NEXT_PUBLIC_SPRING_CLIENT_WEBSOCKET}/archive/queue/batch/update`;

export default function ArchiveQueueUpdateProvider({
  authUser,
  children,
  texts,
}: PropsWithChildren<Props>) {
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

  const lastProcessedMessageId = useRef<string | null>(null);

  const { lastMessage, readyState } = useWebSocket(
    wsUrl,
    {
      shouldReconnect: (_) => true,
      queryParams: {
        // authToken,
      },
      share: true,
      onMessage: (msg) => {
        if (msg !== null) {
          const parsedMessage = JSON.parse(msg.data);
          console.log("parsedMessage", parsedMessage);
          if (parsedMessage?.id !== lastProcessedMessageId.current) {
            lastProcessedMessageId.current = parsedMessage.id;
            handleLastMessageUpdate(parsedMessage);
          }
        }
      },
    },
    authUser?.role === "ROLE_ADMIN",
  );

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
  const prevActionsState = useRef(actions);

  const getBatchUpdates = useCallback(
    (q: ArchiveQueue) => batchUpdateMessages[q],
    [batchUpdateMessages],
  );

  const getAction = useCallback((q: ArchiveQueue) => actions[q], [actions]);
  useEffect(() => {
    const difActions = Object.entries(actions).reduce(
      (acc, [key, value]) => {
        const castedKey = key as ArchiveQueue;
        if (prevActionsState.current[castedKey] !== value) {
          acc.push({
            queueName: castedKey,
            action: value,
          });
        }
        return acc;
      },
      [] as {
        queueName: ArchiveQueue;
        action: ContainerAction | undefined;
      }[],
    );
    console.log("difActions", difActions);
    if (difActions.length) {
      difActions.forEach(({ queueName, action }) => {
        // for putting the time in manual
        if (!action || action === ContainerAction.START_MANUAL) return;
        const title = fromArchiveQueueToTitle(queueName, texts.titles);
        const actionText = texts.toastActions[action];
        toast({
          title,
          description: actionText,
        });
      });
    }

    prevActionsState.current = actions;
  }, [actions]);

  return (
    <ArchiveQueueUpdateContext.Provider
      value={{
        lastMessage,
        readyState,
        batchUpdateMessages,
        actions,
        getBatchUpdates,
        getAction,
      }}
    >
      {children}
    </ArchiveQueueUpdateContext.Provider>
  );
}

export function useArchiveQueueUpdateContext() {
  const context = useContext(ArchiveQueueUpdateContext);
  if (!context) {
    throw new Error(
      "useArchiveQueueUpdateContext must be used within a ArchiveQueueUpdateProvider",
    );
  }
  return context;
}
