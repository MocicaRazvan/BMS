import { ReactNode } from "react";
import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";

export default function AuthLayout({
  children,
  params: { locale },
}: {
  children: ReactNode;
  params: { locale: Locale };
}) {
  unstable_setRequestLocale(locale);
  return <div>{children}</div>;
}
