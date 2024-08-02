import { ReactNode } from "react";
import { getUserWithMinRole } from "@/lib/user";
import { unstable_setRequestLocale } from "next-intl/server";
import { Locale } from "@/navigation";

export default async function UserLayout({
  children,
  params: { locale },
}: {
  children: ReactNode;
  params: { locale: Locale };
}) {
  unstable_setRequestLocale(locale);
  const user = await getUserWithMinRole("ROLE_USER");
  console.log("authUser", user);
  return <>{children}</>;
}
