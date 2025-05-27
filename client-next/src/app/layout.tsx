import { cn } from "@/lib/utils";
import { Inter as FontSans } from "next/font/google";
import { Locale } from "@/navigation";
import { ThemeProvider } from "@/providers/theme-provider";
import { NextAuthSessionProvider } from "@/providers/session-provider";
import { Toaster } from "@/components/ui/toaster";
import { StompProvider } from "@/providers/stomp-provider";
import { ChatMessageNotificationProvider } from "@/context/chat-message-notification-context";
import { PostApproveNotificationProvider } from "@/context/post-approve-notification-context";
import { RecipeApproveNotificationProvider } from "@/context/recipe-approve-notification-context";
import { PlanApproveNotificationProvider } from "@/context/plan-approve-notification-context";
import { CartProvider } from "@/context/cart-context";
import { SubscriptionProvider } from "@/context/subscriptions-context";
import { BoughtNotificationProvider } from "@/context/bought-notification-context";
import ScrollTopProvider from "@/providers/scroll-top";
import { getAiChatBoxTexts } from "@/texts/components/ai-chat";
import ValidUserSessionContext from "@/context/valid-user-session";
import { NotificationPopProvider } from "@/context/notification-pop-context";
import { AiChatBoxWrapper } from "@/components/ai-chat/ai-chat-box-wrapper";
import { KanbanRouteChangeProvider } from "@/context/kanban-route-change-context";
import { NavigationGuardProvider } from "next-navigation-guard";
import { CacheProvider } from "@/providers/cache-provider";
import { Metadata } from "next";
import ChatConnectContext from "@/context/chat-connect-context";
import { UmamiAnalytics } from "@/lib/umami-analytics";
import ArchiveNotificationsProvider from "@/context/archive-notifications-context";
import { ReactNode } from "react";
import { AbstractIntlMessages, NextIntlClientProvider } from "next-intl";

const fontSans = FontSans({
  subsets: ["latin"],
  variable: "--font-sans",
});

interface Props {
  children: ReactNode;
  params: { locale: Locale };
}

export const metadata: Metadata = {
  icons: {
    icon: [
      {
        media: "(prefers-color-scheme: light)",
        url: "/images/logo-dark.svg",
        href: "/images/logo-dark.svg",
      },
      {
        media: "(prefers-color-scheme: dark)",
        url: "/images/logo-light.svg",
        href: "/images/logo-light-dark.svg",
      },
    ],
  },
};

export default async function BaseLayout({
  children,
  params: { locale },
}: Props) {
  const spring = process.env.NEXT_PUBLIC_SPRING_CLIENT!;

  // let aiTexts;

  // if (process.env.NODE_ENV === "production") {
  //   const [lg, aiTextsP] = await Promise.all([
  //     vectorStoreInstance.initialize(false, false),
  //     getAiChatBoxTexts(),
  //   ]);
  //   aiTexts = aiTextsP;
  // } else {
  //   const [lg, aiTextsP] = await Promise.all([
  //     vectorStoreInstance.initialize(false, false),
  //     getAiChatBoxTexts(),
  //   ]);
  //   aiTexts = aiTextsP;
  // }

  const aiTexts = await getAiChatBoxTexts();

  return (
    <html lang={locale} suppressHydrationWarning>
      <UmamiAnalytics />
      <body
        className={cn(
          "bg-background font-sans antialiased ",
          fontSans.variable,
        )}
      >
        <NextIntlClientProvider
          messages={null as unknown as AbstractIntlMessages}
        >
          <CacheProvider>
            <ThemeProvider
              attribute="class"
              defaultTheme="system"
              enableSystem
              disableTransitionOnChange
            >
              <NavigationGuardProvider>
                <NextAuthSessionProvider>
                  <ScrollTopProvider>
                    <ValidUserSessionContext>
                      <StompProvider url={spring + "/ws/ws-service"}>
                        <ChatMessageNotificationProvider>
                          <PostApproveNotificationProvider>
                            <RecipeApproveNotificationProvider>
                              <PlanApproveNotificationProvider>
                                <BoughtNotificationProvider>
                                  <ArchiveNotificationsProvider>
                                    <NotificationPopProvider>
                                      <CartProvider>
                                        <SubscriptionProvider>
                                          <KanbanRouteChangeProvider>
                                            <ChatConnectContext>
                                              <>
                                                {children}
                                                <AiChatBoxWrapper
                                                  {...aiTexts}
                                                />
                                              </>
                                            </ChatConnectContext>
                                          </KanbanRouteChangeProvider>
                                        </SubscriptionProvider>
                                      </CartProvider>
                                    </NotificationPopProvider>
                                  </ArchiveNotificationsProvider>
                                </BoughtNotificationProvider>
                              </PlanApproveNotificationProvider>
                            </RecipeApproveNotificationProvider>
                          </PostApproveNotificationProvider>
                        </ChatMessageNotificationProvider>
                      </StompProvider>
                    </ValidUserSessionContext>
                  </ScrollTopProvider>
                </NextAuthSessionProvider>
                <Toaster />
              </NavigationGuardProvider>
            </ThemeProvider>
          </CacheProvider>
        </NextIntlClientProvider>
      </body>
    </html>
  );
}
