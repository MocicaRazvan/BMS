"use client";
import {
  IdGenerateDto,
  NotificationTemplateBody,
  NotificationTemplateResponse,
} from "@/types/dto";
import React from "react";
import { Client } from "@stomp/stompjs";
import useFetchStream from "@/hoooks/useFetchStream";
import { Session } from "next-auth";
import { useSubscription } from "react-stomp-hooks";
import { WithUser } from "@/lib/user";

interface TotalsNotification {
  total: number;
  totalByType: { [key: string]: number };
  totalBySender: { [key: string]: number };
}

export interface NotificationState<
  R extends IdGenerateDto,
  E extends string,
  T extends NotificationTemplateResponse<R, E>,
> extends TotalsNotification {
  notifications: T[];
}

interface PayloadWithStomp<
  R extends IdGenerateDto,
  E extends string,
  T extends NotificationTemplateResponse<R, E>,
> {
  payload: T;
  stompClient: Client;
  notificationName: string;
}

export type NotificationAction<
  R extends IdGenerateDto,
  E extends string,
  T extends NotificationTemplateResponse<R, E>,
> =
  | { type: "ADD"; payload: T }
  | {
      type: "REMOVE_ONE";
      payload: {
        payload: T;
        stompClient: Client;
        notificationName: string;
      };
    }
  | {
      type: "REMOVE_BY_TYPE";
      payload: {
        type: E;
        stompClient: Client;
        notificationName: string;
        receiverEmail: string;
      };
    }
  | {
      type: "REMOVE_BY_SENDER";
      payload: {
        senderEmail: string;
        stompClient: Client;
        notificationName: string;
        receiverEmail: string;
      };
    }
  | { type: "REMOVE_BY_REFERENCE"; payload: { referenceId: number } }
  | { type: "INIT_MULTIPLE"; payload: T[] }
  | {
      type: "CLEAR";
      payload: {
        stompClient: Client;
        notificationName: string;
        receiverEmail: string;
      };
    };

const generateTotals = <
  R extends IdGenerateDto,
  E extends string,
  T extends NotificationTemplateResponse<R, E>,
>(
  notifications: T[],
) =>
  notifications.reduce(
    (acc, cur) => {
      acc.total += 1;
      acc.totalByType[cur.type] = (acc.totalByType[cur.type] || 0) + 1;
      acc.totalBySender[cur.sender.email] =
        (acc.totalBySender[cur.sender.email] || 0) + 1;
      return acc;
    },
    {
      total: 0,
      totalByType: {},
      totalBySender: {},
    } as TotalsNotification,
  );

const updateTotals = <
  R extends IdGenerateDto,
  E extends string,
  T extends NotificationTemplateResponse<R, E>,
>(
  state: NotificationState<R, E, T>,
  notification: NotificationTemplateResponse<R, E>,
  decrement = false,
) => {
  const step = decrement ? -1 : 1;
  state.total += step;
  state.totalByType[notification.type] =
    (state.totalByType[notification.type] || 0) + step;
  state.totalBySender[notification.sender.email] =
    (state.totalBySender[notification.sender.email] || 0) + step;
  return state;
};

const fromNotificationResponseToBody = <
  R extends IdGenerateDto,
  E extends string,
  RESP extends NotificationTemplateResponse<R, E>,
  BODY extends NotificationTemplateBody<E>,
>(
  resp: RESP,
): BODY =>
  ({
    senderEmail: resp.sender.email,
    receiverEmail: resp.receiver.email,
    type: resp.type,
    referenceId: resp.reference.id,
    content: resp.content,
    extraLink: resp.extraLink,
  }) as BODY;

export interface NotificationContextType<
  R extends IdGenerateDto,
  E extends string,
  T extends NotificationTemplateResponse<R, E>,
> {
  state: NotificationState<R, E, T>;
  dispatch: React.Dispatch<NotificationAction<R, E, T>>;
}

