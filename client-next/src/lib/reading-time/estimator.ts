import readingTime from "reading-time";
import { Locale } from "@/navigation/navigation";
import { getTextFromMinutes } from "@/lib/reading-time/i18n";

export function estimateReadingTime(
  input: string,
  wordsPerMinute: number,
  locale: Locale,
) {
  const result = readingTime(input, {
    wordsPerMinute,
  });
  const minutes = Math.ceil(result.minutes);
  const text = getTextFromMinutes(minutes, locale);
  return {
    result: {
      ...result,
      minutes,
    },
    text,
  };
}
