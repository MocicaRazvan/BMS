"use client";

import { motion, useInView } from "framer-motion";
import WordPullUp from "@/components/magicui/word-pull-up";
import { memo, useRef } from "react";
import { isDeepEqual } from "@/lib/utils";

export interface HomeAboutTexts {
  title: string;
  content1: string;
  content2: string;
  content3: string;
}

const HomeAbout = memo(
  ({ title, content2, content3, content1 }: HomeAboutTexts) => {
    const ref = useRef(null);
    const isInView = useInView(ref, {
      once: true,
      amount: 0,
    });
    return (
      <div className={"flex flex-col items-center justify-center gap-2 p-2"}>
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
              <motion.li
                key={text + index}
                className="text-lg md:text-xl"
                initial={{ opacity: 0, scale: 0.6 }}
                whileInView={{ opacity: 1, scale: 1 }}
                transition={{
                  duration: 0.6,
                  ease: "easeOut",
                  delay: 0.15 * (index + 1),
                  type: "keyframes",
                }}
                viewport={{ once: true, amount: 0.15 }}
              >
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
      </div>
    );
  },
  (p, n) => isDeepEqual(p, n),
);

HomeAbout.displayName = "HomeAbout";

export default HomeAbout;
