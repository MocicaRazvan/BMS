import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getTheSameUserOrAdmin } from "@/lib/user";
import { ReactNode } from "react";

interface Props {
  params: { locale: Locale; id: string };
  children: ReactNode;
}

export default async function UserItemsLayout({
  params: { locale, id },
  children,
}: Props) {
  unstable_setRequestLocale(locale);
  const user = await getTheSameUserOrAdmin(id);
  return <>{children}</>;
}
