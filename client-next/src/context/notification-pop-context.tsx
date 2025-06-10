"use client";
import {
  createContext,
  Dispatch,
  ReactNode,
  SetStateAction,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { ChatMessageNotificationContentTexts } from "@/components/nav/chat-notitifications-content";
import { ApproveNotificationContentTexts } from "@/components/nav/approve-notifications-content";
import { BoughtNotificationContentTexts } from "@/components/nav/bought-notification-content";
import { useChatNotification } from "@/context/chat-message-notification-context";
import { usePostApproveNotification } from "@/context/post-approve-notification-context";
import { useRecipeApproveNotification } from "@/context/recipe-approve-notification-context";
import { usePlanApproveNotification } from "@/context/plan-approve-notification-context";
import {
  BoughtPayloadStomp,
  useBoughtNotification,
} from "@/context/bought-notification-context";
import { useStompClient } from "react-stomp-hooks";
import { usePathname } from "@/navigation/navigation";
import {
  getApprovedNotificationTextsByItems,
  getBoughtNotificationTextsByItems,
  getChatMessageNotificationTextsForReceiver,
  getNotificationPopTexts,
} from "@/texts/components/nav";
import isEqual from "lodash.isequal";
import {
  ApprovedNotificationType,
  ApprovePlanNotificationResponse,
  ApprovePostNotificationResponse,
  ApproveRecipeNotificationResponse,
  BoughtNotificationResponse,
  ChatMessageNotificationResponse,
  NotificationPlanResponse,
  NotificationPostResponse,
  NotificationRecipeResponse,
  NotifyContainerAction,
  PlanResponse,
} from "@/types/dto";
import { NotificationPopTexts } from "@/components/nav/notification-pop";
import { Client } from "@stomp/stompjs";
import { NotificationState } from "@/context/notification-template-context";
import { useLocale } from "next-intl";
import { WithUser } from "@/lib/user";
import { useArchiveNotifications } from "@/context/archive-notifications-context";
import { useSession } from "next-auth/react";

interface NotificationPopProviderProps {
  children: ReactNode;
}

interface NotificationPopContextType {
  notificationPopTexts: NotificationPopTexts | null;
  setNotificationPopTexts: Dispatch<
    SetStateAction<NotificationPopTexts | null>
  >;
  chatMessageNotificationTexts: Record<
    string,
    ChatMessageNotificationContentTexts
  > | null;
  setChatMessageNotificationTexts: Dispatch<
    SetStateAction<Record<string, ChatMessageNotificationContentTexts> | null>
  >;
  postMessageNotificationsTexts: Record<
    string,
    ApproveNotificationContentTexts
  > | null;
  setPostMessageNotificationsTexts: Dispatch<
    SetStateAction<Record<string, ApproveNotificationContentTexts> | null>
  >;
  recipeMessageNotificationsTexts: Record<
    string,
    ApproveNotificationContentTexts
  > | null;
  setRecipeMessageNotificationsTexts: Dispatch<
    SetStateAction<Record<string, ApproveNotificationContentTexts> | null>
  >;
  planMessageNotificationsTexts: Record<
    string,
    ApproveNotificationContentTexts
  > | null;
  setPlanMessageNotificationsTexts: Dispatch<
    SetStateAction<Record<string, ApproveNotificationContentTexts> | null>
  >;
  boughtNotificationTexts: Record<
    string,
    BoughtNotificationContentTexts
  > | null;
  setBoughtNotificationTexts: Dispatch<
    SetStateAction<Record<string, BoughtNotificationContentTexts> | null>
  >;
  totalNotifications: number;
  totalChatNotifications: number;
  totalPostNotifications: number;
  totalRecipeNotifications: number;
  totalPlanNotifications: number;
  totalBoughtNotifications: number;
  clearChatNotifications: (p: {
    stompClient: Client;
    receiverEmail: string;
  }) => void;
  clearPostNotifications: (p: {
    stompClient: Client;
    receiverEmail: string;
  }) => void;
  clearRecipeNotifications: (p: {
    stompClient: Client;
    receiverEmail: string;
  }) => void;
  clearPlanNotifications: (p: {
    stompClient: Client;
    receiverEmail: string;
  }) => void;
  clearNotificationsBought: (p: {
    stompClient: Client;
    receiverEmail: string;
  }) => void;
  removePostNotificationByAppId: (appId: number, stompClient: Client) => void;
  removeRecipeNotificationByAppId: (appId: number, stompClient: Client) => void;
  removePlanNotificationByAppId: (appId: number, stompClient: Client) => void;
  removeNotificationBought: (p: BoughtPayloadStomp) => void;
  removeBySender: (params: {
    senderEmail: string;
    receiverEmail: string;
    stompClient: Client;
  }) => void;
  chatNotificationsGroupedBySender: {
    notifications: { [p: string]: ChatMessageNotificationResponse[] };
    total: number;
    totalSenders: number;
  };
  stompClient: Client | undefined;
  pathName: string;
  getPostNotificationState: () => NotificationState<
    NotificationPostResponse,
    ApprovedNotificationType,
    ApprovePostNotificationResponse
  >;
  getRecipeNotificationState: () => NotificationState<
    NotificationRecipeResponse,
    ApprovedNotificationType,
    ApproveRecipeNotificationResponse
  >;
  getPlanNotificationState: () => NotificationState<
    NotificationPlanResponse,
    ApprovedNotificationType,
    ApprovePlanNotificationResponse
  >;
  getBoughtNotificationState: () => NotificationState<
    PlanResponse,
    "NEW_BOUGHT",
    BoughtNotificationResponse
  >;
  archiveQueueNotifications: NotifyContainerAction[];
  deleteArchiveNotification: (id: string) => void;
  deleteManyArchiveNotifications: (ids: string[]) => void;
  deleteAllArchiveNotifications: () => void;
}
export const NotificationPopContext =
  createContext<NotificationPopContextType | null>(null);

export function NotificationPopProvider({
  children,
}: NotificationPopProviderProps) {
  const session = useSession();
  const authUser = session.data?.user;
  const locale = useLocale();

  const [notificationPopTexts, setNotificationPopTexts] =
    useState<NotificationPopTexts | null>(null);

  const {
    notifications: archiveQueueNotifications,
    deleteNotification: deleteArchiveNotification,
    deleteAllNotifications: deleteAllArchiveNotifications,
    deleteManyNotifications: deleteManyArchiveNotifications,
  } = useArchiveNotifications(authUser);

  const [chatMessageNotificationTexts, setChatMessageNotificationTexts] =
    useState<Record<string, ChatMessageNotificationContentTexts> | null>(null);
  const previousChatMsgGroupedBySender = useRef<
    ReturnType<typeof getNotificationsGroupedBySender>["notifications"] | null
  >(null);

  const [postMessageNotificationsTexts, setPostMessageNotificationsTexts] =
    useState<Record<string, ApproveNotificationContentTexts> | null>(null);
  const previousPostNotifications = useRef<
    ReturnType<typeof getPostNotificationState>["notifications"] | null
  >(null);

  const [recipeMessageNotificationsTexts, setRecipeMessageNotificationsTexts] =
    useState<Record<string, ApproveNotificationContentTexts> | null>(null);
  const previousRecipeNotifications = useRef<
    ReturnType<typeof getRecipeNotificationState>["notifications"] | null
  >(null);

  const [planMessageNotificationsTexts, setPlanMessageNotificationsTexts] =
    useState<Record<string, ApproveNotificationContentTexts> | null>(null);
  const previousPlanNotifications = useRef<
    ReturnType<typeof getPlanNotificationState>["notifications"] | null
  >(null);

  const [boughtNotificationTexts, setBoughtNotificationTexts] = useState<Record<
    string,
    BoughtNotificationContentTexts
  > | null>(null);
  const previousBoughtNotifications = useRef<
    ReturnType<typeof getBoughtNotificationState>["notifications"] | null
  >(null);

  const {
    getNotificationsGroupedBySender,
    getTotals: getChatTotals,
    clearNotifications: clearChatNotifications,
  } = useChatNotification();
  const {
    getTotals: getPostTotals,
    getNotificationState: getPostNotificationState,
    removeByAppId: removePostNotificationByAppId,
    clearNotifications: clearPostNotifications,
  } = usePostApproveNotification();

  const {
    getTotals: getRecipeTotals,
    getNotificationState: getRecipeNotificationState,
    removeByAppId: removeRecipeNotificationByAppId,
    clearNotifications: clearRecipeNotifications,
  } = useRecipeApproveNotification();
  const {
    getTotals: getPlanTotals,
    getNotificationState: getPlanNotificationState,
    removeByAppId: removePlanNotificationByAppId,
    clearNotifications: clearPlanNotifications,
  } = usePlanApproveNotification();

  const {
    getTotals: getBoughtTotals,
    getNotificationState: getBoughtNotificationState,
    removeNotificationBought,
    clearNotificationsBought,
  } = useBoughtNotification();

  const totalChatNotifications = useMemo(
    () => getChatTotals().total,
    [getChatTotals().total],
  );
  const totalPostNotifications = useMemo(
    () => getPostTotals().total,
    [getPostTotals().total],
  );
  const totalRecipeNotifications = useMemo(
    () => getRecipeTotals().total,
    [getRecipeTotals().total],
  );
  const totalPlanNotifications = useMemo(
    () => getPlanTotals().total,
    [getPlanTotals().total],
  );
  const totalBoughtNotifications = useMemo(
    () => getBoughtTotals().total,
    [getBoughtTotals().total],
  );
  const totalNotifications = useMemo(
    () =>
      totalChatNotifications +
      totalPostNotifications +
      totalRecipeNotifications +
      totalPlanNotifications +
      totalBoughtNotifications +
      archiveQueueNotifications.length,
    [
      totalChatNotifications,
      totalPostNotifications,
      totalRecipeNotifications,
      totalPlanNotifications,
      totalBoughtNotifications,
      archiveQueueNotifications.length,
    ],
  );
  const chatNotificationsGroupedBySender = getNotificationsGroupedBySender();
  const stompClient = useStompClient();
  const { removeBySender } = useChatNotification();
  const pathName = usePathname();

  useEffect(() => {
    getNotificationPopTexts(totalNotifications).then(setNotificationPopTexts);
  }, [totalNotifications, locale]);

  useEffect(() => {
    if (
      chatMessageNotificationTexts === null ||
      previousChatMsgGroupedBySender.current === null ||
      !isEqual(
        chatNotificationsGroupedBySender.notifications,
        previousChatMsgGroupedBySender.current,
      )
    ) {
      previousChatMsgGroupedBySender.current =
        chatNotificationsGroupedBySender.notifications;
      getChatMessageNotificationTextsForReceiver(
        chatNotificationsGroupedBySender.notifications,
      ).then(setChatMessageNotificationTexts);
    }
  }, [
    chatMessageNotificationTexts,
    chatNotificationsGroupedBySender.notifications,
    locale,
  ]);

  useEffect(() => {
    if (
      postMessageNotificationsTexts === null ||
      previousPostNotifications.current === null ||
      !isEqual(
        getPostNotificationState().notifications,
        previousPostNotifications.current,
      )
    ) {
      previousPostNotifications.current =
        getPostNotificationState().notifications;

      getApprovedNotificationTextsByItems(
        "post",
        getPostNotificationState().notifications,
      ).then((texts) => {
        setPostMessageNotificationsTexts(texts);
      });
    }
  }, [
    getPostNotificationState,
    JSON.stringify(getPostNotificationState().notifications),
    postMessageNotificationsTexts,
    locale,
  ]);

  useEffect(() => {
    if (
      recipeMessageNotificationsTexts === null ||
      previousRecipeNotifications.current === null ||
      !isEqual(
        getRecipeNotificationState().notifications,
        previousRecipeNotifications.current,
      )
    ) {
      previousRecipeNotifications.current =
        getRecipeNotificationState().notifications;

      getApprovedNotificationTextsByItems(
        "recipe",
        getRecipeNotificationState().notifications,
      ).then(setRecipeMessageNotificationsTexts);
    }
  }, [
    getRecipeNotificationState,
    recipeMessageNotificationsTexts,
    JSON.stringify(getRecipeNotificationState().notifications),
    locale,
  ]);

  useEffect(() => {
    if (
      planMessageNotificationsTexts === null ||
      previousPlanNotifications.current === null ||
      !isEqual(
        getPlanNotificationState().notifications,
        previousPlanNotifications.current,
      )
    ) {
      previousPlanNotifications.current =
        getPlanNotificationState().notifications;

      getApprovedNotificationTextsByItems(
        "plan",
        getPlanNotificationState().notifications,
      ).then(setPlanMessageNotificationsTexts);
    }
  }, [
    getPlanNotificationState,
    JSON.stringify(getPlanNotificationState().notifications),
    planMessageNotificationsTexts,
    locale,
  ]);

  useEffect(() => {
    if (
      boughtNotificationTexts === null ||
      previousBoughtNotifications.current === null ||
      !isEqual(
        getBoughtNotificationState().notifications,
        previousBoughtNotifications.current,
      )
    ) {
      previousBoughtNotifications.current =
        getBoughtNotificationState().notifications;

      getBoughtNotificationTextsByItems(
        getBoughtNotificationState().notifications,
      ).then(setBoughtNotificationTexts);
    }
  }, [
    boughtNotificationTexts,
    getBoughtNotificationState,
    JSON.stringify(getBoughtNotificationState().notifications),
    locale,
  ]);

  const content = (
    <NotificationPopContext.Provider
      value={{
        notificationPopTexts,
        setNotificationPopTexts,
        chatMessageNotificationTexts,
        setChatMessageNotificationTexts,
        postMessageNotificationsTexts,
        setPostMessageNotificationsTexts,
        recipeMessageNotificationsTexts,
        setRecipeMessageNotificationsTexts,
        planMessageNotificationsTexts,
        setPlanMessageNotificationsTexts,
        boughtNotificationTexts,
        setBoughtNotificationTexts,
        totalNotifications,
        totalChatNotifications,
        totalPostNotifications,
        totalRecipeNotifications,
        totalPlanNotifications,
        totalBoughtNotifications,
        clearChatNotifications,
        clearPostNotifications,
        clearRecipeNotifications,
        clearPlanNotifications,
        clearNotificationsBought,
        removePostNotificationByAppId,
        removeRecipeNotificationByAppId,
        removePlanNotificationByAppId,
        removeNotificationBought,
        removeBySender,
        chatNotificationsGroupedBySender,
        stompClient,
        pathName,
        getPlanNotificationState,
        getRecipeNotificationState,
        getPostNotificationState,
        getBoughtNotificationState,
        archiveQueueNotifications,
        deleteArchiveNotification,
        deleteManyArchiveNotifications,
        deleteAllArchiveNotifications,
      }}
    >
      {children}
    </NotificationPopContext.Provider>
  );
  if (!authUser || !authUser.email) return content;

  return <NotificationPopProviderUser authUser={authUser} content={content} />;
}
interface NotificationPopProviderUserProps extends WithUser {
  content: ReactNode;
}

function NotificationPopProviderUser({
  authUser,
  content,
}: NotificationPopProviderUserProps) {
  // const stompClient = useStompClient();
  // const router = useRouter();
  // const { removeBySender } = useChatNotification();
  // useSubscription(`/queue/chat-changed-${authUser?.email}`, (message) => {
  //   const newMessage = JSON.parse(message.body) as ConversationUserResponse;
  //
  //   // navigate to chat room after it is created
  //   if (newMessage.connectedChatRoom?.id) {
  //     const sender = newMessage.connectedChatRoom?.users.find(
  //       (user) => user.email !== authUser?.email,
  //     );
  //     if (sender && stompClient && stompClient.connected) {
  //       removeBySender({
  //         stompClient,
  //         senderEmail: sender.email,
  //         receiverEmail: authUser?.email || "",
  //       });
  //       //todo vezi aici
  //       // router.push(`/chat/?chatId=${newMessage.connectedChatRoom?.id}`);
  //     }
  //   }
  // });

  return content;
}

export function useNotificationPop() {
  const context = useContext(NotificationPopContext);
  if (!context) {
    throw new Error(
      "useNotificationPop must be used within a NotificationPopProvider",
    );
  }
  return context;
}