const initialState: NotificationState<any, any, any> = {
  notifications: [],
  total: 0,
  totalByType: {},
  totalBySender: {},
};

// export const NotificationContext = React.createContext<NotificationContextType<
//   any,
//   any,
//   any
// > | null>(null);

export const notificationReducer = <
  R extends IdGenerateDto,
  E extends string,
  T extends NotificationTemplateResponse<R, E>,
>(
  state: NotificationState<R, E, T>,
  action: NotificationAction<R, E, T>,
): NotificationState<R, E, T> => {
  switch (action.type) {
    case "ADD":
      if (!action.payload) return state;
      console.log("ADD_REDUCER", action.payload.id);
      const notifExists = state.notifications.find(
        ({ id }) => id === action.payload.id,
      );
      if (notifExists) return state;
      const updatedStateAdd = {
        ...state,
        notifications: [...state.notifications, action.payload],
      };
      return updateTotals(updatedStateAdd, action.payload);

    case "REMOVE_ONE":
      try {
        if (!action.payload) return state;
        action.payload.stompClient.publish({
          destination: `/app/${action.payload.notificationName}/deleteNotification/${action.payload.payload.id}`,
          body: JSON.stringify(
            fromNotificationResponseToBody(action.payload.payload),
          ),
        });
        const newNotifications = state.notifications.filter(
          ({ id }) => id !== action.payload.payload.id,
        );
        const updatedStateRemove = {
          ...state,
          notifications: newNotifications,
        };
        return updateTotals(updatedStateRemove, action.payload.payload, true);
      } catch (e) {
        console.error("REMOVE_ONE error:", e);
        return state;
      }

    case "REMOVE_BY_TYPE":
      try {
        if (!action.payload) return state;

        action.payload.stompClient.publish({
          destination: `/app/${action.payload.notificationName}/deleteAllByReceiverEmailAndType`,
          body: JSON.stringify({
            senderEmail: action.payload.receiverEmail,
            type: action.payload.type,
          }),
        });

        const [remaining, removed, totalsBySender] = state.notifications.reduce(
          (acc, cur) => {
            if (cur.type !== action.payload.type) acc[0].push(cur);
            else {
              acc[1].push(cur);
              acc[2][cur.sender.email] = (acc[2][cur.sender.email] || 0) + 1;
            }
            return acc;
          },
          [[], [], {}] as [T[], T[], { [key: string]: number }],
        );
        const updatedStateRemoveByType: NotificationState<R, E, T> = {
          ...state,
          notifications: remaining,
          total: state.total - removed.length,
          totalByType: {
            ...state.totalByType,
            [action.payload.type]: 0,
          },
        };
        Object.entries(totalsBySender).forEach(([key, value]) => {
          updatedStateRemoveByType.totalBySender[key] -= value;
        });

        return updatedStateRemoveByType;
      } catch (e) {
        console.error("REMOVE_BY_TYPE error:", e);
        return state;
      }

    case "REMOVE_BY_SENDER":
      try {
        if (!action.payload) return state;

        action.payload.stompClient.publish({
          destination: `/app/${action.payload.notificationName}/deleteAllByReceiverEmailSenderEmail`,
          body: JSON.stringify({
            senderEmail: action.payload.senderEmail,
            receiverEmail: action.payload.receiverEmail,
          }),
        });

        const [remainingBySender, removedBySender, totalsByType] =
          state.notifications.reduce<[T[], T[], { [key: string]: number }]>(
            (acc, cur) => {
              if (cur.sender.email !== action.payload.senderEmail)
                acc[0].push(cur);
              else {
                acc[1].push(cur);
                acc[2][cur.type] = (acc[2][cur.type] || 0) + 1;
              }
              return acc;
            },
            [[], [], {}],
          );
        const updatedStateRemoveBySender: NotificationState<R, E, T> = {
          ...state,
          notifications: remainingBySender,
          total: state.total - removedBySender.length,
          totalBySender: {
            ...state.totalBySender,
            [action.payload.senderEmail]: 0,
          },
        };
        Object.entries(totalsByType).forEach(([key, value]) => {
          updatedStateRemoveBySender.totalByType[key] -= value;
        });

        return updatedStateRemoveBySender;
      } catch (e) {
        console.error("REMOVE_BY_SENDER error:", e);
        return state;
      }

    case "REMOVE_BY_REFERENCE":
      // return state;
      //todo in backend
      console.log("REMOVE BY REFERENCE", action.payload.referenceId);
      if (!action.payload) return state;
      const [
        remainingByReference,
        removedByReference,
        totalByReferenceSender,
        totalByReferenceType,
      ] = state.notifications.reduce(
        (acc, cur) => {
          if (cur.reference.id !== action.payload.referenceId) acc[0].push(cur);
          else {
            acc[1].push(cur);
            acc[2][cur.sender.email] = (acc[2][cur.sender.email] || 0) + 1;
            acc[3][cur.type] = (acc[3][cur.type] || 0) + 1;
          }
          return acc;
        },
        [[], [], {}, {}] as [
          T[],
          T[],
          { [key: string]: number },
          { [key: string]: number },
        ],
      );
      const updatedStateRemoveByReference: NotificationState<R, E, T> = {
        ...state,
        notifications: remainingByReference,
        total: state.total - removedByReference.length,
      };

      Object.entries(totalByReferenceSender).forEach(([key, value]) => {
        updatedStateRemoveByReference.totalBySender[key] -= value;
      });
      Object.entries(totalByReferenceType).forEach(([key, value]) => {
        updatedStateRemoveByReference.totalByType[key] -= value;
      });
      console.log(
        "REMOVE BY REFERENCE",
        state.notifications,
        updatedStateRemoveByReference.notifications,
      );

      return updatedStateRemoveByReference;

    case "INIT_MULTIPLE":
      if (!action.payload) return state;
      return {
        ...state,
        notifications: action.payload,
        ...generateTotals(action.payload),
      };

    case "CLEAR":
      try {
        if (!action.payload) return state;

        action.payload.stompClient.publish({
          destination: `/app/${action.payload.notificationName}/deleteAllByReceiverEmailAndType`,
          body: JSON.stringify({
            senderEmail: action.payload.receiverEmail,
          }),
        });
        return initialState;
      } catch (e) {
        console.error("CLEAR error:", e);
        return state;
      }

    default:
      return state;
  }
};

