import type { Metadata } from "next";
import "../globals.css";
import { Locale, locales } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getIntlMetadata } from "@/texts/metadata";
import { ReactNode } from "react";

interface Props {
  children: ReactNode;
  params: { locale: Locale };
}

export function generateStaticParams() {
  return locales.map((locale) => ({ locale }));
}

export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  const { title, ...rest } = await getIntlMetadata("LocaleLayout");
  const url = process.env.NEXTAUTH_URL;
  if (!url) {
    throw new Error("NEXTAUTH_URL must be set in the environment");
  }
  const languages = locales.reduce((acc, l) => ({ ...acc, [l]: `/${l}` }), {});

  return {
    title: {
      template: `%s | ${title}`,
      default: title,
    },
    ...rest,
    alternates: {
      canonical: `/`,
      languages,
    },
    metadataBase: new URL(url),
  };
}

export default function LocaleLayout({
  children,
  params: { locale },
}: Readonly<Props>) {
  unstable_setRequestLocale(locale);
  return <div>{children}</div>;
}
