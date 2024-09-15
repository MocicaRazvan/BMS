import { HomeHeaderTexts } from "@/components/home/home-header";
import { getTranslations } from "next-intl/server";
import { HomeCardsText } from "@/components/home/home-cards";
import { HomeAboutTexts } from "@/components/home/home-about";
import { HomeTimelineTexts } from "@/components/home/home-timeline";

export async function getHomeHeaderTexts(): Promise<HomeHeaderTexts> {
  const t = await getTranslations("components.home.HomeHeaderTexts");
  return {
    title: t("title"),
  };
}

export async function getHomeCardsText(): Promise<HomeCardsText> {
  const t = await getTranslations("components.home.HomeCardsText");

  return {
    ...["register", "posts", "plans"].reduce(
      (acc, key) => ({
        ...acc,
        [key]: {
          title: t(`${key}.title`),
          description: t(`${key}.description`),
        },
      }),
      {} as HomeCardsText,
    ),
  };
}
export async function getHomeAboutTexts(): Promise<HomeAboutTexts> {
  const t = await getTranslations("components.home.HomeAboutTexts");
  return {
    title: t("title"),
    content1: t("content1"),
    content2: t("content2"),
    content3: t("content3"),
  };
}

export async function getHomeTimelineTexts(): Promise<HomeTimelineTexts> {
  const t = await getTranslations("components.home.HomeTimelineTexts");
  return {
    ...[
      "be-aware",
      "be-afraid",
      "make-a-plan",
      "step-by-step",
      "work-hard",
      "comfortable",
      "stay-consistent",
    ].reduce(
      (acc, cur) => ({
        ...acc,
        [cur]: {
          date: t(`${cur}.date`),
          title: t(`${cur}.title`),
          text: t(`${cur}.text`),
        },
      }),
      {} as HomeTimelineTexts,
    ),
  };
}
