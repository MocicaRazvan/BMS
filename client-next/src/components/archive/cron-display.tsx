"use client";
import cronstrue from "cronstrue";
import "cronstrue/locales/ro";
import "cronstrue/locales/en";
import convert from "cron-timezone-converter";
import { Locale } from "@/navigation/navigation";
import { memo } from "react";

const CronDisplay = memo(
  ({ cronExpression, locale }: { cronExpression: string; locale: Locale }) => {
    return (
      <p>
        {cronstrue.toString(
          convert(
            cronExpression,
            "UTC",
            Intl.DateTimeFormat().resolvedOptions().timeZone,
            true,
          ),
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
