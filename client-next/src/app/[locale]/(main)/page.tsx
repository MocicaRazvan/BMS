import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { HomeHeaderTexts } from "@/components/home/home-header";
import { getHomeTexts } from "@/texts/pages";
import HomeCards, { HomeCardsText } from "@/components/home/home-cards";
import { HomeAboutTexts } from "@/components/home/home-about";
import HomeTimeline, {
  HomeTimelineTexts,
} from "@/components/home/home-timeline";
import { HomeHero, HomeHeroTexts } from "@/components/home/home-hero";
import {
  HomeTestimonials,
  HomeTestimonialsTexts,
  TypeTestimonials,
} from "@/components/home/home-testimonials";
import person1 from "@/assets/images/people/person1.jpg";
import person2 from "@/assets/images/people/person2.jpg";
import person3 from "@/assets/images/people/person3.jpg";
import person4 from "@/assets/images/people/person4.jpg";
import Logo from "@/components/logo/logo";
import LogoWall from "@/components/reactbits/logo-wall";
import HomeAboutWrapper from "@/components/home/home-about-wrapper";
import HomeMap, { HomeMapTexts } from "@/components/home/home-map";
import WorldWrapper from "@/components/aceternityui/world-wrapper";
import ScrollProgress from "@/components/common/scroll-progress";
import HomeMapWrapper from "@/components/home/home-map-wrapper";

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
  homeMapTexts: HomeMapTexts;
}

const logoWallArr = Array.from({ length: 8 }, (_, i) => (
  <div
    className="w-full h-full flex justify-center items-center"
    key={i + "-logo-wall-element"}
  >
    <Logo width={70} height={70} />
  </div>
));
const personImages = [person1, person2, person3, person4];
export default async function Home({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);

  const {
    homeHeaderTexts,
    homeCardsTexts,
    homeAboutTexts,
    homeTimelineTexts,
    homeHeroTexts,
    homeTestimonialsTexts,
    homeMapTexts,
  } = await getHomeTexts();

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
    <main className="w-full space-y-5">
      <ScrollProgress />
      {/*<HomeHeader {...homeHeaderTexts} />*/}
      <HomeHero {...homeHeroTexts} />
      <HomeCards {...homeCardsTexts} />
      <div className="h-8 lg:h-14" />
      <HomeAboutWrapper texts={homeAboutTexts} />
      <HomeTimeline {...homeTimelineTexts} />
      {/*<div className="h-10" />*/}
      <HomeMapWrapper {...homeMapTexts} />
      <HomeTestimonials
        testimonials={testimonials}
        title={homeTestimonialsTexts.title}
      />
      <div className="mb-3 mt-5 md:mt-12 pt-0 md:pt-5">
        <LogoWall
          items={logoWallArr}
          size={"clamp(4rem, 1rem + 15vmin, 13rem)"}
          duration={"40s"}
        />
      </div>
    </main>
  );
}
