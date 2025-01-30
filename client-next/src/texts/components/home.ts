import { HomeHeaderTexts } from "@/components/home/home-header";
import { getTranslations } from "next-intl/server";
import { HomeCardsText } from "@/components/home/home-cards";
import { HomeAboutTexts } from "@/components/home/home-about";
import { HomeTimelineTexts } from "@/components/home/home-timeline";
import { HomeHeroTexts, TitleKeys } from "@/components/home/home-hero";
import {
  HomeTestimonialsTexts,
  TypeTestimonials,
} from "@/components/home/home-testimonials";
import { Testimonial } from "@/components/aceternityui/animated-testimonials";

export async function getHomeHeaderTexts(): Promise<HomeHeaderTexts> {
  const t = await getTranslations("components.home.HomeHeaderTexts");
  return {
    title: t("title"),
  };
}
export async function getHomeHeroTexts(): Promise<HomeHeroTexts> {
  const t = await getTranslations("components.home.HomeHeroTexts");
  return {
    title: t.rich("title"),
    description: t("description"),
    community: t("community"),
    platform: t("platform"),
    titles: Array.from({ length: 10 }).reduce(
      (acc, _, i) => {
        const key = `titles.title${i + 1}` as TitleKeys;
        return Object.assign(acc as object, { [key]: t(key) });
      },
      {} as Record<TitleKeys, string>,
    ) as Record<TitleKeys, string>,
  };
}

export async function getHomeTestimonialsTexts(): Promise<HomeTestimonialsTexts> {
  const t = await getTranslations("components.home.HomeTestimonialsTexts");
  return {
    title: t("title"),
    testimonials: Array.from({ length: 4 }).reduce((acc, _, i) => {
      const key = `testimonial${i + 1}`;
      return Object.assign(acc as object, {
        [key]: {
          quote: t(`testimonials.${key}.quote`),
          name: t(`testimonials.${key}.name`),
          pleasure: t(`testimonials.${key}.pleasure`),
        },
      });
    }, {}) as Record<TypeTestimonials, Omit<Testimonial, "src">>,
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
