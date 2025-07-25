"use client";
import { ReactNode, useRef } from "react";
import {
  motion,
  MotionValue,
  useScroll,
  useSpring,
  useTransform,
} from "framer-motion";
import Image, { StaticImageData } from "next/image";
import { Link } from "@/navigation/navigation";
import { BlurInOut } from "@/components/common/blur-text";

export interface HeroParallaxTexts {
  title: string | ReactNode;
  platform: string;
  community: string;
  description: string | ReactNode;
}

const HeroParallax = ({
  products,
  texts,
}: {
  products: {
    title: string;
    link: string;
    thumbnail: string | StaticImageData;
  }[];
  texts: HeroParallaxTexts;
}) => {
  const firstRow = products.slice(0, 5);
  const secondRow = products.slice(5, 10);
  const thirdRow = products.slice(10, 15);
  const ref = useRef(null);
  const { scrollYProgress } = useScroll({
    target: ref,
    offset: ["start start", "end start"],
  });

  const springConfig = { stiffness: 300, damping: 30, bounce: 100 };

  const translateX = useSpring(
    useTransform(scrollYProgress, [0, 1], [0, 1000]),
    springConfig,
  );
  const translateXReverse = useSpring(
    useTransform(scrollYProgress, [0, 1], [0, -1000]),
    springConfig,
  );
  const rotateX = useSpring(
    useTransform(scrollYProgress, [0, 0.2], [15, 0]),
    springConfig,
  );
  const opacity = useSpring(
    useTransform(scrollYProgress, [0, 0.2], [0.2, 1]),
    springConfig,
  );
  const rotateZ = useSpring(
    useTransform(scrollYProgress, [0, 0.2], [20, 0]),
    springConfig,
  );
  const translateY = useSpring(
    useTransform(scrollYProgress, [0, 0.2], [-700, 500]),
    springConfig,
  );
  return (
    <div
      ref={ref}
      className="h-[2150px] py-40 lg:pb-36 overflow-hidden antialiased relative flex flex-col self-auto [perspective:1000px] [transform-style:preserve-3d] !transform-gpu"
    >
      <Header {...texts} />
      <motion.div
        style={{
          rotateX,
          rotateZ,
          translateY,
          opacity,
        }}
      >
        <motion.div className="flex flex-row-reverse space-x-reverse space-x-20 mb-20">
          {firstRow.map((product) => (
            <ProductCard
              product={product}
              translate={translateX}
              key={product.title}
            />
          ))}
        </motion.div>
        <motion.div className="flex flex-row  mb-20 space-x-20 ">
          {secondRow.map((product) => (
            <ProductCard
              product={product}
              translate={translateXReverse}
              key={product.title}
            />
          ))}
        </motion.div>
        <motion.div className="flex flex-row-reverse space-x-reverse space-x-20">
          {thirdRow.map((product) => (
            <ProductCard
              product={product}
              translate={translateX}
              key={product.title}
            />
          ))}
        </motion.div>
      </motion.div>
    </div>
  );
};

export const Header = (texts: HeroParallaxTexts) => {
  //todo change
  let firstTitle = texts.title;
  let secondTitle = "";
  if (typeof texts.title === "string") {
    const split = texts.title.split("<br/>");
    firstTitle = split[0];
    secondTitle = split[1] + " ";
  }
  return (
    <div className="max-w-7xl relative mx-auto py-20 md:py-40 px-4 w-full  left-0 top-0 z-[1] pointer-events-none">
      <h1 className="text-2xl md:text-7xl font-bold  w-full py-1">
        {/*<p dangerouslySetInnerHTML={{ __html: `${texts.title}` }} />*/}
        <p>{firstTitle}</p>
        <span className="hidden sm:flex items-center gap-2 md:gap-5 pb-2 ">
          <p>{`${secondTitle} `}</p>
          <BlurInOut
            outText={texts.platform}
            inText={texts.community}
            duration={1.25}
            changeTime={2.5}
            inStartScale={1.12}
          />
        </span>
        <span className="inline sm:hidden">{`${secondTitle} ${texts.community}`}</span>
      </h1>
      <p className="max-w-2xl text-base md:text-xl mt-8 text-primary/90">
        {texts.description}
      </p>
    </div>
  );
};

export const ProductCard = ({
  product,
  translate,
}: {
  product: {
    title: string;
    link: string;
    thumbnail: string | StaticImageData;
  };
  translate: MotionValue<number>;
}) => {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{
        opacity: { duration: 0.37, ease: "easeOut", delay: 0.32 },
      }}
      style={{
        x: translate,
      }}
      whileHover={{
        y: -20,
      }}
      key={product.title}
      className="group/product h-96 w-[30rem] relative flex-shrink-0 rounded-lg"
    >
      <Link
        href={product.link}
        className="block group-hover/product:shadow-2xl rounded-lg"
      >
        <Image
          src={product.thumbnail}
          height="600"
          width="600"
          className="object-cover object-left-top absolute h-full w-full inset-0 rounded-lg"
          alt={product.title}
          priority={true}
        />
      </Link>
      <div className="absolute inset-0 h-full w-full opacity-20  group-hover/product:opacity-0 bg-black pointer-events-none rounded-lg" />
      <div className="absolute inset-0 h-full w-full opacity-0 group-hover/product:opacity-70 bg-black pointer-events-none rounded-lg" />
      <h2 className="absolute bottom-4 left-4 opacity-0 group-hover/product:opacity-100 text-white">
        {product.title}
      </h2>
    </motion.div>
  );
};

export { HeroParallax };
