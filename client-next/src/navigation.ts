import { createSharedPathnamesNavigation } from "next-intl/navigation";

export type Locale = "en" | "ro";
export const locales: Locale[] = ["en", "ro"];

export interface LocaleProps {
  params: {
    locale: Locale;
  };
}

export const { Link, redirect, usePathname, useRouter } =
  createSharedPathnamesNavigation({ locales });
