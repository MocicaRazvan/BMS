"use client";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { Bell, Trash2 } from "lucide-react";
import { useEffect, useState } from "react";

import { Session } from "next-auth";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import ChatNotificationsContent from "@/components/nav/chat-notitifications-content";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { Badge } from "@/components/ui/badge";
import ApproveNotificationContent from "@/components/nav/approve-notifications-content";

import BoughtNotificationContent from "@/components/nav/bought-notification-content";
import { useNotificationPop } from "@/context/notification-pop-context";

interface NotificationPopProps {
  authUser: NonNullable<Session["user"]>;
}

enum ACCORDION_ITEMS {
  MESSAGES = "messages",
  POSTS = "posts",
  RECIPES = "recipes",
  PLANS = "plans",
  BOUGHT = "bought",
}

type AccordionsState = Record<ACCORDION_ITEMS, ACCORDION_ITEMS | "">;
const initialAccordionsState: AccordionsState = {
  [ACCORDION_ITEMS.MESSAGES]: "",
  [ACCORDION_ITEMS.POSTS]: "",
  [ACCORDION_ITEMS.RECIPES]: "",
  [ACCORDION_ITEMS.PLANS]: "",
  [ACCORDION_ITEMS.BOUGHT]: "",
};

export interface NotificationPopTexts {
  tooltip: string;
  title: string;
  header: string;
  clearButton: string;
  openAllButton: string;
  closeAllButton: string;
  messages: string;
  posts: string;
  recipes: string;
  plans: string;
  bought: string;
}

