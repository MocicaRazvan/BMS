import { DateRangePickerTexts } from "@/components/ui/date-range-picker";
import { getTranslations } from "next-intl/server";

export async function getDateRangePickerTexts(): Promise<DateRangePickerTexts> {
  const t = await getTranslations("components.ui.DateRangePickerTexts");
  const presets = [
    "today",
    "yesterday",
    "last7",
    "last14",
    "last30",
    "thisWeek",
    "lastWeek",
    "thisMonth",
    "lastMonth",
    "pastYear",
  ];

  return {
    cancel: t("cancel"),
    compare: t("compare"),
    update: t("update"),
    presets: {
      ...presets.reduce(
        (acc, cur) => ({ ...acc, [cur]: t(`presets.${cur}`) }),
        {},
      ),
    },
  };
}
