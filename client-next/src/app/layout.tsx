import { ReactNode } from "react";
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
import { AbstractIntlMessages, NextIntlClientProvider } from "next-intl";
import { PostApproveNotificationProvider } from "@/context/post-approve-notification-context";
import { RecipeApproveNotificationProvider } from "@/context/recipe-approve-notification-context";
import { PlanApproveNotificationProvider } from "@/context/plan-approve-notification-context";
import { CartProvider } from "@/context/cart-context";
import { SubscriptionProvider } from "@/context/subscriptions-context";
import { BoughtNotificationProvider } from "@/context/bought-notification-context";
import ScrollTopProvider from "@/providers/scroll-top";
import type { Metadata } from "next";

const fontSans = FontSans({
  subsets: ["latin"],
  variable: "--font-sans",
});
export const metadata: Metadata = {
  icons: {
    icon: [
      {
        url: "/images/logo-light.svg", // /public path
        href: "/images/logo-light.svg", // /public path
      },
    ],
  },
};
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
  const session = await getServerSession(authOptions);

  return (
    <html lang={locale} suppressHydrationWarning>
      <head>
        <link rel="icon" href="/public/logo-light.ico" sizes="any" />
      </head>
      <body
        className={cn(
          " bg-background font-sans antialiased ",
          fontSans.variable,
        )}
      >
        <NextIntlClientProvider
          // messages={messages.NotFoundPage as AbstractIntlMessages}
          locale={locale}
        >
          <ThemeProvider
            attribute="class"
            defaultTheme="system"
            enableSystem
            disableTransitionOnChange
          >
            <NextAuthSessionProvider>
              <StompProvider
                url={spring + "/ws/ws-service"}
                authUser={session?.user}
              >
                <ScrollTopProvider>
                  <ChatProvider authUser={session?.user}>
                    <ChatMessageNotificationProvider authUser={session?.user}>
                      <PostApproveNotificationProvider authUser={session?.user}>
                        <RecipeApproveNotificationProvider
                          authUser={session?.user}
                        >
                          <PlanApproveNotificationProvider
                            authUser={session?.user}
                          >
                            <BoughtNotificationProvider
                              authUser={session?.user}
                            >
                              <CartProvider>
                                <SubscriptionProvider authUser={session?.user}>
                                  {children}
                                </SubscriptionProvider>
                              </CartProvider>
                            </BoughtNotificationProvider>
                          </PlanApproveNotificationProvider>
                        </RecipeApproveNotificationProvider>
                      </PostApproveNotificationProvider>
                    </ChatMessageNotificationProvider>
                  </ChatProvider>
                </ScrollTopProvider>
              </StompProvider>
              <Toaster />
            </NextAuthSessionProvider>
          </ThemeProvider>
        </NextIntlClientProvider>
      </body>
    </html>
  );
}
