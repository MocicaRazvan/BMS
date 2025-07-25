"use client";

import { motion, useInView } from "framer-motion";
import WordPullUp from "@/components/magicui/word-pull-up";
import { useRef } from "react";
import { NumberTicker } from "@/components/ui/number-ticker";
import { OverallSummary } from "@/types/dto";

export interface HomeAboutTexts {
  title: string;
  content1: string;
  content2: string;
  content3: string;
  ordersCompleted: string;
  plansSold: string;
}
const ITEM_DELAY = 0.15;
const getItemFramerProps = (index: number) => ({
  className: "text-lg md:text-xl",
  initial: { opacity: 0, scale: 0.6 },
  whileInView: { opacity: 1, scale: 1 },
  transition: {
    duration: 0.6,
    ease: "easeOut",
    delay: ITEM_DELAY * (index + 1),
    type: "keyframes",
  },
  viewport: { once: true, amount: 0.15 },
});

const HomeAbout = ({
  title,
  content2,
  content3,
  content1,
  overallSummary,
  plansSold,
  ordersCompleted,
}: HomeAboutTexts & {
  overallSummary: OverallSummary;
}) => {
  const ref = useRef(null);
  const isInView = useInView(ref, {
    once: true,
    amount: 0,
  });
  return (
    <div className="flex flex-col items-center justify-center gap-2 p-2">
      {/*<BoxReveal boxColor={"hsl(var(--primary))"} duration={0.4}>*/}
      {/*  <h1 className="text-4xl lg:text-6xl font-bold tracking-tighter p-2  ">*/}
      {/*    {title}*/}
      {/*  </h1>*/}
      {/*</BoxReveal>*/}
      <motion.div
        ref={ref}
        initial={{ opacity: 0, scale: 0.7 }}
        animate={isInView ? { opacity: 1, scale: 1 } : undefined}
        transition={{ duration: 0.6, ease: "easeOut" }}
      >
        {isInView && (
          <WordPullUp
            words={title}
            className="text-4xl lg:text-6xl font-bold tracking-tighter p-2"
          />
        )}
      </motion.div>
      <div className="prose max-w-none mx-auto my-8 px-4 md:px-12 lg:px-24 py-6 text-foreground text-xl">
        <ul className="text-xl md:text-4xl leading-relaxed list-disc pl-5 md:space-y-6 list-outside">
          {[content1, content2, content3].map((text, index) => (
            <motion.li key={text + index} {...getItemFramerProps(index)}>
              <p>{text}</p>
              {/*<BoxReveal*/}
              {/*  boxColor={"hsl(var(--primary))"}*/}
              {/*  duration={0.6}*/}
              {/*  delay={0.15 * (index + 1)}*/}
              {/*>*/}
              {/*  <p>{text}</p>*/}
              {/*</BoxReveal>*/}
            </motion.li>
          ))}
        </ul>
      </div>
      <motion.div
        {...getItemFramerProps(3)}
        className="flex flex-col w-full max-w-3xl mx-auto md:flex-row gap-8 md:gap-22 px-8 md:items-center justify-between text-3xl mb-16 sm:mb-20 mt-6 sm:mt-10"
      >
        <div className="flex items-center justify-start gap-2 h-5 sm:h-4 ">
          <NumberTicker
            value={overallSummary.ordersCount}
            delay={ITEM_DELAY * 4}
            className={`!text-success !dark:text-opacity-75 text-lg sm:text-xl md:text-3xl`}
            style={{
              width: `${20 + overallSummary.ordersCount.toString().length * 20}px`,
            }}
          />
          <p className="text-transparent bg-clip-text bg-gradient-to-r from-[hsl(var(--foreground))] dark:to-neutral-400 to-neutral-500">
            <span className="text-lg sm:text-xl md:text-3xl">
              {ordersCompleted}
            </span>
          </p>
        </div>
        <div className="flex items-center justify-start gap-2 h-5 sm:h-4 ">
          <NumberTicker
            value={overallSummary.plansCount}
            delay={ITEM_DELAY * 5}
            className={`!text-success !dark:text-opacity-75 text-lg sm:text-xl md:text-3xl`}
            style={{
              width: `${20 + overallSummary.plansCount.toString().length * 20}px`,
            }}
          />
          <p className="text-transparent bg-clip-text bg-gradient-to-r from-[hsl(var(--foreground))] dark:to-neutral-400 to-neutral-500">
            <span className="text-lg sm:text-xl md:text-3xl">{plansSold}</span>
          </p>
        </div>
      </motion.div>
    </div>
  );
};

HomeAbout.displayName = "HomeAbout";

export default HomeAbout;
