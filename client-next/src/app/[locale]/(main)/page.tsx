import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { HomeHeaderTexts } from "@/components/home/home-header";
import { getHomeTexts } from "@/texts/pages";
import HomeCards, { HomeCardsText } from "@/components/home/home-cards";
import HomeAbout, { HomeAboutTexts } from "@/components/home/home-about";
import HomeTimeline, {
  HomeTimelineTexts,
} from "@/components/home/home-timeline";
import { HomeHero, HomeHeroTexts } from "@/components/home/home-hero";
import {
  HomeTestimonials,
  HomeTestimonialsTexts,
  TypeTestimonials,
} from "@/components/home/home-testimonials";
import person1 from "@/../public/images/people/person1.jpg";
import person2 from "@/../public/images/people/person2.jpg";
import person3 from "@/../public/images/people/person3.jpg";
import person4 from "@/../public/images/people/person4.jpg";

interface Props {
  params: { locale: Locale };
}

export interface HomeTexts {
  homeHeaderTexts: HomeHeaderTexts;
  homeCardsTexts: HomeCardsText;
  homeAboutTexts: HomeAboutTexts;
  homeTimelineTexts: HomeTimelineTexts;
  homeHeroTexts: HomeHeroTexts;
  homeTestimonialsTexts: HomeTestimonialsTexts;
}
export default async function Home({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);

  const {
    homeHeaderTexts,
    homeCardsTexts,
    homeAboutTexts,
    homeTimelineTexts,
    homeHeroTexts,
    homeTestimonialsTexts,
  } = await getHomeTexts();

  const personImages = [person1, person2, person3, person4];
  const testimonials = Array.from({ length: 4 }).map((_, i) => {
    const key = `testimonial${i + 1}` as TypeTestimonials;
    return {
      quote: homeTestimonialsTexts.testimonials[key].quote,
      name: homeTestimonialsTexts.testimonials[key].name,
      pleasure: homeTestimonialsTexts.testimonials[key].pleasure,
      src: personImages[i],
    };
  });

  return (
    <main className="  w-full  space-y-5">
      {/*<HomeHeader {...homeHeaderTexts} />*/}
      <HomeHero {...homeHeroTexts} />
      <HomeCards {...homeCardsTexts} />
      <div className="h-8 lg:h-14" />
      <HomeAbout {...homeAboutTexts} />
      <HomeTimeline {...homeTimelineTexts} />
      <div className="h-10 md:h-14" />
      <HomeTestimonials
        testimonials={testimonials}
        title={homeTestimonialsTexts.title}
      />
    </main>
  );
}
