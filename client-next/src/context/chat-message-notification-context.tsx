"use client";

import { createContext, ReactNode, useCallback } from "react";
import {
  NotificationContextType,
  NotificationTemplateProvider,
  useNotificationTemplate,
} from "@/context/notification-template-context";
import {
  ChatMessageNotificationResponse,
  ChatMessageNotificationType,
  ChatRoomResponse,
} from "@/types/dto";
import { Client } from "@stomp/stompjs";
import { useSession } from "next-auth/react";

interface ChatMessageNotificationProviderProps {
  children: ReactNode;
}

export const ChatMessageNotificationContext =
  createContext<NotificationContextType<any, any, any> | null>(null);

export function ChatMessageNotificationProvider({
  children,
}: ChatMessageNotificationProviderProps) {
  const session = useSession();
  const authUser = session.data?.user;
  return (
    <NotificationTemplateProvider<
      ChatRoomResponse,
      ChatMessageNotificationType,
      ChatMessageNotificationResponse
    >
      notificationName={"chatMessageNotification"}
      authUser={authUser}
      NotificationContext={ChatMessageNotificationContext}
    >
      {children}
    </NotificationTemplateProvider>
  );
}

export interface ChatPayloadStomp {
  payload: ChatMessageNotificationResponse;
  stompClient: Client;
}

export const useChatNotification = () => {
  const {
    removeNotification,
    removeByType,
    removeBySender,
    clearNotifications,
    ...rest
  } = useNotificationTemplate<
    ChatRoomResponse,
    ChatMessageNotificationType,
    ChatMessageNotificationResponse
  >(ChatMessageNotificationContext);

  const removeNotificationChat = useCallback(
    (p: ChatPayloadStomp) =>
      removeNotification({
        notificationName: "chatMessageNotification",
        ...p,
      }),
    [removeNotification],
  );

  const removeByTypeChat = useCallback(
    (p: {
      type: ChatMessageNotificationType;
      stompClient: Client;
      receiverEmail: string;
    }) =>
      removeByType({
        notificationName: "chatMessageNotification",
        ...p,
      }),
    [removeByType],
  );

  const removeBySenderChat = useCallback(
    (p: { stompClient: Client; senderEmail: string; receiverEmail: string }) =>
      removeBySender({
        notificationName: "chatMessageNotification",
        ...p,
      }),
    [removeBySender],
  );

  const clearNotificationsChat = useCallback(
    (p: { stompClient: Client; receiverEmail: string }) =>
      clearNotifications({
        notificationName: "chatMessageNotification",
        ...p,
      }),
    [clearNotifications],
  );

  return {
    removeNotification: removeNotificationChat,
    removeByType: removeByTypeChat,
    removeBySender: removeBySenderChat,
    clearNotifications: clearNotificationsChat,
    ...rest,
  };
};
