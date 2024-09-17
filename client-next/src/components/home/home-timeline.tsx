"use client";
import {
  VerticalTimeline,
  VerticalTimelineElement,
} from "react-vertical-timeline-component";
import "react-vertical-timeline-component/style.min.css";
import { BookIcon, Briefcase, PersonStanding } from "lucide-react";
import { useRef } from "react";
import { useInView, useScroll } from "framer-motion";
import { cn } from "@/lib/utils";
import AnimatedGridPattern from "@/components/magicui/animated-grid-pattern";

export const ids = [
  "be-aware",
  "be-afraid",
  "make-a-plan",
  "step-by-step",
  "work-hard",
  "comfortable",
  "stay-consistent",
] as const;

export type TimelineItemTexts = Record<"date" | "title" | "text", string>;

export type HomeTimelineTexts = Record<(typeof ids)[number], TimelineItemTexts>;

const timeLine = [
  {
    id: ids[0],

    icon: <BookIcon />,
  },
  {
    id: ids[1],

    icon: <BookIcon />,
  },
  {
    id: ids[2],

    icon: <BookIcon />,
  },
  {
    id: ids[3],

    icon: <PersonStanding />,
  },
  {
    id: ids[4],

    icon: <PersonStanding />,
  },
  {
    id: ids[5],

    icon: <PersonStanding />,
  },
  {
    id: ids[6],
    icon: <Briefcase />,
  },
];
export default function HomeTimeline(props: HomeTimelineTexts) {
  const { scrollY } = useScroll();

  // Make the background scroll slower by using a smaller output range

  return (
    <div className="w-full h-full relative overflow-hidden">
      <div className="timeline-content relative z-10">
        <AnimatedGridPattern
          numSquares={60}
          maxOpacity={0.15}
          duration={3}
          repeatDelay={1}
          className={cn(
            "[mask-image:radial-gradient(800px_circle_at_center,white,transparent)]",
            "inset-x-0 inset-y-[-50%] h-[200%] skew-y-12",
          )}
        />
        <VerticalTimeline lineColor="hsl(var(--foreground))">
          {timeLine.map(({ icon, id }, i) => (
            <TimelineElement
              key={id}
              {...props[id]}
              icon={icon}
              id={id}
              index={i}
            />
          ))}
        </VerticalTimeline>
      </div>
    </div>
  );
}
function TimelineElement({
  date,
  title,
  text,
  icon,
  index,
}: (typeof timeLine)[number] & TimelineItemTexts & { index: number }) {
  const ref = useRef(null);
  const isInView = useInView(ref, { once: true });
  return (
    <div ref={ref}>
      <VerticalTimelineElement
        className={cn("vertical-timeline-element--work", "!mb-10")}
        date={date}
        iconStyle={{
          boxShadow: "none",
          backgroundColor: "hsl(var(--foreground))",
          color: "hsl(var(--background))",
        }}
        icon={icon}
        visible={isInView}
        position={index % 2 === 0 ? "left" : "right"}
        contentArrowStyle={{
          borderRight: "7px solid  hsl(var(--foreground))",
        }}
        contentStyle={{
          backgroundColor: "hsl(var(--border))",
          color: "hsl(var(--card-foreground))",
          borderRadius: "var(--radius)",
          fontSize: "1.2rem",
        }}
        textClassName={"!text-foreground !font-bold !text-xl"}
        dateClassName={"!text-foreground !font-bold !text-xl"}
      >
        <h3 className="!text-foreground">{title}</h3>
        <p className="!text-foreground">{text}</p>
      </VerticalTimelineElement>
    </div>
  );
}
