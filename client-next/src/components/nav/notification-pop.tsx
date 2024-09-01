"use client";
import { useChatNotification } from "@/context/chat-message-notification-context";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { Bell, Trash2 } from "lucide-react";
import { cn } from "@/lib/utils";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Fragment, useCallback, useEffect, useMemo, useState } from "react";
import { useStompClient, useSubscription } from "react-stomp-hooks";
import { usePathname, useRouter, Link } from "@/navigation";
import {
  ChatMessageNotificationResponse,
  ConversationUserResponse,
} from "@/types/dto";
import { Session } from "next-auth";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { usePostApproveNotification } from "@/context/post-approve-notification-context";
import ChatNotificationsContent, {
  ChatMessageNotificationContentTexts,
} from "@/components/nav/chat-notitifications-content";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { Badge } from "@/components/ui/badge";
import ApproveNotificationContent, {
  ApproveNotificationContentTexts,
} from "@/components/nav/approve-notifications-content";
import { Client } from "@stomp/stompjs";
import {
  getApprovedNotificationTextsByItems,
  getBoughtNotificationTextsByItems,
  getChatMessageNotificationContentTexts,
  getChatMessageNotificationTextsForReceiver,
  getNotificationPopTexts,
} from "@/texts/components/nav";
import { useRecipeApproveNotification } from "@/context/recipe-approve-notification-context";
import { usePlanApproveNotification } from "@/context/plan-approve-notification-context";
import BoughtNotificationContent, {
  BoughtNotificationContentTexts,
} from "@/components/nav/bought-notification-content";
import { useBoughtNotification } from "@/context/bought-notification-context";

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

  const [notificationPopTexts, setNotificationPopTexts] =
    useState<NotificationPopTexts | null>(null);
  const [chatMessageNotificationTexts, setChatMessageNotificationTexts] =
    useState<Record<string, ChatMessageNotificationContentTexts> | null>(null);
  const [postMessageNotificationsTexts, setPostMessageNotificationsTexts] =
    useState<Record<string, ApproveNotificationContentTexts> | null>(null);
  const [recipeMessageNotificationsTexts, setRecipeMessageNotificationsTexts] =
    useState<Record<string, ApproveNotificationContentTexts> | null>(null);
  const [planMessageNotificationsTexts, setPlanMessageNotificationsTexts] =
    useState<Record<string, ApproveNotificationContentTexts> | null>(null);
  const [boughtNotificationTexts, setBoughtNotificationTexts] = useState<Record<
    string,
    BoughtNotificationContentTexts
  > | null>(null);

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

  const totalChatNotifications = getChatTotals().total;
  const totalPostNotifications = getPostTotals().total;
  const totalRecipeNotifications = getRecipeTotals().total;
  const totalPlanNotifications = getPlanTotals().total;
  const totalBoughtNotifications = getBoughtTotals().total;
  const totalNotifications =
    totalChatNotifications +
    totalPostNotifications +
    totalRecipeNotifications +
    totalPlanNotifications +
    totalBoughtNotifications;
  const chatNotificationsGroupedBySender = getNotificationsGroupedBySender();
  const stompClient = useStompClient();
  const router = useRouter();
  // const { setActiveChatId } = useChatContext();
  const { removeBySender } = useChatNotification();
  const pathName = usePathname();

  useEffect(() => {
    getNotificationPopTexts(totalNotifications).then(setNotificationPopTexts);
  }, [totalNotifications]);

  useEffect(() => {
    getChatMessageNotificationTextsForReceiver(
      chatNotificationsGroupedBySender.notifications,
    ).then(setChatMessageNotificationTexts);
  }, [JSON.stringify(chatNotificationsGroupedBySender.notifications)]);

  useEffect(() => {
    getApprovedNotificationTextsByItems(
      "post",
      getPostNotificationState().notifications,
    ).then(setPostMessageNotificationsTexts);
  }, [JSON.stringify(getPostNotificationState().notifications)]);

  useEffect(() => {
    getApprovedNotificationTextsByItems(
      "recipe",
      getRecipeNotificationState().notifications,
    ).then(setRecipeMessageNotificationsTexts);
  }, [JSON.stringify(getRecipeNotificationState().notifications)]);

  useEffect(() => {
    getApprovedNotificationTextsByItems(
      "plan",
      getPlanNotificationState().notifications,
    ).then(setPlanMessageNotificationsTexts);
  }, [JSON.stringify(getPlanNotificationState().notifications)]);

  useEffect(() => {
    getBoughtNotificationTextsByItems(
      getBoughtNotificationState().notifications,
    ).then(setBoughtNotificationTexts);
  }, [JSON.stringify(getBoughtNotificationState().notifications)]);

  useSubscription(`/user/${authUser.email}/chat/changed`, (message) => {
    const newMessage = JSON.parse(message.body) as ConversationUserResponse;
    console.log("newMessage chat notif", newMessage);
    console.log("not path", pathName);

    // navigate to chat room after it is created
    if (newMessage.connectedChatRoom?.id) {
      // router.push(`/chat/?chatId=${newMessage.connectedChatRoom?.id}`);
      // setActiveChatId(newMessage.connectedChatRoom?.id);
      // router.push(`/chat`);
      const sender = newMessage.connectedChatRoom?.users.find(
        (user) => user.email !== authUser.email,
      );
      if (sender && stompClient && stompClient.connected) {
        removeBySender({
          stompClient,
          senderEmail: sender.email,
          receiverEmail: authUser.email,
        });
        router.push(`/chat/?chatId=${newMessage.connectedChatRoom?.id}`);
        // if (pathName === "/chat") {
        //   router.refresh();
        // } else {
        //   router.push(`/chat`);
        // }
      }
    }
  });

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

  return (
    <DropdownMenu modal={false}>
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
            <ScrollArea className="w-full max-h-[400px]">
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
            </ScrollArea>
          </DropdownMenuGroup>
        </DropdownMenuContent>
      )}
    </DropdownMenu>
  );
}