export type ReactNotificationType = React.Context<NotificationContextType<
  any,
  any,
  any
> | null>;

export interface NotificationProviderProps {
  children: React.ReactNode;
  notificationName: string;
  authUser: Session["user"];
  NotificationContext: ReactNotificationType;
}

// export const NotificationContext = React.createContext<NotificationContextType<
//   any,
//   any,
//   any
// > | null>(null);
export const NotificationTemplateProvider = <
  R extends IdGenerateDto,
  E extends string,
  T extends NotificationTemplateResponse<R, E>,
>({
  children,
  notificationName,
  authUser,
  NotificationContext,
}: NotificationProviderProps) => {
  // console.log("total creating", notificationName);
  const [state, dispatch] = React.useReducer(
    notificationReducer as React.Reducer<
      NotificationState<R, E, T>,
      NotificationAction<R, E, T>
    >,
    initialState as NotificationState<R, E, T>,
  );
  if (!authUser || !authUser.email) {
    return (
      <NotificationContext.Provider value={{ state, dispatch }}>
        {children}
      </NotificationContext.Provider>
    );
  }
  return (
    <NotificationTemplateAuthUser
      authUser={authUser}
      NotificationContext={NotificationContext}
      state={state}
      notificationName={notificationName}
      dispatch={dispatch}
    >
      {children}
    </NotificationTemplateAuthUser>
  );
};
interface NotificationTemplateAuthUser<
  R extends IdGenerateDto,
  E extends string,
  T extends NotificationTemplateResponse<R, E>,
