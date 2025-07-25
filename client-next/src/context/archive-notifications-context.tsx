"use client";

import { NotifyContainerAction } from "@/types/dto";
import {
  createContext,
  ReactNode,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { Session } from "next-auth";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import useWebSocket from "react-use-websocket";
import { useSession } from "next-auth/react";

interface ArchiveNotificationsContextType {
  notifications: NotifyContainerAction[];
  deleteNotification: (id: string) => void;
  deleteManyNotifications: (ids: string[]) => void;
  deleteAllNotifications: () => void;
}

const ArchiveNotificationsContext =
  createContext<ArchiveNotificationsContextType | null>(null);
const wsUrl = `${process.env.NEXT_PUBLIC_SPRING_CLIENT_WEBSOCKET}/archive/container/actions`;

interface Props {
  children: ReactNode;
}

export default function ArchiveNotificationsProvider({ children }: Props) {
  const session = useSession();
  const authUser = session.data?.user;
  const [notifications, setNotifications] = useState<NotifyContainerAction[]>(
    [],
  );
  const { messages, isFinished } = useFetchStream<NotifyContainerAction>({
    authToken: true,
    refetchOnFocus: false,
    path: "/archive/actions/notifications",
    trigger: authUser?.role === "ROLE_ADMIN",
  });
  const { sendJsonMessage } = useWebSocket(
    wsUrl,
    {
      shouldReconnect: (_) => true,
      share: true,
      onMessage: (message) => {
        if (message !== null) {
          const parsedMessage = JSON.parse(
            message.data,
          ) as NotifyContainerAction;
          setNotifications((prevMessages) => [parsedMessage, ...prevMessages]);
        }
      },
    },
    authUser?.role === "ROLE_ADMIN",
  );

  useEffect(() => {
    if (isFinished && messages.length > 0) {
      setNotifications(messages);
    }
  }, [isFinished, messages]);

  const deleteNotifications = useCallback(
    (ids: string[]) => {
      if (notifications.length === 0) return;
      sendJsonMessage({
        type: "delete",
        ids,
      });
      setNotifications((prev) =>
        prev.filter((notification) => !ids.includes(notification.id)),
      );
    },
    [notifications.length, sendJsonMessage],
  );

  const deleteNotification = useCallback(
    (id: string) => deleteNotifications([id]),
    [deleteNotifications],
  );

  const deleteAllNotifications = useCallback(() => {
    if (notifications.length === 0) return;
    sendJsonMessage({
      type: "delete",
      ids: notifications.map((notification) => notification.id),
    });
    setNotifications([]);
  }, [notifications, sendJsonMessage]);

  const memoizedContext: ArchiveNotificationsContextType = useMemo(
    () => ({
      notifications,
      deleteNotification,
      deleteManyNotifications: deleteNotifications,
      deleteAllNotifications,
    }),
    [
      notifications,
      deleteNotification,
      deleteNotifications,
      deleteAllNotifications,
    ],
  );

  return (
    <ArchiveNotificationsContext.Provider value={memoizedContext}>
      {children}
    </ArchiveNotificationsContext.Provider>
  );
}

export function useArchiveNotifications(authUser: Session["user"]) {
  const deleteNotificationNoOp = useCallback((_: string) => {}, []);
  const deleteManyNotificationsNoOp = useCallback((_: string[]) => {}, []);
  const deleteAllNotificationsNoOp = useCallback(() => {}, []);
  const context = useContext(ArchiveNotificationsContext);
  if (!authUser) {
    return {
      notifications: [],
      deleteNotification: deleteNotificationNoOp,
      deleteManyNotifications: deleteManyNotificationsNoOp,
      deleteAllNotifications: deleteAllNotificationsNoOp,
    };
  }
  if (!context) {
    throw new Error(
      "useArchiveNotifications must be used within an ArchiveNotificationsProvider",
    );
  }
  return context;
}
