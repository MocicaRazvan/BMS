"use client";
import {
  ApprovedNotificationType,
  ApproveModelNotificationResponse,
  ApproveNotificationResponse,
} from "@/types/dto";
import {
  NotificationProviderProps,
  NotificationTemplateProvider,
  ReactNotificationType,
  useNotificationTemplate,
} from "@/context/notification-template-context";
import { useCallback } from "react";
import { Client } from "@stomp/stompjs";

export const ApproveNotificationTemplateProvider = <
  R extends ApproveModelNotificationResponse,
  T extends ApproveNotificationResponse<R>,
>({
  notificationName,
  authUser,
  children,
  NotificationContext,
}: NotificationProviderProps) => (
  <NotificationTemplateProvider<R, ApprovedNotificationType, T>
    notificationName={notificationName}
    authUser={authUser}
    NotificationContext={NotificationContext}
  >
    {children}
  </NotificationTemplateProvider>
);

export const useApproveNotificationTemplate = <
  R extends ApproveModelNotificationResponse,
  T extends ApproveNotificationResponse<R>,
>(
  NotificationContext: ReactNotificationType,
  notificationName: string,
) => {
  const { context, ...rest } = useNotificationTemplate<
    R,
    ApprovedNotificationType,
    T
  >(NotificationContext);

  const getReferenceByAppId = useCallback(
    (appId: number) => {
      if (!context) return {} as T;
      const reference = context.state.notifications.find(
        (r) => r.reference.appId === appId,
      );
      if (!reference) return {} as T;
      return reference.reference;
    },
    [context],
  );

  const getByAppId = useCallback(
    (appId: number) => {
      if (!context) return [] as T[];
      const ref = getReferenceByAppId(appId);
      if (!ref?.id) return [] as T[];
      return rest.getByReference(ref.id);
    },
    [context, getReferenceByAppId, rest],
  );

  const getTotalByAppId = useCallback(
    (appId: number) => {
      if (!context) return 0;
      const ref = getReferenceByAppId(appId);
      if (!ref?.id) return 0;
      return rest.getTotalByReference(ref.id);
    },
    [context, getReferenceByAppId, rest],
  );

  const removeByAppId = useCallback(
    (appId: number, stompClient: Client) => {
      if (!context) return;
      const ref = getReferenceByAppId(appId);
      console.log("REMOVE BY REFERENCE", ref);
      if (!ref?.id) return;
      if (stompClient.connected) {
        stompClient.publish({
          destination: `/app/${notificationName}/deleteByReferenceId/${ref.id}`,
        });
      }
      rest.removeByReference({ referenceId: ref.id });
    },
    [context, getReferenceByAppId, notificationName, rest],
  );

  return {
    context,
    ...rest,
    getByAppId,
    getTotalByAppId,
    removeByAppId,
  };
};