> extends Omit<NotificationProviderProps, "authUser">,
    WithUser {
  state: NotificationState<R, E, T>;
  dispatch: React.Dispatch<NotificationAction<R, E, T>>;
}
const NotificationTemplateAuthUser = <
  R extends IdGenerateDto,
  E extends string,
  T extends NotificationTemplateResponse<R, E>,
>({
  children,
  notificationName,
  authUser,
  NotificationContext,
  state,
  dispatch,
}: NotificationTemplateAuthUser<R, E, T>) => {
  const { messages, error, isFinished } = useFetchStream<T[]>({
    path: `/ws-http/${notificationName}/getAllByReceiverEmailAndType`,
    method: "PATCH",
    authToken: true,
    acceptHeader: "application/json",
    body: {
      senderEmail: authUser?.email,
      type: null,
    },
  });

  useSubscription(
    `/queue/notification-${notificationName}-added-${authUser?.email}`,
    (message) => {
      console.log("total added", notificationName);
      const newMessage = JSON.parse(message.body) satisfies T;
      console.log("ADD", newMessage.id);
      dispatch({ type: "ADD", payload: newMessage });
    },
  );

  useSubscription(
    `/queue/notification-${notificationName}-removed-${authUser?.email}`,
    (message) => {
      const newMessage = JSON.parse(message.body);

      let referenceId;

      if (typeof newMessage === "string") {
        console.log("referenceId string");
        referenceId = parseInt(newMessage);
      } else if (newMessage?.reference?.id) {
        console.log("referenceId obj");
        referenceId = newMessage.reference.id;
      }

      if (!referenceId) return;

      console.log("referenceId", referenceId);

      console.log("total removed", newMessage);
      console.log("REMOVE BY REFERENCE", newMessage);
      dispatch({
        type: "REMOVE_BY_REFERENCE",
        payload: {
          referenceId,
        },
      });
    },
  );

  React.useEffect(() => {
    if (isFinished && !error && messages && messages?.[0]?.length > 0) {
      // console.log("total init", notificationName);
      dispatch({ type: "INIT_MULTIPLE", payload: messages[0] });
    }
  }, [isFinished, JSON.stringify(messages), error]);

  if (error) {
    console.log(messages);
    console.error(error);
    // return children;
  }

  // const ContextProvider = createNotificationTemplateContext<R, E, T>();
  return (
    <NotificationContext.Provider value={{ state, dispatch }}>
      {children}
    </NotificationContext.Provider>
  );
};

export const useNotificationTemplate = <
  R extends IdGenerateDto,
  E extends string,
  T extends NotificationTemplateResponse<R, E>,
