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
import ArchiveQueueNotificationContent from "@/components/nav/archive-queue-notification-content";
import { WithUser } from "@/lib/user";
import { Locale } from "@/navigation/navigation";

interface NotificationPopProps extends WithUser {
  locale: Locale;
}

enum ACCORDION_ITEMS {
  MESSAGES = "messages",
  POSTS = "posts",
  RECIPES = "recipes",
  PLANS = "plans",
  BOUGHT = "bought",
  ARCHIVE = "archive",
}

type AccordionsState = Record<ACCORDION_ITEMS, ACCORDION_ITEMS | "">;
const initialAccordionsState: AccordionsState = {
  [ACCORDION_ITEMS.MESSAGES]: "",
  [ACCORDION_ITEMS.POSTS]: "",
  [ACCORDION_ITEMS.RECIPES]: "",
  [ACCORDION_ITEMS.PLANS]: "",
  [ACCORDION_ITEMS.BOUGHT]: "",
  [ACCORDION_ITEMS.ARCHIVE]: "",
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
  archive: string;
}

export default function NotificationPop({
  authUser,
  locale,
}: NotificationPopProps) {
  const [accordionsState, setAccordionsState] = useState<AccordionsState>(
    initialAccordionsState,
  );
  const [isAccordionOpen, setIsAccordionOpen] = useState(false);

  const {
    chatMessageNotificationTexts,
    chatNotificationsGroupedBySender,
    boughtNotificationTexts,
    notificationPopTexts,
    postMessageNotificationsTexts,
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
    stompClient,
    getPlanNotificationState,
    getRecipeNotificationState,
    getPostNotificationState,
    getBoughtNotificationState,
    archiveQueueNotifications,
    deleteAllArchiveNotifications,
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

  return (
    <DropdownMenu
      modal={false}
      open={isAccordionOpen}
      onOpenChange={(v) => setIsAccordionOpen(v)}
    >
      <DropdownMenuTrigger asChild disabled={totalNotifications === 0}>
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
        <DropdownMenuContent className="max-w-[460px] w-screen p-4 ps-5 resize-y min-h-[140px] max-h-[75vh] overflow-y-auto">
          <div className=" mb-4 space-y-4 w-full">
            <div className="space-y-1">
              <h3 className="text-xl font-semibold">
                {notificationPopTexts.title}
              </h3>
              <p className="text-sm text-muted-foreground">
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

                    deleteAllArchiveNotifications();
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
                          locale={locale}
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
                          locale={locale}
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
                          locale={locale}
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
                          locale={locale}
                        />
                      </AccordionContent>
                    </AccordionItem>
                  </Accordion>
                )}
              {archiveQueueNotifications.length > 0 &&
                authUser?.role === "ROLE_ADMIN" && (
                  <Accordion
                    type={"single"}
                    collapsible
                    value={accordionsState.archive}
                    onValueChange={(v) =>
                      setAccordionsState(
                        (prev) =>
                          ({
                            ...prev,
                            archive: v,
                          }) as AccordionsState,
                      )
                    }
                  >
                    <AccordionItem value={ACCORDION_ITEMS.ARCHIVE}>
                      <div className="flex items-center justify-between gap-14 w-full">
                        <div className="w-1/2">
                          <AccordionTrigger className="hover:no-underline group">
                            <div className="flex justify-start items-center gap-3 ">
                              <h4 className="font-bold group-hover:underline">
                                {notificationPopTexts.archive}
                              </h4>
                              <Badge
                                variant={"destructive"}
                                className="hover:bg-destructive hover:!no-underline"
                              >
                                {archiveQueueNotifications.length}
                              </Badge>
                            </div>
                          </AccordionTrigger>
                        </div>
                        <Button
                          variant="outline"
                          size={"icon"}
                          className="border-destructive text-destructive w-10 h-10"
                          onClick={() => {
                            deleteAllArchiveNotifications();
                          }}
                        >
                          <Trash2 size={18} />
                        </Button>
                      </div>
                      <AccordionContent className=" p-2">
                        <ArchiveQueueNotificationContent authUser={authUser} />
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
