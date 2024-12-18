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
  ApprovePostNotificationResponse,
  NotificationPostResponse,
} from "@/types/dto";
import { createContext, useCallback } from "react";
import { Client } from "@stomp/stompjs";

export const PostApproveNotificationContext =
  createContext<NotificationContextType<any, any, any> | null>(null);

export function PostApproveNotificationProvider({
  children,
  authUser,
}: Omit<
  NotificationProviderProps,
  "notificationName" | "NotificationContext"
>) {
  return (
    <ApproveNotificationTemplateProvider<
      NotificationPostResponse,
      ApprovePostNotificationResponse
    >
      notificationName={"approvePostNotification"}
      authUser={authUser}
      NotificationContext={PostApproveNotificationContext}
    >
      {children}
    </ApproveNotificationTemplateProvider>
  );
}

export interface PostPayloadStomp {
  payload: ApprovePostNotificationResponse;
  stompClient: Client;
}

export const approvePostNotificationName = "approvePostNotification" as const;

export const usePostApproveNotification = () => {
  const {
    removeNotification,
    removeByType,
    removeBySender,
    clearNotifications,
    ...rest
  } = useApproveNotificationTemplate<
    NotificationPostResponse,
    ApprovePostNotificationResponse
  >(PostApproveNotificationContext, approvePostNotificationName);

  const removeNotificationPost = useCallback(
    (p: PostPayloadStomp) =>
      removeNotification({
        notificationName: approvePostNotificationName,
        ...p,
      }),
    [removeNotification],
  );

  const removeByTypePost = useCallback(
    (p: {
      type: ApprovedNotificationType;
      stompClient: Client;
      receiverEmail: string;
    }) =>
      removeByType({
        notificationName: approvePostNotificationName,
        ...p,
      }),
    [removeByType],
  );

  const removeBySenderPost = useCallback(
    (p: { stompClient: Client; senderEmail: string; receiverEmail: string }) =>
      removeBySender({
        notificationName: approvePostNotificationName,
        ...p,
      }),
    [removeBySender],
  );

  const clearNotificationsPost = useCallback(
    (p: { stompClient: Client; receiverEmail: string }) =>
      clearNotifications({
        notificationName: approvePostNotificationName,
        ...p,
      }),
    [clearNotifications],
  );
  return {
    ...rest,
    removeNotification: removeNotificationPost,
    removeByType: removeByTypePost,
    removeBySender: removeBySenderPost,
    clearNotifications: clearNotificationsPost,
  };
};
