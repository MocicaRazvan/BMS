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
import useWebSocket, { ReadyState } from "react-use-websocket";
import { toast } from "@/components/ui/use-toast";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

interface ItemCnt {
  count: number;
  finished: boolean;
}
interface ContainerActionEntry {
  action?: ContainerAction;
  id?: string;
}
type ContainerActionsType = Record<ArchiveQueue, ContainerActionEntry>;
interface ArchiveQueueUpdateContextType {
  lastMessage: MessageEvent<any> | null;
  readyState: ReadyState;
  batchUpdateMessages: Record<ArchiveQueue, ItemCnt>;
  actions: ContainerActionsType;
  getBatchUpdates: (q: ArchiveQueue) => ItemCnt;
  getAction: (q: ArchiveQueue) => ContainerActionEntry;
  getLastCronActionHandled: (q: ArchiveQueue) => string | undefined;
  setLastCronActionHandled: (q: ArchiveQueue, id: string | undefined) => void;
}

export type ArchiveQueueUpdateTexts = {
  titles: Record<ArchiveQueuePrefix, { delete: string; update: string }>;
  toastActions: Record<ContainerAction, string>;
};

const fromArchiveQueueToTitle = (
  q: ArchiveQueue,
  texts: ArchiveQueueUpdateTexts["titles"],
) => {
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

const initialActions = Object.values(ArchiveQueue).reduce((acc, curr) => {
  acc[curr as ArchiveQueue] = {
    action: undefined,
    id: undefined,
  };
  return acc;
}, {} as ContainerActionsType);

type LastCronActionsHandledType = Record<ArchiveQueue, string | undefined>;
const initialLastCronActionsHandled = Object.values(ArchiveQueue).reduce(
  (acc, curr) => {
    acc[curr as ArchiveQueue] = undefined;
    return acc;
  },
  {} as LastCronActionsHandledType,
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
  texts: ArchiveQueueUpdateTexts;
}
const wsUrl = `${process.env.NEXT_PUBLIC_SPRING_CLIENT_WEBSOCKET}/archive/queue/batch/update`;

export default function ArchiveQueueUpdateProvider({
  children,
  texts,
}: PropsWithChildren<Props>) {
  const { authUser } = useAuthUserMinRole();

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
        [msg.queueName]: {
          action: msg.action,
          id: msg.id,
        },
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

  const lastCronActionsHandled = useRef<LastCronActionsHandledType>(
    initialLastCronActionsHandled,
  );

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
  const [actions, setActions] = useState<
    Record<
      ArchiveQueue,
      {
        action?: ContainerAction;
        id?: string;
      }
    >
  >(initialActions);
  const prevActionsState = useRef(actions);

  const getBatchUpdates = useCallback(
    (q: ArchiveQueue) => batchUpdateMessages[q],
    [batchUpdateMessages],
  );

  const getLastCronActionHandled = useCallback(
    (q: ArchiveQueue) => lastCronActionsHandled.current[q],
    [],
  );
  const setLastCronActionHandled = useCallback(
    (q: ArchiveQueue, id: string | undefined) => {
      lastCronActionsHandled.current[q] = id;
    },
    [],
  );

  const getAction = useCallback((q: ArchiveQueue) => actions[q], [actions]);
  useEffect(() => {
    const difActions = Object.entries(actions).reduce(
      (acc, [key, value]) => {
        const castedKey = key as ArchiveQueue;
        if (prevActionsState.current[castedKey].action !== value.action) {
          acc.push({
            queueName: castedKey,
            action: value.action,
          });
        }
        return acc;
      },
      [] as {
        queueName: ArchiveQueue;
        action: ContainerAction | undefined;
      }[],
    );
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
        getLastCronActionHandled,
        setLastCronActionHandled,
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
