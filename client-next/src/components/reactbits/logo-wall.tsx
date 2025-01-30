"use client";
import { DetailedHTMLProps, HTMLAttributes, ReactNode, useState } from "react";

export default function LogoWall({
  items = [],
  direction = "horizontal",
  pauseOnHover = false,
  size = "clamp(8rem, 1rem + 30vmin, 25rem)",
  duration = "60s",
}: {
  items: ReactNode[];
  direction?: "horizontal" | "vertical";
  pauseOnHover?: boolean;
  size?: string;
  duration?: string;
}) {
  const [isPaused, setIsPaused] = useState(false);

  const wrapperClass = [
    "flex",
    "flex-col",
    "gap-[calc(var(--size)/14)]",
    "mx-auto",
    "max-w-full",
    "p-[20px_10px]",
    direction === "vertical" && "flex-row justify-center h-full",
  ]
    .filter(Boolean)
    .join(" ");

  const marqueeClass = [
    "relative",
    "flex",
    "overflow-hidden",
    "select-none",
    "gap-[calc(var(--size)/14)]",
    "justify-start",
    "w-full",
    "mask-horizontal",
    direction === "vertical" && "flex-col h-full mask-vertical",
    isPaused && "paused",
  ]
    .filter(Boolean)
    .join(" ");

  return (
    <article
      className={wrapperClass}
      style={
        {
          ["--size"]: size,
          ["--duration"]: duration,
          ["--color-text"]: "hsl(var(--background))",
          ["--color-bg"]: "hsl(var(--foreground))",
          ["--color-bg-accent"]: "hsl(var(--accent))",
          color: "hsl(var(--foreground))",
          backgroundColor: "hsl(var(--background))",
        } as DetailedHTMLProps<HTMLAttributes<HTMLElement>, HTMLElement>
      }
    >
      <div
        className={marqueeClass}
        onMouseEnter={() => pauseOnHover && setIsPaused(true)}
        onMouseLeave={() => pauseOnHover && setIsPaused(false)}
      >
        <div
          className={[
            "flex-shrink-0",
            "flex",
            "items-center",
            "justify-around",
            "gap-[calc(var(--size)/14)]",
            "min-w-full",
            "animate-scrollX",
            direction === "vertical" && "flex-col min-h-full animate-scrollY",
          ]
            .filter(Boolean)
            .join(" ")}
        >
          {items.map((item, idx) => (
            <div
              key={idx + "-logo-first-row"}
              className={[
                // "bg-[var(--color-bg-accent)]",
                "rounded-md",
                "object-contain",
                "aspect-video",
                `w-[var(--size)] p-[calc(var(--size)/10)]`,
                direction === "vertical" &&
                  "aspect-square w-[calc(var(--size)/1.5)] p-[calc(var(--size)/6)]",
              ]
                .filter(Boolean)
                .join(" ")}
            >
              {item}
            </div>
          ))}
        </div>
        <div
          aria-hidden="true"
          className={[
            "flex-shrink-0",
            "flex",
            "items-center",
            "justify-around",
            "gap-[calc(var(--size)/14)]",
            "min-w-full",
            "animate-scrollX",
            direction === "vertical" && "flex-col min-h-full animate-scrollY",
          ]
            .filter(Boolean)
            .join(" ")}
        >
          {items.map((item, idx) => (
            <div
              key={idx + "-logo-second-row"}
              className={[
                // "bg-[var(--color-bg-accent)]",
                "rounded-md",
                "object-contain",
                "aspect-video",
                `w-[var(--size)] p-[calc(var(--size)/10)]`,
                direction === "vertical" &&
                  "aspect-square w-[calc(var(--size)/1.5)] p-[calc(var(--size)/6)]",
              ]
                .filter(Boolean)
                .join(" ")}
            >
              {item}
            </div>
          ))}
        </div>
      </div>

      <div
        className={marqueeClass + " marquee--reverse"}
        onMouseEnter={() => pauseOnHover && setIsPaused(true)}
        onMouseLeave={() => pauseOnHover && setIsPaused(false)}
      >
        <div
          className={[
            "flex-shrink-0",
            "flex",
            "items-center",
            "justify-around",
            "gap-[calc(var(--size)/14)]",
            "min-w-full",
            "animate-scrollX reverse-x",
            direction === "vertical" &&
              "flex-col min-h-full animate-scrollY reverse-x",
          ]
            .filter(Boolean)
            .join(" ")}
        >
          {items.map((item, idx) => (
            <div
              key={`rev-${idx}-logo-first-row`}
              className={[
                // "bg-[var(--color-bg-accent)]",
                "rounded-md",
                "object-contain",
                "aspect-video",
                `w-[var(--size)] p-[calc(var(--size)/10)]`,
                direction === "vertical" &&
                  "aspect-square w-[calc(var(--size)/1.5)] p-[calc(var(--size)/6)]",
              ]
                .filter(Boolean)
                .join(" ")}
            >
              {item}
            </div>
          ))}
        </div>
        <div
          aria-hidden="true"
          className={[
            "flex-shrink-0",
            "flex",
            "items-center",
            "justify-around",
            "gap-[calc(var(--size)/14)]",
            "min-w-full",
            "animate-scrollX reverse-x",
            direction === "vertical" &&
              "flex-col min-h-full animate-scrollY reverse-x",
          ]
            .filter(Boolean)
            .join(" ")}
        >
          {items.map((item, idx) => (
            <div
              key={`dup2-${idx}-logo-second-row`}
              className={[
                // "bg-[var(--color-bg-accent)]",
                "rounded-md",
                "object-contain",
                "aspect-video",
                `w-[var(--size)] p-[calc(var(--size)/10)]`,
                direction === "vertical" &&
                  "aspect-square w-[calc(var(--size)/1.5)] p-[calc(var(--size)/6)]",
              ]
                .filter(Boolean)
                .join(" ")}
            >
              {item}
            </div>
          ))}
        </div>
      </div>
    </article>
  );
}