export default function NotificationPop({ authUser }: NotificationPopProps) {
  const [accordionsState, setAccordionsState] = useState<AccordionsState>(
    initialAccordionsState,
  );
  const [isAccordionOpen, setIsAccordionOpen] = useState(false);

  // const [notificationPopTexts, setNotificationPopTexts] =
  //   useState<NotificationPopTexts | null>(null);
  //
  // const [chatMessageNotificationTexts, setChatMessageNotificationTexts] =
  //   useState<Record<string, ChatMessageNotificationContentTexts> | null>(null);
  // const previousChatMsgGroupedBySender = useRef<
  //   ReturnType<typeof getNotificationsGroupedBySender>["notifications"] | null
  // >(null);
  //
  // const [postMessageNotificationsTexts, setPostMessageNotificationsTexts] =
  //   useState<Record<string, ApproveNotificationContentTexts> | null>(null);
  // const previousPostNotifications = useRef<
  //   ReturnType<typeof getPostNotificationState>["notifications"] | null
  // >(null);
  //
  // const [recipeMessageNotificationsTexts, setRecipeMessageNotificationsTexts] =
  //   useState<Record<string, ApproveNotificationContentTexts> | null>(null);
  // const previousRecipeNotifications = useRef<
  //   ReturnType<typeof getRecipeNotificationState>["notifications"] | null
  // >(null);
  //
  // const [planMessageNotificationsTexts, setPlanMessageNotificationsTexts] =
  //   useState<Record<string, ApproveNotificationContentTexts> | null>(null);
  // const previousPlanNotifications = useRef<
  //   ReturnType<typeof getPlanNotificationState>["notifications"] | null
  // >(null);
  //
  // const [boughtNotificationTexts, setBoughtNotificationTexts] = useState<Record<
  //   string,
  //   BoughtNotificationContentTexts
  // > | null>(null);
  // const previousBoughtNotifications = useRef<
  //   ReturnType<typeof getBoughtNotificationState>["notifications"] | null
  // >(null);
  //
  // const {
  //   getNotificationsGroupedBySender,
  //   getTotals: getChatTotals,
  //   clearNotifications: clearChatNotifications,
  // } = useChatNotification();
  // const {
  //   getTotals: getPostTotals,
  //   getNotificationState: getPostNotificationState,
  //   removeByAppId: removePostNotificationByAppId,
  //   clearNotifications: clearPostNotifications,
  // } = usePostApproveNotification();
  //
  // const {
  //   getTotals: getRecipeTotals,
  //   getNotificationState: getRecipeNotificationState,
  //   removeByAppId: removeRecipeNotificationByAppId,
  //   clearNotifications: clearRecipeNotifications,
  // } = useRecipeApproveNotification();
  // const {
  //   getTotals: getPlanTotals,
  //   getNotificationState: getPlanNotificationState,
  //   removeByAppId: removePlanNotificationByAppId,
  //   clearNotifications: clearPlanNotifications,
  // } = usePlanApproveNotification();
  //
  // const {
  //   getTotals: getBoughtTotals,
  //   getNotificationState: getBoughtNotificationState,
  //   removeNotificationBought,
  //   clearNotificationsBought,
  // } = useBoughtNotification();
  //
  // const totalChatNotifications = useMemo(
  //   () => getChatTotals().total,
  //   [getChatTotals().total],
  // );
  // const totalPostNotifications = useMemo(
  //   () => getPostTotals().total,
  //   [getPostTotals().total],
  // );
  // const totalRecipeNotifications = useMemo(
  //   () => getRecipeTotals().total,
  //   [getRecipeTotals().total],
  // );
  // const totalPlanNotifications = useMemo(
  //   () => getPlanTotals().total,
  //   [getPlanTotals().total],
  // );
  // const totalBoughtNotifications = useMemo(
  //   () => getBoughtTotals().total,
  //   [getBoughtTotals().total],
  // );
  // const totalNotifications = useMemo(
  //   () =>
  //     totalChatNotifications +
  //     totalPostNotifications +
  //     totalRecipeNotifications +
  //     totalPlanNotifications +
  //     totalBoughtNotifications,
  //   [
  //     totalChatNotifications,
  //     totalPostNotifications,
  //     totalRecipeNotifications,
  //     totalPlanNotifications,
  //     totalBoughtNotifications,
  //   ],
  // );
  // const chatNotificationsGroupedBySender = getNotificationsGroupedBySender();
  // const stompClient = useStompClient();
  // const router = useRouter();
  // // const { setActiveChatId } = useChatContext();
  // const { removeBySender } = useChatNotification();
  // const pathName = usePathname();
  //
  // useEffect(() => {
  //   getNotificationPopTexts(totalNotifications).then(setNotificationPopTexts);
  // }, [totalNotifications]);
  //
  // useEffect(() => {
  //   if (
  //     chatMessageNotificationTexts === null ||
  //     previousChatMsgGroupedBySender.current === null ||
  //     !isEqual(
  //       chatNotificationsGroupedBySender.notifications,
  //       previousChatMsgGroupedBySender.current,
  //     )
  //   ) {
  //     previousChatMsgGroupedBySender.current =
  //       chatNotificationsGroupedBySender.notifications;
  //     getChatMessageNotificationTextsForReceiver(
  //       chatNotificationsGroupedBySender.notifications,
  //     ).then(setChatMessageNotificationTexts);
  //   }
  // }, [
  //   chatMessageNotificationTexts,
  //   chatNotificationsGroupedBySender.notifications,
  // ]);
  //
  // useEffect(() => {
  //   console.error(
  //     "Fetching post notifications...",
  //     postMessageNotificationsTexts,
  //   );
  //   console.error("Prev Post", previousPostNotifications.current);
  //   console.error(
  //     "isEqual",
  //     isEqual(
  //       getPostNotificationState().notifications,
  //       previousPostNotifications.current,
  //     ),
  //   );
  //   console.error("Current Post", getPostNotificationState().notifications);
  //   if (
  //     postMessageNotificationsTexts === null ||
  //     previousPostNotifications.current === null ||
  //     !isEqual(
  //       getPostNotificationState().notifications,
  //       previousPostNotifications.current,
  //     )
  //   ) {
  //     console.error("Setting post notifications...");
  //     previousPostNotifications.current =
  //       getPostNotificationState().notifications;
  //
  //     getApprovedNotificationTextsByItems(
  //       "post",
  //       getPostNotificationState().notifications,
  //     ).then((texts) => {
  //       console.error("TEXTS post", texts);
  //       setPostMessageNotificationsTexts(texts);
  //     });
  //   }
  // }, [
  //   getPostNotificationState,
  //   JSON.stringify(getPostNotificationState().notifications),
  //   postMessageNotificationsTexts,
  // ]);
  //
  // // useEffect(() => {
  // //   if (postMessageNotificationsTexts === null) {
  // //     getApprovedNotificationTextsByItems(
  // //       "post",
  // //       getPostNotificationState().notifications,
  // //     ).then(setPostMessageNotificationsTexts);
  // //   }
  // // }, [postMessageNotificationsTexts, getPostNotificationState]);
  //
  // useEffect(() => {
  //   if (
  //     recipeMessageNotificationsTexts === null ||
  //     previousRecipeNotifications.current === null ||
  //     !isEqual(
  //       getRecipeNotificationState().notifications,
  //       previousRecipeNotifications.current,
  //     )
  //   ) {
  //     previousRecipeNotifications.current =
  //       getRecipeNotificationState().notifications;
  //
  //     getApprovedNotificationTextsByItems(
  //       "recipe",
  //       getRecipeNotificationState().notifications,
  //     ).then(setRecipeMessageNotificationsTexts);
  //   }
  // }, [
  //   getRecipeNotificationState,
  //   recipeMessageNotificationsTexts,
  //   JSON.stringify(getRecipeNotificationState().notifications),
  // ]);
  //
  // // useEffect(() => {
  // //   if (recipeMessageNotificationsTexts === null) {
  // //     getApprovedNotificationTextsByItems(
  // //       "recipe",
  // //       getRecipeNotificationState().notifications,
  // //     ).then(setRecipeMessageNotificationsTexts);
  // //   }
  // // }, [getRecipeNotificationState, recipeMessageNotificationsTexts]);
  //
  // useEffect(() => {
  //   if (
  //     planMessageNotificationsTexts === null ||
  //     previousPlanNotifications.current === null ||
  //     !isEqual(
  //       getPlanNotificationState().notifications,
  //       previousPlanNotifications.current,
  //     )
  //   ) {
  //     previousPlanNotifications.current =
  //       getPlanNotificationState().notifications;
  //
  //     getApprovedNotificationTextsByItems(
  //       "plan",
  //       getPlanNotificationState().notifications,
  //     ).then(setPlanMessageNotificationsTexts);
  //   }
  // }, [
  //   getPlanNotificationState,
  //   JSON.stringify(getPlanNotificationState().notifications),
  //   planMessageNotificationsTexts,
  // ]);
  //
  // // useEffect(() => {
  // //   if (planMessageNotificationsTexts === null) {
  // //     getApprovedNotificationTextsByItems(
  // //       "plan",
  // //       getPlanNotificationState().notifications,
  // //     ).then(setPlanMessageNotificationsTexts);
  // //   }
  // // }, [getPlanNotificationState, planMessageNotificationsTexts]);
  //
  // useEffect(() => {
  //   if (
  //     boughtNotificationTexts === null ||
  //     previousBoughtNotifications.current === null ||
  //     !isEqual(
  //       getBoughtNotificationState().notifications,
  //       previousBoughtNotifications.current,
  //     )
  //   ) {
  //     previousBoughtNotifications.current =
  //       getBoughtNotificationState().notifications;
  //
  //     getBoughtNotificationTextsByItems(
  //       getBoughtNotificationState().notifications,
  //     ).then(setBoughtNotificationTexts);
  //   }
  // }, [
  //   boughtNotificationTexts,
  //   getBoughtNotificationState,
  //   JSON.stringify(getBoughtNotificationState().notifications),
  // ]);
  //
  // // useEffect(() => {
  // //   if (boughtNotificationTexts === null) {
  // //     getBoughtNotificationTextsByItems(
  // //       getBoughtNotificationState().notifications,
  // //     ).then(setBoughtNotificationTexts);
  // //   }
  // // }, [boughtNotificationTexts, getBoughtNotificationState]);
  //
  // // useEffect(() => {
  // //   console.error(
  // //     "Fetching post notifications...",
  // //     postMessageNotificationsTexts,
  // //   );
  // //   if (
  // //     postMessageNotificationsTexts === null ||
  // //     !isEqual(
  // //       getPostNotificationState().notifications,
  // //       previousPostNotifications.current,
  // //     )
  // //   ) {
  // //     console.error("Setting post notifications...");
  // //     previousPostNotifications.current =
  // //       getPostNotificationState().notifications;
  // //     getApprovedNotificationTextsByItems(
  // //       "post",
  // //       getPostNotificationState().notifications,
  // //     ).then(setPostMessageNotificationsTexts);
  // //   }
  // // }, [getPostNotificationState, postMessageNotificationsTexts]);
  // //
  // // useEffect(() => {
  // //   if (postMessageNotificationsTexts === null) {
  // //     getApprovedNotificationTextsByItems(
  // //       "post",
  // //       getPostNotificationState().notifications,
  // //     ).then(setPostMessageNotificationsTexts);
  // //   }
  // // }, [postMessageNotificationsTexts, getPostNotificationState]);
  // //
  // // useEffect(() => {
  // //   if (
  // //     recipeMessageNotificationsTexts === null ||
  // //     !isEqual(
  // //       getRecipeNotificationState().notifications,
  // //       previousRecipeNotifications.current,
  // //     )
  // //   ) {
  // //     previousRecipeNotifications.current =
  // //       getRecipeNotificationState().notifications;
  // //     getApprovedNotificationTextsByItems(
  // //       "recipe",
  // //       getRecipeNotificationState().notifications,
  // //     ).then(setRecipeMessageNotificationsTexts);
  // //   }
  // // }, [getRecipeNotificationState, recipeMessageNotificationsTexts]);
  // //
  // // useEffect(() => {
  // //   if (recipeMessageNotificationsTexts === null) {
  // //     getApprovedNotificationTextsByItems(
  // //       "recipe",
  // //       getRecipeNotificationState().notifications,
  // //     ).then(setRecipeMessageNotificationsTexts);
  // //   }
  // // }, [getRecipeNotificationState, recipeMessageNotificationsTexts]);
  // //
  // // useEffect(() => {
  // //   if (
  // //     planMessageNotificationsTexts === null ||
  // //     !isEqual(
  // //       getPlanNotificationState().notifications,
  // //       previousPlanNotifications.current,
  // //     )
  // //   ) {
  // //     previousPlanNotifications.current =
  // //       getPlanNotificationState().notifications;
  // //     getApprovedNotificationTextsByItems(
  // //       "plan",
  // //       getPlanNotificationState().notifications,
  // //     ).then(setPlanMessageNotificationsTexts);
  // //   }
  // // }, [getPlanNotificationState, planMessageNotificationsTexts]);
  // //
  // // useEffect(() => {
  // //   if (planMessageNotificationsTexts === null) {
  // //     getApprovedNotificationTextsByItems(
  // //       "plan",
  // //       getPlanNotificationState().notifications,
  // //     ).then(setPlanMessageNotificationsTexts);
  // //   }
  // // }, [getPlanNotificationState, planMessageNotificationsTexts]);
  // //
  // // useEffect(() => {
  // //   if (
  // //     boughtNotificationTexts === null ||
  // //     !isEqual(
  // //       getBoughtNotificationState().notifications,
  // //       previousBoughtNotifications.current,
  // //     )
  // //   ) {
  // //     previousBoughtNotifications.current =
  // //       getBoughtNotificationState().notifications;
  // //     getBoughtNotificationTextsByItems(
  // //       getBoughtNotificationState().notifications,
  // //     ).then(setBoughtNotificationTexts);
  // //   }
  // // }, [boughtNotificationTexts, getBoughtNotificationState]);
  // //
  // // useEffect(() => {
  // //   if (boughtNotificationTexts === null) {
  // //     getBoughtNotificationTextsByItems(
  // //       getBoughtNotificationState().notifications,
  // //     ).then(setBoughtNotificationTexts);
  // //   }
  // // }, [boughtNotificationTexts, getBoughtNotificationState]);
  //
  // // useSubscription(`/user/${authUser.email}/chat/changed`, (message) => {
  // //   const newMessage = JSON.parse(message.body) as ConversationUserResponse;
  // //   console.log("newMessage chat notif", newMessage);
  // //   console.log("not path", pathName);
  // //
  // //   // navigate to chat room after it is created
  // //   if (newMessage.connectedChatRoom?.id) {
  // //     // router.push(`/chat/?chatId=${newMessage.connectedChatRoom?.id}`);
  // //     // setActiveChatId(newMessage.connectedChatRoom?.id);
  // //     // router.push(`/chat`);
  // //     const sender = newMessage.connectedChatRoom?.users.find(
  // //       (user) => user.email !== authUser.email,
  // //     );
  // //     if (sender && stompClient && stompClient.connected) {
  // //       removeBySender({
  // //         stompClient,
  // //         senderEmail: sender.email,
  // //         receiverEmail: authUser.email,
  // //       });
  // //       router.push(`/chat/?chatId=${newMessage.connectedChatRoom?.id}`);
  // //       // if (pathName === "/chat") {
  // //       //   router.refresh();
  // //       // } else {
  // //       //   router.push(`/chat`);
  // //       // }
  // //     }
  // //   }
  // // });
  // useSubscription(`/queue/chat-changed-${authUser.email}`, (message) => {
  //   const newMessage = JSON.parse(message.body) as ConversationUserResponse;
  //   console.log("newMessage chat notif", newMessage);
  //   console.log("not path", pathName);
  //
  //   // navigate to chat room after it is created
  //   if (newMessage.connectedChatRoom?.id) {
  //     // router.push(`/chat/?chatId=${newMessage.connectedChatRoom?.id}`);
  //     // setActiveChatId(newMessage.connectedChatRoom?.id);
  //     // router.push(`/chat`);
  //     const sender = newMessage.connectedChatRoom?.users.find(
  //       (user) => user.email !== authUser.email,
  //     );
  //     if (sender && stompClient && stompClient.connected) {
  //       removeBySender({
  //         stompClient,
  //         senderEmail: sender.email,
  //         receiverEmail: authUser.email,
  //       });
  //       router.push(`/chat/?chatId=${newMessage.connectedChatRoom?.id}`);
  //       // if (pathName === "/chat") {
  //       //   router.refresh();
  //       // } else {
  //       //   router.push(`/chat`);
  //       // }
  //     }
  //   }
  // });
  const {
    chatMessageNotificationTexts,
    chatNotificationsGroupedBySender,
    notificationPopTexts,
    setNotificationPopTexts,
    boughtNotificationTexts,
    setBoughtNotificationTexts,
    postMessageNotificationsTexts,
    setPostMessageNotificationsTexts,
    recipeMessageNotificationsTexts,
    clearRecipeNotifications,
    planMessageNotificationsTexts,
    clearPlanNotifications,
    clearNotificationsBought,
    clearChatNotifications,
    removeNotificationBought,
    clearPostNotifications,
    totalBoughtNotifications,
    removePlanNotificationByAppId,
    totalChatNotifications,
    totalPostNotifications,
    totalRecipeNotifications,
    totalPlanNotifications,
    removePostNotificationByAppId,
    removeRecipeNotificationByAppId,
    totalNotifications,
    removeBySender,
    setChatMessageNotificationTexts,
    setPlanMessageNotificationsTexts,
    setRecipeMessageNotificationsTexts,
    stompClient,
    pathName,
    getPlanNotificationState,
    getRecipeNotificationState,
    getPostNotificationState,
    getBoughtNotificationState,
  } = useNotificationPop();

  useEffect(() => {
    if (totalNotifications === 0) {
      setIsAccordionOpen(false);
    }
  }, [totalNotifications]);

  const isNotUser = authUser.role !== "ROLE_USER";
  const isOneAccordionOpen = Object.values(accordionsState).some(
    (v) => v !== "",
  );

  if (!notificationPopTexts)
    return (
      <Button variant="outline" disabled={true}>
        <Bell />
      </Button>
    );

  // console.error("NAV AccordingState", accordionsState);
  // console.error(
  //   "POSTS",
  //   getPostNotificationState().notifications,
  //   totalPostNotifications,
  //   postMessageNotificationsTexts !== null,
  // );
  // console.error(
  //   "RECIPES",
  //   getRecipeNotificationState().notifications,
  //   totalRecipeNotifications,
  //   recipeMessageNotificationsTexts !== null,
  // );
  // console.error(
  //   "PLANS",
  //   getPlanNotificationState().notifications,
  //   totalPlanNotifications,
  //   planMessageNotificationsTexts !== null,
  // );
  // console.error(
  //   "BOUGHT",
  //   getBoughtNotificationState().notifications,
  //   totalBoughtNotifications,
  //   boughtNotificationTexts !== null,
  // );
  // console.error(
  //   "CHAT",
  //   chatNotificationsGroupedBySender.notifications,
  //   totalChatNotifications,
  //   chatMessageNotificationTexts !== null,
  // );

  return (
    <DropdownMenu
      modal={false}
      open={isAccordionOpen}
      onOpenChange={(v) => setIsAccordionOpen(v)}
    >
      <DropdownMenuTrigger asChild>
        <div className="relative">
          <TooltipProvider disableHoverableContent>
            <Tooltip>
              <TooltipTrigger asChild>
                <span tabIndex={0}>
                  <Button variant="outline" disabled={totalNotifications === 0}>
                    <Bell />
                  </Button>
                </span>
              </TooltipTrigger>
              {totalNotifications === 0 && (
                <TooltipContent side="bottom" className="w-32">
                  <p className="text-center font-semibold">
                    {notificationPopTexts.tooltip}
                  </p>
                </TooltipContent>
              )}
            </Tooltip>
          </TooltipProvider>
          {totalNotifications > 0 && (
            <div className="absolute top-[-2px] right-[-10px] rounded-full w-7 h-7 bg-destructive flex items-center justify-center">
              <p>{totalNotifications}</p>
            </div>
          )}
        </div>
      </DropdownMenuTrigger>
      {totalNotifications > 0 && (
        <DropdownMenuContent className={"max-w-[460px] w-screen p-4 ps-5"}>
          <div className=" mb-4 space-y-4 w-full">
            <div className="space-y-1">
              <h3 className="text-xl font-semibold">
                {notificationPopTexts.title}
              </h3>
              <p className="text-sm text-gray-600 dark:text-gray-400">
                {notificationPopTexts.header}
              </p>
            </div>
            <div className="flex w-full justify-between items-center">
              <Button
                variant="outline"
                className="border-destructive text-destructive h-[40px]"
                onClick={() => {
                  if (stompClient && stompClient.connected) {
                    clearPostNotifications({
                      stompClient,
                      receiverEmail: authUser.email,
                    });
                    clearChatNotifications({
                      stompClient,
                      receiverEmail: authUser.email,
                    });
                    clearRecipeNotifications({
                      stompClient,
                      receiverEmail: authUser.email,
                    });
                    clearPlanNotifications({
                      stompClient,
                      receiverEmail: authUser.email,
                    });

                    clearNotificationsBought({
                      stompClient,
                      receiverEmail: authUser.email,
                    });
                  }
                }}
              >
                <span className="font-semibold mr-2">
                  {notificationPopTexts.clearButton}
                </span>
                <Trash2 className="w-4 h-4" />
              </Button>
              <Button
                variant={"outline"}
                className="h-[40px]"
                onClick={() => {
                  if (isOneAccordionOpen) {
                    setAccordionsState(initialAccordionsState);
                  } else {
                    setAccordionsState((prev) =>
                      Object.keys(prev).reduce<AccordionsState>(
                        (acc, cur) => ({
                          ...acc,
                          [cur]: cur,
                        }),
                        {} as AccordionsState,
                      ),
                    );
                  }
                }}
              >
                {isOneAccordionOpen
                  ? notificationPopTexts.closeAllButton
                  : notificationPopTexts.openAllButton}
              </Button>
            </div>
          </div>
          <hr className="border my-3" />

          <DropdownMenuGroup>
            <div className="w-full max-h-[400px] pe-2.5 overflow-y-auto">
              {totalChatNotifications > 0 &&
                chatMessageNotificationTexts &&
                (isNotUser ? (
                  <Accordion
                    type={"single"}
                    collapsible
                    value={accordionsState.messages}
                    disabled={!chatMessageNotificationTexts}
                    onValueChange={(v) =>
                      setAccordionsState(
                        (prev) =>
                          ({
                            ...prev,
                            messages: v,
                          }) as AccordionsState,
                      )
                    }
                  >
                    <AccordionItem value={ACCORDION_ITEMS.MESSAGES}>
                      <div className="flex items-center justify-between gap-14 w-full">
                        <div className="w-1/2">
                          <AccordionTrigger className="hover:no-underline group">
                            <div className="flex justify-start items-center gap-3 ">
                              <h4 className="font-bold group-hover:underline">
                                {notificationPopTexts.messages}
                              </h4>
                              <Badge
                                variant={"destructive"}
                                className="hover:bg-destructive "
                              >
                                {totalChatNotifications}
                              </Badge>
                            </div>
                          </AccordionTrigger>
                        </div>
                        <Button
                          variant="outline"
                          size={"icon"}
                          className="border-destructive text-destructive w-10 h-10"
                          onClick={() => {
                            if (stompClient && stompClient.connected) {
                              clearChatNotifications({
                                stompClient,
                                receiverEmail: authUser.email,
                              });
                            }
                          }}
                        >
                          <Trash2 size={18} />
                        </Button>
                      </div>
                      <AccordionContent className=" p-2">
                        <ChatNotificationsContent
                          notifications={
                            chatNotificationsGroupedBySender.notifications
                          }
                          chatMessageNotificationTexts={
                            chatMessageNotificationTexts
                          }
                        />
                      </AccordionContent>
                    </AccordionItem>
                  </Accordion>
                ) : (
                  <ChatNotificationsContent
                    notifications={
                      chatNotificationsGroupedBySender.notifications
                    }
                    chatMessageNotificationTexts={chatMessageNotificationTexts}
                  />
                ))}
              {totalPostNotifications > 0 &&
                postMessageNotificationsTexts &&
                isNotUser && (
                  <Accordion
                    type={"single"}
                    collapsible
                    value={accordionsState.posts}
                    onValueChange={(v) =>
                      setAccordionsState(
                        (prev) =>
                          ({
                            ...prev,
                            posts: v,
                          }) as AccordionsState,
                      )
                    }
                  >
                    <AccordionItem value={ACCORDION_ITEMS.POSTS}>
                      <div className="flex items-center justify-between gap-14 w-full">
                        <div className="w-1/2">
                          <AccordionTrigger className="hover:no-underline group">
                            <div className="flex justify-start items-center gap-3 ">
                              <h4 className="font-bold group-hover:underline">
                                {notificationPopTexts.posts}
                              </h4>
                              <Badge
                                variant={"destructive"}
                                className="hover:bg-destructive hover:!no-underline"
                              >
                                {totalPostNotifications}
                              </Badge>
                            </div>
                          </AccordionTrigger>
                        </div>
                        <Button
                          variant="outline"
                          size={"icon"}
                          className="border-destructive text-destructive w-10 h-10"
                          onClick={() => {
                            if (stompClient && stompClient.connected) {
                              clearPostNotifications({
                                stompClient,
                                receiverEmail: authUser.email,
                              });
                            }
                          }}
                        >
                          <Trash2 size={18} />
                        </Button>
                      </div>
                      <AccordionContent className=" p-2">
                        <ApproveNotificationContent
                          items={getPostNotificationState().notifications}
                          itemName="Post"
                          deleteCallback={removePostNotificationByAppId}
                          itemsText={postMessageNotificationsTexts}
                        />
                      </AccordionContent>
                    </AccordionItem>
                  </Accordion>
                )}
              {totalRecipeNotifications > 0 &&
                recipeMessageNotificationsTexts &&
                isNotUser && (
                  <Accordion
                    type={"single"}
                    collapsible
                    value={accordionsState.recipes}
                    onValueChange={(v) =>
                      setAccordionsState(
                        (prev) =>
                          ({
                            ...prev,
                            recipes: v,
                          }) as AccordionsState,
                      )
                    }
                  >
                    <AccordionItem value={ACCORDION_ITEMS.RECIPES}>
                      <div className="flex items-center justify-between gap-14 w-full">
                        <div className="w-1/2">
                          <AccordionTrigger className="hover:no-underline group">
                            <div className="flex justify-start items-center gap-3 ">
                              <h4 className="font-bold group-hover:underline">
                                {notificationPopTexts.recipes}
                              </h4>
                              <Badge
                                variant={"destructive"}
                                className="hover:bg-destructive hover:!no-underline"
                              >
                                {totalRecipeNotifications}
                              </Badge>
                            </div>
                          </AccordionTrigger>
                        </div>
                        <Button
                          variant="outline"
                          size={"icon"}
                          className="border-destructive text-destructive w-10 h-10"
                          onClick={() => {
                            if (stompClient && stompClient.connected) {
                              clearRecipeNotifications({
                                stompClient,
                                receiverEmail: authUser.email,
                              });
                            }
                          }}
                        >
                          <Trash2 size={18} />
                        </Button>
                      </div>
                      <AccordionContent className=" p-2">
                        <ApproveNotificationContent
                          items={getRecipeNotificationState().notifications}
                          itemName="Recipe"
                          deleteCallback={removeRecipeNotificationByAppId}
                          itemsText={recipeMessageNotificationsTexts}
                        />
                      </AccordionContent>
                    </AccordionItem>
                  </Accordion>
                )}
              {totalPlanNotifications > 0 &&
                planMessageNotificationsTexts &&
                isNotUser && (
                  <Accordion
                    type={"single"}
                    collapsible
                    value={accordionsState.plans}
                    onValueChange={(v) =>
                      setAccordionsState(
                        (prev) =>
                          ({
                            ...prev,
                            plans: v,
                          }) as AccordionsState,
                      )
                    }
                  >
                    <AccordionItem value={ACCORDION_ITEMS.PLANS}>
                      {" "}
                      <div className="flex items-center justify-between gap-14 w-full">
                        <div className="w-1/2">
                          <AccordionTrigger className="hover:no-underline group">
                            <div className="flex justify-start items-center gap-3 ">
                              <h4 className="font-bold group-hover:underline">
                                {notificationPopTexts.plans}
                              </h4>
                              <Badge
                                variant={"destructive"}
                                className="hover:bg-destructive hover:!no-underline"
                              >
                                {totalPlanNotifications}
                              </Badge>
                            </div>
                          </AccordionTrigger>
                        </div>
                        <Button
                          variant="outline"
                          size={"icon"}
                          className="border-destructive text-destructive w-10 h-10"
                          onClick={() => {
                            if (stompClient && stompClient.connected) {
                              clearPlanNotifications({
                                stompClient,
                                receiverEmail: authUser.email,
                              });
                            }
                          }}
                        >
                          <Trash2 size={18} />
                        </Button>
                      </div>
                      <AccordionContent className=" p-2">
                        <ApproveNotificationContent
                          items={getPlanNotificationState().notifications}
                          itemName="Plans"
                          deleteCallback={removePlanNotificationByAppId}
                          itemsText={planMessageNotificationsTexts}
                        />
                      </AccordionContent>
                    </AccordionItem>
                  </Accordion>
                )}
              {totalBoughtNotifications > 0 &&
                boughtNotificationTexts &&
                isNotUser && (
                  <Accordion
                    type={"single"}
                    collapsible
                    value={accordionsState.bought}
                    onValueChange={(v) =>
                      setAccordionsState(
                        (prev) =>
                          ({
                            ...prev,
                            bought: v,
                          }) as AccordionsState,
                      )
                    }
                  >
                    <AccordionItem value={ACCORDION_ITEMS.BOUGHT}>
                      {" "}
                      <div className="flex items-center justify-between gap-14 w-full">
                        <div className="w-1/2">
                          <AccordionTrigger className="hover:no-underline group">
                            <div className="flex justify-start items-center gap-3 ">
                              <h4 className="font-bold group-hover:underline">
                                {notificationPopTexts.bought}
                              </h4>
                              <Badge
                                variant={"destructive"}
                                className="hover:bg-destructive hover:!no-underline"
                              >
                                {totalBoughtNotifications}
                              </Badge>
                            </div>
                          </AccordionTrigger>
                        </div>
                        <Button
                          variant="outline"
                          size={"icon"}
                          className="border-destructive text-destructive w-10 h-10"
                          onClick={() => {
                            if (stompClient && stompClient.connected) {
                              clearNotificationsBought({
                                stompClient,
                                receiverEmail: authUser.email,
                              });
                            }
                          }}
                        >
                          <Trash2 size={18} />
                        </Button>
                      </div>
                      <AccordionContent className=" p-2">
                        <BoughtNotificationContent
                          items={getBoughtNotificationState().notifications}
                          deleteCallback={removeNotificationBought}
                          itemsText={boughtNotificationTexts}
                        />
                      </AccordionContent>
                    </AccordionItem>
                  </Accordion>
                )}
            </div>
          </DropdownMenuGroup>
        </DropdownMenuContent>
      )}
    </DropdownMenu>
  );
}
