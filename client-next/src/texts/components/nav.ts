"use server";
import { getTranslations } from "next-intl/server";
import { ChatMessageNotificationContentTexts } from "@/components/nav/chat-notitifications-content";
import {
  ApprovedNotificationType,
  ApproveModelNotificationResponse,
  ApproveNotificationResponse,
  BoughtNotificationResponse,
  ChatMessageNotificationResponse,
} from "@/types/dto";
import { ApproveNotificationContentTexts } from "@/components/nav/approve-notifications-content";
import { cn } from "@/lib/utils";
import { createElement } from "react";
import { NotificationPopTexts } from "@/components/nav/notification-pop";
import { NavTexts } from "@/components/nav/nav";
import { CartPopsTexts } from "@/components/nav/cart-pop";
import { BoughtNotificationContentTexts } from "@/components/nav/bought-notification-content";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

export interface ThemeSwitchTexts {
  srButton: string;
  light: string;
  dark: string;
  system: string;
}

export async function getThemeSwitchTexts(): Promise<ThemeSwitchTexts> {
  const t = await getTranslations("components.nav.ThemeSwitchTexts");
  return {
    srButton: t("srButton"),
    light: t("light"),
    dark: t("dark"),
    system: t("system"),
  };
}

export interface TextWithId {
  id: string;
  text: string;
}

export async function getMenuLinkTexts(mainKey: string, keys: string[]) {
  const t = await getTranslations(`components.nav.${mainKey}`);
  return keys.reduce(
    (acc, k) => ({ ...acc, [k]: t(k) }),
    {} as Record<string, string>,
  );
}

export async function getNavTexts(): Promise<NavTexts> {
  const [
    themeSwitchTexts,
    postsTexts,
    recipesTexts,
    plansTexts,
    findInSiteTexts,
    links,
  ] = await Promise.all([
    getThemeSwitchTexts(),
    getMenuLinkTexts("PostsTexts", [
      "approvedPosts",
      "allPosts",
      "yourPosts",
      "createPost",
    ]),
    getMenuLinkTexts("RecipesTexts", [
      "allRecipes",
      "yourRecipes",
      "createRecipe",
    ]),
    getMenuLinkTexts("PlansTexts", [
      "allPlans",
      "yourPlans",
      "createPlan",
      "approvedPlans",
      "monthlyPlan",
      "dailyPlan",
    ]),
    getFindInSiteTexts(),
    getTranslations("components.nav.Links"),
  ]);
  return {
    themeSwitchTexts,
    postsTexts,
    recipesTexts,
    plansTexts,
    findInSiteTexts,
    dayCalendarCTATexts: {
      header: links("dayCalendarCTA"),
    },
    links: {
      posts: links("posts"),
      home: links("home"),
      adminDashboard: links("adminDashboard"),
      chat: links("chat"),
      ingredients: links("ingredients"),
      recipes: links("recipes"),
      plans: links("plans"),
      subscriptions: links("subscriptions"),
      orders: links("orders"),
      kanban: links("kanban"),
      trainerDashboard: links("trainerDashboard"),
      calculator: links("calculator"),
    },
  };
}

export async function getChatMessageNotificationContentTexts(
  count: number,
  sender: string,
): Promise<ChatMessageNotificationContentTexts> {
  const t = await getTranslations(
    "components.nav.ChatMessageNotificationContentTexts",
  );
  return { content: t("content", { count, sender }) };
}

export async function getChatMessageNotificationTextsForReceiver(notifications: {
  [p: string]: ChatMessageNotificationResponse[];
}): Promise<Record<string, ChatMessageNotificationContentTexts>> {
  const entries = await Promise.all(
    Object.entries(notifications).map(async ([sender, notif]) => {
      const texts = await getChatMessageNotificationContentTexts(
        notif.length,
        sender,
      );
      return [sender, texts] as [string, ChatMessageNotificationContentTexts];
    }),
  );

  return Object.fromEntries(entries);
}

export async function getApproveNotificationContentTexts(
  type: "post" | "recipe" | "plan",
  approved: ApprovedNotificationType,
  title: string,
): Promise<ApproveNotificationContentTexts> {
  const t = await getTranslations(
    "components.nav.ApproveNotificationContentTexts",
  );
  const intlType = t(`type.${type}`);
  const intlApproved = t(`approved.${approved.toLocaleLowerCase()}`);
  return {
    title: t.rich("title", {
      type: intlType,
      approved: intlApproved,
      app: (chunks) =>
        createElement(
          "span",
          {
            className: cn(
              "ms-2",
              approved === "APPROVED" ? "text-success" : "text-destructive",
            ),
          },
          chunks,
        ),
    }),
    content: t("content", {
      title,
      approved: intlApproved,
    }),
  };
}

export async function getBoughtNotificationContentTexts(
  title: string,
): Promise<BoughtNotificationContentTexts> {
  const t = await getTranslations(
    "components.nav.BoughtNotificationContentTexts",
  );
  return { title: t("title"), content: t("content", { title }) };
}

export async function getApprovedNotificationTextsByItems<
  T extends ApproveModelNotificationResponse,
  I extends ApproveNotificationResponse<T>,
>(
  type: "post" | "recipe" | "plan",
  items: I[],
): Promise<Record<string, ApproveNotificationContentTexts>> {
  const entries = await Promise.all(
    items.map(async (item) => {
      const content = JSON.parse(item.content);
      const texts = await getApproveNotificationContentTexts(
        type,
        item.type,
        content?.title || "",
      );
      return [item.id.toString(), texts] as [
        string,
        ApproveNotificationContentTexts,
      ];
    }),
  );

  return Object.fromEntries(entries);
}

export async function getBoughtNotificationTextsByItems(
  items: BoughtNotificationResponse[],
): Promise<Record<string, BoughtNotificationContentTexts>> {
  const entries = await Promise.all(
    items.map(async (item) => {
      const content = JSON.parse(item.content);
      const texts = await getBoughtNotificationContentTexts(
        content?.title || "",
      );
      return [item.id.toString(), texts] as [
        string,
        BoughtNotificationContentTexts,
      ];
    }),
  );

  return Object.fromEntries(entries);
}

export async function getNotificationPopTexts(
  count: number,
): Promise<NotificationPopTexts> {
  const t = await getTranslations("components.nav.NotificationPopTexts");
  return {
    tooltip: t("tooltip"),
    title: t("title"),
    header: t("header", { count }),
    clearButton: t("clearButton"),
    openAllButton: t("openAllButton"),
    closeAllButton: t("closeAllButton"),
    messages: t("messages"),
    posts: t("posts"),
    recipes: t("recipes"),
    plans: t("plans"),
    bought: t("bought"),
    archive: t("archive"),
  };
}
export async function getCartPopsTexts(): Promise<CartPopsTexts> {
  const t = await getTranslations("components.nav.CartPopsTexts");
  return {
    checkout: t("checkout"),
    toastDescription: t("toastDescription"),
    total: t("total"),
    undo: t("undo"),
    emptyCart: t("emptyCart"),
  };
}

export async function getFindInSiteTexts(): Promise<FindInSiteTexts> {
  const t = await getTranslations("components.nav.FindInSiteTexts");
  return {
    navigateText: t("navigateText"),
    closeText: t("closeText"),
    selectText: t("selectText"),
    title: t("title"),
    noResults: t("noResults"),
    placeholder: t("placeholder"),
    pressText: t("pressText"),
  };
}
