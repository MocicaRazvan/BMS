import { getTranslations } from "next-intl/server";
import { ElementHeaderTexts } from "@/components/common/element-header";
import { RadioSortTexts } from "@/components/common/radio-sort";
import { NutritionalTableTexts } from "@/components/common/nutritional-table";
import { getIngredientTableColumnTexts } from "@/texts/components/table";
import { ArchiveQueueCardsTexts } from "@/components/common/archive-queue-card";
import {
  ArchiveQueuePrefix,
  archiveQueuePrefixes,
  ContainerAction,
} from "@/types/dto";
import {
  MonthPicker,
  MonthPickerTexts,
} from "@/components/common/month-picker";
import { ImageCropTexts } from "@/components/common/image-cropper";
import { ArchiveQueueUpdateTexts } from "@/context/archive-queue-update-context";

export async function getElementHeaderTexts(): Promise<ElementHeaderTexts> {
  const t = await getTranslations("components.common.ElementHeaderTexts");
  return {
    notApproved: t("notApproved"),
    approved: t("approved"),
  };
}

export async function getRadioSortTexts(): Promise<RadioSortTexts> {
  const t = await getTranslations("components.common.RadioSortTexts");
  return {
    noSort: t("noSort"),
  };
}

export async function getNutritionalTableTexts(): Promise<NutritionalTableTexts> {
  const [ingredientColumnTexts, t] = await Promise.all([
    getIngredientTableColumnTexts(),
    getTranslations("components.common.NutritionalTableTexts"),
  ]);

  return {
    tableCaption: t("tableCaption"),
    ingredientColumnTexts,
  };
}

export async function getArchiveQueueTitleForPrefixes(): Promise<ArchiveQueueUpdateTexts> {
  const t = await getTranslations("components.common.ArchiveQueueCardsTexts");
  return {
    titles: [
      "comment",
      "day",
      "ingredient",
      "meal",
      "plan",
      "post",
      "recipe",
      "user",
    ].reduce(
      (acc, type) => {
        acc[type as ArchiveQueuePrefix] = {
          delete: t("title.delete", { type }),
          update: t("title.update", { type }),
        };
        return acc;
      },
      {} as ArchiveQueueUpdateTexts["titles"],
    ),
    toastActions: {
      [ContainerAction.START_MANUAL]: t("managePopTexts.toastCron"),
      [ContainerAction.STOP]: t("managePopTexts.toastStop"),
      [ContainerAction.START_CRON]: t("managePopTexts.toastCron"),
    },
  };
}
export async function getArchiveQueueCardsTexts(
  prefix: ArchiveQueuePrefix,
): Promise<ArchiveQueueCardsTexts> {
  const t = await getTranslations("components.common.ArchiveQueueCardsTexts");
  const type = t(`types.${prefix}`);
  return {
    title: {
      delete: t("title.delete", { type }),
      update: t("title.update", { type }),
    },
    description: t("description"),
    badgeText: {
      high: t("badgeText.high"),
      medium: t("badgeText.medium"),
      low: t("badgeText.low"),
    },
    refreshButtonText: t("refreshButtonText"),
    errorTitle: t("errorTitle"),
    errorDescription: t("errorDescription"),
    header: t("header"),
    lastRefresh: t("lastRefresh"),
    currentConsumers: t("currentConsumers"),
    managePopTexts: {
      manageBtn: t("managePopTexts.manageBtn"),
      scheduleBtn: t("managePopTexts.scheduleBtn"),
      scheduleBtnLoading: t("managePopTexts.scheduleBtnLoading"),
      selectLabel: t("managePopTexts.selectLabel"),
      stopBtn: t("managePopTexts.stopBtn"),
      stopBtnLoading: t("managePopTexts.stopBtnLoading"),
      stopTooltip: t("managePopTexts.stopTooltip"),
      toastStop: t("managePopTexts.toastStop"),
      toastSchedule: t("managePopTexts.toastSchedule"),
      scheduleTooltip: t("managePopTexts.scheduleTooltip"),
      consumerStoppedDescription: t(
        "managePopTexts.consumerStoppedDescription",
      ),
      toastCron: t("managePopTexts.toastCron"),
    },
  };
}

export async function getAllArchiveQueueCardsTexts(): Promise<
  Record<ArchiveQueuePrefix, ArchiveQueueCardsTexts>
> {
  return (
    await Promise.all(
      archiveQueuePrefixes.map(async (prefix) => ({
        texts: await getArchiveQueueCardsTexts(prefix),
        prefix,
      })),
    )
  ).reduce(
    (acc, { texts, prefix }) => {
      acc[prefix] = texts;
      return acc;
    },
    {} as Record<ArchiveQueuePrefix, ArchiveQueueCardsTexts>,
  );
}

export async function getMonthPickerTexts(): Promise<MonthPickerTexts> {
  const t = await getTranslations("components.common.MonthPickerTexts");
  return {
    placeholder: t("placeholder"),
  };
}

export async function getImageCropTexts(): Promise<ImageCropTexts> {
  const t = await getTranslations("components.common.ImageCropTexts");
  return {
    buttonText: t("buttonText"),
    tooltipText: t("tooltipText"),
  };
}
