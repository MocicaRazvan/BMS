import { cn } from "@/lib/utils";
import { Inter as FontSans } from "next/font/google";
import { Locale, locales } from "@/navigation";
import { ThemeProvider } from "@/providers/theme-provider";
import { NextAuthSessionProvider } from "@/providers/session-provider";
import { Toaster } from "@/components/ui/toaster";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import { StompProvider } from "@/providers/stomp-provider";
import { ChatProvider } from "@/context/chat-context";
import { ChatMessageNotificationProvider } from "@/context/chat-message-notification-context";
import { NextIntlClientProvider } from "next-intl";
import { PostApproveNotificationProvider } from "@/context/post-approve-notification-context";
import { RecipeApproveNotificationProvider } from "@/context/recipe-approve-notification-context";
import { PlanApproveNotificationProvider } from "@/context/plan-approve-notification-context";
import { CartProvider } from "@/context/cart-context";
import { SubscriptionProvider } from "@/context/subscriptions-context";
import { BoughtNotificationProvider } from "@/context/bought-notification-context";
import ScrollTopProvider from "@/providers/scroll-top";
import { loadGlobalModel } from "@/actions/toxcity";
import { vectorStoreInstance } from "@/lib/langchain/langchain";
import { getAiChatBoxTexts } from "@/texts/components/ai-chat";
import ValidUserSessionContext from "@/context/valid-user-session";
import { NotificationPopProvider } from "@/context/notification-pop-context";
import { AiChatBoxWrapper } from "@/components/ai-chat/ai-chat-box-wrapper";
import { KanbanRouteChangeProvider } from "@/context/kanban-route-change-context";

const fontSans = FontSans({
  subsets: ["latin"],
  variable: "--font-sans",
});

interface Props {
  children: React.ReactNode;
  params: { locale: Locale };
}

export function generateStaticParams() {
  return locales.map((locale) => ({ locale }));
}

export default async function BaseLayout({
  children,
  params: { locale },
}: Props) {
  const spring = process.env.NEXT_PUBLIC_SPRING_CLIENT!;

  let session;
  let aiTexts;

  if (process.env.NODE_ENV === "production") {
    const [sessionP, tf, lg, aiTextsP] = await Promise.all([
      getServerSession(authOptions),
      loadGlobalModel(),
      vectorStoreInstance.initialize(false, false),
      getAiChatBoxTexts(),
    ]);
    session = sessionP;
    aiTexts = aiTextsP;
  } else {
    const [sessionP, lg, aiTextsP] = await Promise.all([
      getServerSession(authOptions),
      vectorStoreInstance.initialize(false, false),
      getAiChatBoxTexts(),
    ]);
    session = sessionP;
    aiTexts = aiTextsP;
  }

  return (
    <html lang={locale} suppressHydrationWarning>
      <body
        className={cn(
          " bg-background font-sans antialiased ",
          fontSans.variable,
        )}
      >
        <NextIntlClientProvider locale={locale}>
          <ThemeProvider
            attribute="class"
            defaultTheme="system"
            enableSystem
            disableTransitionOnChange
          >
            <NextAuthSessionProvider>
              <ScrollTopProvider>
                <ValidUserSessionContext>
                  <StompProvider
                    url={spring + "/ws/ws-service"}
                    authUser={session?.user}
                  >
                    <ChatProvider authUser={session?.user}>
                      <ChatMessageNotificationProvider authUser={session?.user}>
                        <PostApproveNotificationProvider
                          authUser={session?.user}
                        >
                          <RecipeApproveNotificationProvider
                            authUser={session?.user}
                          >
                            <PlanApproveNotificationProvider
                              authUser={session?.user}
                            >
                              <BoughtNotificationProvider
                                authUser={session?.user}
                              >
                                <NotificationPopProvider
                                  authUser={session?.user}
                                >
                                  <CartProvider>
                                    <SubscriptionProvider
                                      authUser={session?.user}
                                    >
                                      <KanbanRouteChangeProvider>
                                        <>
                                          {children}
                                          <AiChatBoxWrapper {...aiTexts} />
                                        </>
                                      </KanbanRouteChangeProvider>
                                    </SubscriptionProvider>
                                  </CartProvider>
                                </NotificationPopProvider>
                              </BoughtNotificationProvider>
                            </PlanApproveNotificationProvider>
                          </RecipeApproveNotificationProvider>
                        </PostApproveNotificationProvider>
                      </ChatMessageNotificationProvider>
                    </ChatProvider>
                  </StompProvider>
                </ValidUserSessionContext>
              </ScrollTopProvider>
            </NextAuthSessionProvider>
            <Toaster />
          </ThemeProvider>
        </NextIntlClientProvider>
      </body>
    </html>
  );
}
