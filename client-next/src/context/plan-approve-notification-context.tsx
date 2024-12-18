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
  ApprovePlanNotificationResponse,
  NotificationPlanResponse,
} from "@/types/dto";
import { createContext, useCallback } from "react";
import { Client } from "@stomp/stompjs";

export const PlanApproveNotificationContext =
  createContext<NotificationContextType<any, any, any> | null>(null);

export function PlanApproveNotificationProvider({
  children,
  authUser,
}: Omit<
  NotificationProviderProps,
  "notificationName" | "NotificationContext"
>) {
  return (
    <ApproveNotificationTemplateProvider<
      NotificationPlanResponse,
      ApprovePlanNotificationResponse
    >
      notificationName={"approvePlanNotification"}
      authUser={authUser}
      NotificationContext={PlanApproveNotificationContext}
    >
      {children}
    </ApproveNotificationTemplateProvider>
  );
}

export interface PlanPayloadStomp {
  payload: ApprovePlanNotificationResponse;
  stompClient: Client;
}

export const approvePlanNotification = "approvePlanNotification" as const;

export const usePlanApproveNotification = () => {
  const {
    removeNotification,
    removeByType,
    removeBySender,
    clearNotifications,
    ...rest
  } = useApproveNotificationTemplate<
    NotificationPlanResponse,
    ApprovePlanNotificationResponse
  >(PlanApproveNotificationContext, approvePlanNotification);

  const removeNotificationPlan = useCallback(
    (p: PlanPayloadStomp) =>
      removeNotification({
        notificationName: approvePlanNotification,
        ...p,
      }),
    [removeNotification],
  );

  const removeByTypePlan = useCallback(
    (p: {
      type: ApprovedNotificationType;
      stompClient: Client;
      receiverEmail: string;
    }) =>
      removeByType({
        notificationName: approvePlanNotification,
        ...p,
      }),
    [removeByType],
  );

  const removeBySenderPlan = useCallback(
    (p: { stompClient: Client; senderEmail: string; receiverEmail: string }) =>
      removeBySender({
        notificationName: approvePlanNotification,
        ...p,
      }),
    [removeBySender],
  );

  const clearNotificationsPlan = useCallback(
    (p: { stompClient: Client; receiverEmail: string }) =>
      clearNotifications({
        notificationName: approvePlanNotification,
        ...p,
      }),
    [clearNotifications],
  );
  return {
    ...rest,
    removeNotification: removeNotificationPlan,
    removeByType: removeByTypePlan,
    removeBySender: removeBySenderPlan,
    clearNotifications: clearNotificationsPlan,
  };
};
