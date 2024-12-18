"use client";

import { motion } from "framer-motion";
import { Beef, Newspaper, User2 } from "lucide-react";
import { Link } from "@/navigation";
import { MagicCard } from "@/components/magicui/magic-card";
import AnimatedShinyText from "@/components/magicui/animated-shiny-text";

export type CardTexts = Record<"title" | "description", string>;

const ids = ["register", "posts", "plans"] as const;

export type HomeCardsText = Record<(typeof ids)[number], CardTexts>;
const cards: {
  id: (typeof ids)[number];
  href: string;
  icon: React.ReactNode;
}[] = [
  {
    id: "register",
    href: "/auth/signin",
    icon: <User2 size={64} className="w-full mx-auto" />,
  },
  {
    id: "posts",
    href: "/posts/approved",
    icon: <Newspaper size={64} className="w-full mx-auto" />,
  },
  {
    id: "plans",
    href: "/plans/approved",
    icon: <Beef size={64} className="w-full mx-auto" />,
  },
];

export default function HomeCards(props: HomeCardsText) {
  console.log("PROPS", props);
  return (
    <div className="flex flex-wrap items-center justify-center gap-10 mt-15 overflow-hidden py-16">
      {cards.map((item, i) => (
        <HomeCard key={i} index={i} {...item} {...props[item.id]} />
      ))}
    </div>
  );
}

interface Props {
  index: number;
  title: string;
  description: string;
  href: string;
  icon: React.ReactNode;
}

export function HomeCard({ index, title, description, icon, href }: Props) {
  const slideInAnimationVariants = {
    initial: { opacity: 0, x: "-100%" },
    animate: (index: number) => ({
      opacity: 1,
      x: 0,
      transition: {
        delay: index * 0.05,
        type: "spring",
        stiffness: 80,
      },
    }),
  };
  return (
    <motion.div
      key={index}
      variants={slideInAnimationVariants}
      className="h-[400px] max-w-[300px] w-full  "
      initial="initial"
      whileInView="animate"
      custom={index}
      viewport={{
        once: true,
        // amount: 0.2,
      }}
    >
      <MagicCard
        className=" h-full w-full  !bg-background
      hover:shadow-lg transition-all duration-300 shadow-foreground hover:shadow-foreground/40 hover:scale-[1.025]"
        gradientOpacity={0.25}
        gradientColor={"hsl(var(--primary))"}
        gradientSize={180}
      >
        <Link href={href} className="h-full w-full block py-2  px-3">
          <div className="w-full h-full rounded-lg py-4 px-2 flex flex-col justify-between items-stretch gap-10">
            <AnimatedShinyText className="text-3xl tracking-tighter font-bold text-center">
              {title}
            </AnimatedShinyText>
            <div className="w-full"> {icon}</div>
            <p className=" text-center font-semibold">{description}</p>
          </div>
        </Link>
      </MagicCard>
    </motion.div>
  );
}
