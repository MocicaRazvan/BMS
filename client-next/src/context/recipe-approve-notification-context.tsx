"use client";
import {
  NotificationContextType,
  NotificationProviderProps,
} from "@/context/notification-template-context";
import {
  ApproveNotificationTemplateProvider,
  useApproveNotificationTemplate,
} from "@/context/approve-notification-template-context";
import {
  ApprovedNotificationType,
  ApproveRecipeNotificationResponse,
  NotificationRecipeResponse,
} from "@/types/dto";
import { createContext, useCallback } from "react";
import { Client } from "@stomp/stompjs";

export const RecipeApproveNotificationContext =
  createContext<NotificationContextType<any, any, any> | null>(null);

export function RecipeApproveNotificationProvider({
  children,
  authUser,
}: Omit<
  NotificationProviderProps,
  "notificationName" | "NotificationContext"
>) {
  return (
    <ApproveNotificationTemplateProvider<
      NotificationRecipeResponse,
      ApproveRecipeNotificationResponse
    >
      notificationName={"approveRecipeNotification"}
      authUser={authUser}
      NotificationContext={RecipeApproveNotificationContext}
    >
      {children}
    </ApproveNotificationTemplateProvider>
  );
}

export interface RecipePayloadStomp {
  payload: ApproveRecipeNotificationResponse;
  stompClient: Client;
}

export const approveRecipeNotificationName =
  "approveRecipeNotification" as const;

export const useRecipeApproveNotification = () => {
  const {
    removeNotification,
    removeByType,
    removeBySender,
    clearNotifications,
    ...rest
  } = useApproveNotificationTemplate<
    NotificationRecipeResponse,
    ApproveRecipeNotificationResponse
  >(RecipeApproveNotificationContext, approveRecipeNotificationName);

  const removeNotificationRecipe = useCallback(
    (p: RecipePayloadStomp) =>
      removeNotification({
        notificationName: approveRecipeNotificationName,
        ...p,
      }),
    [removeNotification],
  );

  const removeByTypeRecipe = useCallback(
    (p: {
      type: ApprovedNotificationType;
      stompClient: Client;
      receiverEmail: string;
    }) =>
      removeByType({
        notificationName: approveRecipeNotificationName,
        ...p,
      }),
    [removeByType],
  );

  const removeBySenderRecipe = useCallback(
    (p: { stompClient: Client; senderEmail: string; receiverEmail: string }) =>
      removeBySender({
        notificationName: approveRecipeNotificationName,
        ...p,
      }),
    [removeBySender],
  );

  const clearNotificationsRecipe = useCallback(
    (p: { stompClient: Client; receiverEmail: string }) =>
      clearNotifications({
        notificationName: approveRecipeNotificationName,
        ...p,
      }),
    [clearNotifications],
  );
  return {
    ...rest,
    removeNotification: removeNotificationRecipe,
    removeByType: removeByTypeRecipe,
    removeBySender: removeBySenderRecipe,
    clearNotifications: clearNotificationsRecipe,
  };
};
