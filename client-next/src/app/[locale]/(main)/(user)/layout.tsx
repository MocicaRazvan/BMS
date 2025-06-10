import { ReactNode } from "react";
import { unstable_setRequestLocale } from "next-intl/server";
import { Locale } from "@/navigation/navigation";
// import { getServerSession } from "next-auth";
// import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import DayCalendarProvider from "@/context/day-calendar-context";
import { AuthUserMinRoleProvider } from "@/context/auth-user-min-role-context";

export default async function UserLayout({
  children,
  params: { locale },
}: {
  children: ReactNode;
  params: { locale: Locale };
}) {
  unstable_setRequestLocale(locale);
  // const session = await getServerSession(authOptions);
  //
  // if (!session || !session.user) {
  //   return redirect("/auth/signin");
  // }

  return (
    <AuthUserMinRoleProvider minRole="ROLE_USER">
      <DayCalendarProvider>{children}</DayCalendarProvider>
    </AuthUserMinRoleProvider>
  );
}
