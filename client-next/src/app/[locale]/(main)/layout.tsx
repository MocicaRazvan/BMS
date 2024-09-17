import { ReactNode } from "react";
import { unstable_setRequestLocale } from "next-intl/server";
import { Locale } from "@/navigation";
import NavWrapper from "@/components/nav/nav-wrapper";
import Footer from "@/components/footer/footer";

export default function MainLayout({
  children,
  params: { locale },
}: {
  children: ReactNode;
  params: { locale: Locale };
}) {
  unstable_setRequestLocale(locale);

  return (
    <div>
      <NavWrapper />
      {children}
      <Footer />
    </div>
  );
}
