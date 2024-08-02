"use client";
import { createContext, useCallback } from "react";
import {
  NotificationContextType,
  NotificationProviderProps,
  NotificationTemplateProvider,
  useNotificationTemplate,
} from "@/context/notification-template-context";
import {
  BoughtNotificationResponse,
  BoughtNotificationType,
  PlanResponse,
} from "@/types/dto";
import { Client } from "@stomp/stompjs";

export const BoughtNotificationContext = createContext<NotificationContextType<
  PlanResponse,
  BoughtNotificationType,
  BoughtNotificationResponse
> | null>(null);

export const boughtNotification = "boughtNotification" as const;
export function BoughtNotificationProvider({
  children,
  authUser,
}: Omit<
  NotificationProviderProps,
  "notificationName" | "NotificationContext"
>) {
  return (
    <NotificationTemplateProvider<
      PlanResponse,
      BoughtNotificationType,
      BoughtNotificationResponse
    >
      notificationName={boughtNotification}
      authUser={authUser}
      NotificationContext={BoughtNotificationContext}
    >
      {children}
    </NotificationTemplateProvider>
  );
}

export interface BoughtPayloadStomp {
  payload: BoughtNotificationResponse;
  stompClient: Client;
}

export const useBoughtNotification = () => {
  const {
    removeNotification,
    removeByType,
    removeBySender,
    clearNotifications,
    ...rest
  } = useNotificationTemplate<
    PlanResponse,
    BoughtNotificationType,
    BoughtNotificationResponse
  >(BoughtNotificationContext);

  const removeNotificationBought = useCallback(
    (p: BoughtPayloadStomp) =>
      removeNotification({
        notificationName: boughtNotification,
        ...p,
      }),
    [removeNotification],
  );

  const removeByTypeBought = useCallback(
    (p: {
      type: BoughtNotificationType;
      stompClient: Client;
      receiverEmail: string;
    }) =>
      removeByType({
        notificationName: boughtNotification,
        ...p,
      }),
    [removeByType],
  );

  const removeBySenderBought = useCallback(
    (p: { stompClient: Client; senderEmail: string; receiverEmail: string }) =>
      removeBySender({
        notificationName: boughtNotification,
        ...p,
      }),
    [removeBySender],
  );

  const clearNotificationsBought = useCallback(
    (p: { stompClient: Client; receiverEmail: string }) =>
      clearNotifications({
        notificationName: boughtNotification,
        ...p,
      }),
    [clearNotifications],
  );

  return {
    removeNotificationBought,
    removeByTypeBought,
    removeBySenderBought,
    clearNotificationsBought,
    ...rest,
  };
};