>(
  NotificationContext: ReactNotificationType,
) => {
  const context = React.useContext(NotificationContext);

  const noop = React.useCallback(() => {
    console.error("Notification context not found");
  }, []);
  const noopState = React.useCallback(
    () =>
      ({
        notifications: [],
        total: 0,
        totalByType: {},
        totalBySender: {},
      }) as NotificationState<R, E, T>,
    [],
  );

  const addNotification = React.useCallback(
    context ? (payload: T) => context.dispatch({ type: "ADD", payload }) : noop,
    [context, noop],
  );

  const removeNotification = React.useCallback(
    context
      ? (payload: PayloadWithStomp<R, E, T>) =>
          context.dispatch({ type: "REMOVE_ONE", payload })
      : noop,
    [context, noop],
  );

  const removeByType = React.useCallback(
    context
      ? (payload: {
          type: E;
          stompClient: Client;
          notificationName: string;
          receiverEmail: string;
        }) => context.dispatch({ type: "REMOVE_BY_TYPE", payload })
      : noop,
    [context, noop],
  );

  const removeBySender = React.useCallback(
    context
      ? (payload: {
          senderEmail: string;
          stompClient: Client;
          notificationName: string;
          receiverEmail: string;
        }) => context.dispatch({ type: "REMOVE_BY_SENDER", payload })
      : noop,
    [context, noop],
  );

  const removeByReference = React.useCallback(
    context
      ? (payload: { referenceId: number }) =>
          context.dispatch({ type: "REMOVE_BY_REFERENCE", payload })
      : noop,
    [context, noop],
  );

  const clearNotifications = React.useCallback(
    context
      ? (payload: {
          stompClient: Client;
          notificationName: string;
          receiverEmail: string;
        }) => context.dispatch({ type: "CLEAR", payload })
      : noop,
    [context, noop],
  );

  const getNotificationState = React.useCallback(
    context ? () => context.state as NotificationState<R, E, T> : noopState,
    [context, noopState],
  );

  const getById = React.useCallback(
    context
      ? (id: number) =>
          context.state.notifications.find(
            ({ id: notificationId }) => notificationId === id,
          ) as T
      : (id: number) => undefined as T | undefined,
    [context],
  );

  const getBySenderEmail = React.useCallback(
    context
      ? (senderEmail: string) =>
          context.state.notifications.filter(
            ({ sender }) => sender.email === senderEmail,
          ) as T[]
      : (senderEmail: string) => [] as T[],
    [context],
  );

  const getByType = React.useCallback(
    context
      ? (type: E) =>
          context.state.notifications.filter(
            ({ type: notificationType }) => notificationType === type,
          )
      : (type: E) => [] as T[],
    [context],
  );

  const getTotalByType = React.useCallback(
    context
      ? (type: E) => context.state.totalByType[type] || 0
      : (type: E) => 0,
    [context],
  );

  const getTotalBySender = React.useCallback(
    context
      ? (senderEmail: string) => context.state.totalBySender[senderEmail] || 0
      : (senderEmail: string) => 0,
    [context],
  );

  const getByReference = React.useCallback(
    context
      ? (referenceId: number) =>
          context.state.notifications.filter(
            ({ reference }) => reference.id === referenceId,
          ) as T[]
      : (referenceId: number) => [] as T[],
    [context],
  );

  const getTotalByReference = React.useCallback(
    context
      ? (referenceId: number) => getByReference(referenceId).length
      : (referenceId: number) => 0,
    [context, getByReference],
  );

  const getNotificationsGroupedBySender = React.useCallback(
    context
      ? () =>
          context.state.notifications.reduce<{
            notifications: { [key: string]: T[] };
            total: number;
            totalSenders: number;
          }>(
            (acc, cur) => {
              if (!acc.notifications[cur.sender.email]) {
                acc.notifications[cur.sender.email] = [];
                acc.totalSenders += 1;
              }
              acc.notifications[cur.sender.email].push(cur);
              acc.total = acc.total ? acc.total + 1 : 1;
              return acc;
            },
            { notifications: {}, total: 0, totalSenders: 0 } as {
              notifications: { [key: string]: T[] };
              total: number;
              totalSenders: number;
            },
          )
      : () => ({
          notifications: {},
          total: 0,
          totalSenders: 0,
        }),
    [context],
  );

  const getTotals = React.useCallback(
    context
      ? () =>
          ({
            total: context.state.total,
            totalByType: context.state.totalByType,
            totalBySender: context.state.totalBySender,
          }) as TotalsNotification
      : () => ({
          total: 0,
          totalByType: {},
          totalBySender: {},
        }),
    [context],
  );

  return {
    addNotification,
    removeNotification,
    removeByType,
    removeBySender,
    removeByReference,
    clearNotifications,
    getNotificationState,
    getById,
    getBySenderEmail,
    getByType,
    getTotalByType,
    getTotalBySender,
    getByReference,
    getTotalByReference,
    getNotificationsGroupedBySender,
    getTotals,
    context,
  };
};
