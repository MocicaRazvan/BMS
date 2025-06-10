import { Locale } from "@/navigation/navigation";

export interface ReadingTimeText {
  default: string;
  less: string;
}

export const readingTimes: Record<Locale, ReadingTimeText> = {
  ro: {
    less: "timp de citire mai puțin de un minut",
    default: "min. timp de lectură",
  },
  en: {
    less: "less than a minute read",
    default: "min. read",
  },
};

export const getTextFromMinutes = (minutes: number, locale: Locale): string =>
  minutes < 1
    ? readingTimes[locale].less
    : `${minutes} ${readingTimes[locale].default}`;
