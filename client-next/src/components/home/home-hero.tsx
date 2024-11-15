import {
  HeroParallax,
  HeroParallaxTexts,
} from "@/components/aceternityui/hero-parallax";
import picture1 from "../../../public/images/Picture1.jpg";
import picture2 from "../../../public/images/Picture2.jpg";
import picture3 from "../../../public/images/Picture3.jpg";
import picture4 from "../../../public/images/Picture4.jpg";
import picture5 from "../../../public/images/Picture5.jpg";
import picture6 from "../../../public/images/Picture6.jpg";
import picture7 from "../../../public/images/Picture7.jpg";
import picture8 from "../../../public/images/Picture8.jpg";
import picture9 from "../../../public/images/Picture9.jpg";
import picture10 from "../../../public/images/Picture10.jpg";
import { appendCreatedAtDesc } from "@/lib/utils";
export type TitleKeys = `title${1 | 2 | 3 | 4 | 5 | 6 | 7 | 8 | 9 | 10}`;
export interface HomeHeroTexts extends HeroParallaxTexts {
  titles: Record<TitleKeys, string>;
}
export function HomeHero({ titles, ...rest }: HomeHeroTexts) {
  const pictures = [
    picture1,
    picture2,
    picture3,
    picture4,
    picture5,
    picture6,
    picture7,
    picture8,
    picture9,
    picture10,
  ];
  console.log("titles", titles);
  const products = Array.from({ length: 10 }).map((_, i) => ({
    title: titles[`titles.title${i + 1}` as TitleKeys],
    link: appendCreatedAtDesc(i % 2 ? "/plans/approved" : "/posts/approved"),
    thumbnail: pictures[i],
  }));
  return <HeroParallax products={products} texts={rest} />;
}
