"use client";

import Image from "next/image";
import picture1 from "@/../public/images/Picture1.jpg";
import picture2 from "@/../public/images/Picture2.jpg";
import picture3 from "@/../public/images/Picture3.jpg";
import picture4 from "@/../public/images/Picture4.jpg";
import picture5 from "@/../public/images/Picture5.jpg";
import picture6 from "@/../public/images/Picture6.jpg";
import picture7 from "@/../public/images/Picture7.jpg";
import {
  useScroll,
  useTransform,
  motion,
  useAnimation,
  useMotionValueEvent,
  AnimatePresence,
} from "framer-motion";
import { useEffect, useMemo, useRef, useState } from "react";
import DotPattern from "@/components/magicui/dot-pattern";
import WordPullUp from "@/components/magicui/word-pull-up";
import Logo from "@/components/logo/logo";

export interface HomeHeaderTexts {
  title: string;
}

export default function HomeHeader({ title }: HomeHeaderTexts) {
  const container = useRef(null);

  const { scrollYProgress } = useScroll({
    target: container,
    offset: ["start start", "end end"],
  });
  const [hookedYPosition, setHookedYPosition] = useState(0);
  useMotionValueEvent(scrollYProgress, "change", (latest) => {
    setHookedYPosition(latest);
  });

  const scale4 = useTransform(scrollYProgress, [0, 1], [1, 4]);
  const scale5 = useTransform(scrollYProgress, [0, 1], [1, 5]);
  const scale6 = useTransform(scrollYProgress, [0, 1], [1, 6]);
  const scale8 = useTransform(scrollYProgress, [0, 1], [1, 8]);
  const scale9 = useTransform(scrollYProgress, [0, 1], [1, 9]);
  const fontSize = useTransform(scrollYProgress, [0, 1], ["0.8rem", "2.3rem"]);

  const pictures = useMemo(
    () => [
      {
        src: picture1,
        scale: scale4,
        style: { width: "25vw", height: "25vh" },
      },
      {
        src: picture2,
        scale: scale5,
        style: {
          width: "35vw",
          height: "30vh",
          top: "-30vh",
          left: "5vw",
        },
      },
      {
        src: picture3,
        scale: scale6,
        style: {
          width: "20vw",
          height: "45vh",
          top: "-10vh",
          left: "-25vw",
        },
      },
      {
        src: picture4,
        scale: scale5,
        style: {
          width: "25vw",
          height: "25vh",
          left: "27.5vw",
        },
      },
      {
        src: picture5,
        scale: scale6,
        style: {
          width: "20vw",
          height: "25vh",
          top: "27.5vh",
          left: "5vw",
        },
      },
      {
        src: picture6,
        scale: scale8,
        style: {
          width: "30vw",
          height: "25vh",
          top: "27.5vh",
          left: "-22.5vw",
        },
      },
      {
        src: picture7,
        scale: scale9,
        style: {
          width: "15vw",
          height: "15vh",
          top: "22.5vh",
          left: "25vw",
        },
      },
    ],
    [scale4, scale5, scale6, scale8, scale9],
  );

  return (
    <div ref={container} className="h-[300vh] relative">
      <div className="sticky top-0 h-[100vh] z-10 overflow-hidden">
        {pictures.map(({ src, scale, style }, index) => (
          <motion.div
            style={{ scale }}
            key={src + index.toString()}
            className="w-full h-full absolute top-0 flex items-center justify-center"
          >
            <div className="w-[25vw] h-[25vh] relative" style={{ ...style }}>
              <div className="relative w-full h-full">
                <Image
                  src={src}
                  fill
                  alt="image"
                  placeholder="blur"
                  className="object-cover"
                />{" "}
                <div className="absolute inset-0 bg-black opacity-25 z-1" />
              </div>
              {index === 0 && (
                <AnimatePresence>
                  {hookedYPosition > 0.55 && (
                    <motion.div
                      className="absolute left-1/2 top-[10%] lg:top-[30%] transform -translate-y-1/2 px-4 py-2 w-full text-slate-100"
                      initial={{ y: 20, opacity: 0, x: "-50%" }}
                      animate={{ y: 0, opacity: 1 }}
                      exit={{ y: 20, opacity: 0 }}
                      transition={{ duration: 0.5 }}
                      // style={{ fontSize: `${fontSize} !important` }}
                    >
                      <div className="flex items-center justify-center ">
                        <Logo width={60} height={60} applyShadow />
                      </div>
                      <WordPullUp
                        words={title}
                        className="text-sm lg:text-xl z-2 tracking-tighter font-bold [text-shadow:_1px_5px_1px_rgb(0_0_0_/_50%)]"
                      />
                    </motion.div>
                  )}
                </AnimatePresence>
              )}
            </div>
          </motion.div>
        ))}
      </div>
      <DotPattern className="pointer-events-none absolute inset-0 h-full w-full fill-neutral-500/80 dark:fill-neutral-400/70 [mask-image:radial-gradient(circle_at_center,white,transparent)]" />
    </div>
  );
}
