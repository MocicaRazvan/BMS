"use client";

import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import NotificationPop from "@/components/nav/notification-pop";
import { Locale } from "@/navigation/navigation";

export default function SidebarNotificationPop({ locale }: { locale: Locale }) {
  const { authUser } = useAuthUserMinRole();
  return <NotificationPop authUser={authUser} locale={locale} />;
}
