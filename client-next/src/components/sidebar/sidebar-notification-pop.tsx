"use client";

import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import NotificationPop from "@/components/nav/notification-pop";

export default function SidebarNotificationPop() {
  const { authUser } = useAuthUserMinRole();
  return <NotificationPop authUser={authUser} />;
}
