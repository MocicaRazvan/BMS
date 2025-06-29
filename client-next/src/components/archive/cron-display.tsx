"use client";
import cronstrue from "cronstrue";
import "cronstrue/locales/ro";
import "cronstrue/locales/en";
import convert from "cron-timezone-converter";
import { Locale } from "@/navigation/navigation";
import { memo } from "react";

const userTimeZone =
  typeof Intl !== "undefined"
    ? Intl.DateTimeFormat().resolvedOptions().timeZone
    : "UTC";

const CronDisplay = memo(
  ({ cronExpression, locale }: { cronExpression: string; locale: Locale }) => {
    return (
      <p>
        {cronstrue.toString(
          convert(cronExpression, "UTC", userTimeZone, true),
          {
            locale,
            use24HourTimeFormat: true,
            verbose: true,
          },
        )}
      </p>
    );
  },
);

CronDisplay.displayName = "CronDisplay";
export default CronDisplay;
