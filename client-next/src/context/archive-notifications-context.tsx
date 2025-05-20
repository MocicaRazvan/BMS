"use client";

import { NotifyContainerAction } from "@/types/dto";
import {
  createContext,
  ReactNode,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { Session } from "next-auth";
import useFetchStream from "@/hoooks/useFetchStream";
import useWebSocket from "react-use-websocket";

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
  authUser: Session["user"];
}

export default function ArchiveNotificationsProvider({
  authUser,
  children,
}: Props) {
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

  return (
    <ArchiveNotificationsContext.Provider
      value={{
        notifications,
        deleteNotification,
        deleteManyNotifications: deleteNotifications,
        deleteAllNotifications,
      }}
    >
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
