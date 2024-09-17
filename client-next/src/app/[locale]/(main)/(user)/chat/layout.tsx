import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";

export default function ChatLayout({
  children,
  params: { locale },
}: Readonly<{ children: React.ReactNode; params: { locale: Locale } }>) {
  unstable_setRequestLocale(locale);
  return children;
}
