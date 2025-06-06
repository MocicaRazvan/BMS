"use client";
import { parseHumanReadable } from "cron-js-parser";
import { convert } from "crontzconvert";
import { Locale } from "@/navigation";

export default function CronDisplay({
  cronExpression,
  locale,
}: {
  cronExpression: string;
  locale: Locale;
}) {
  return (
    <p>
      {parseHumanReadable(
        convert(
          cronExpression,
          "UTC",
          Intl.DateTimeFormat().resolvedOptions().timeZone,
        ),
        {
          // placeholder bug in the library
          runOnWeekDay: {
            dayIndex: 0,
            weekIndex: 0,
            isLastWeek: false,
          },
        },
        locale,
      )}
    </p>
  );
}
