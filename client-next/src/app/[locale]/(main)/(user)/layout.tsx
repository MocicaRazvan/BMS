import { ReactNode } from "react";
import { unstable_setRequestLocale } from "next-intl/server";
import { Locale, redirect } from "@/navigation";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";

export default async function UserLayout({
  children,
  params: { locale },
}: {
  children: ReactNode;
  params: { locale: Locale };
}) {
  unstable_setRequestLocale(locale);
  const session = await getServerSession(authOptions);

  if (!session || !session.user) {
    return redirect("/auth/signin");
  }

  return <>{children}</>;
}
