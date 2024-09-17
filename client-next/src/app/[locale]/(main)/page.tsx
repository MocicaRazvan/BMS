import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import HomeHeader, { HomeHeaderTexts } from "@/components/home/home-header";
import { getHomeTexts } from "@/texts/pages";
import HomeCards, { HomeCardsText } from "@/components/home/home-cards";
import HomeAbout, { HomeAboutTexts } from "@/components/home/home-about";
import HomeTimeline, {
  HomeTimelineTexts,
} from "@/components/home/home-timeline";

interface Props {
  params: { locale: Locale };
}

export interface HomeTexts {
  homeHeaderTexts: HomeHeaderTexts;
  homeCardsTexts: HomeCardsText;
  homeAboutTexts: HomeAboutTexts;
  homeTimelineTexts: HomeTimelineTexts;
}
export default async function Home({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);

  const { homeHeaderTexts, homeCardsTexts, homeAboutTexts, homeTimelineTexts } =
    await getHomeTexts();

  return (
    <main className="  w-full  space-y-5">
      <HomeHeader {...homeHeaderTexts} />
      <HomeCards {...homeCardsTexts} />
      <div className="h-8 lg:h-14" />
      <HomeAbout {...homeAboutTexts} />
      <HomeTimeline {...homeTimelineTexts} />
    </main>
  );
}
