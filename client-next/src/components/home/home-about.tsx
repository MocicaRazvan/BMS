"use client";

import BoxReveal from "@/components/magicui/box-reveal";

export interface HomeAboutTexts {
  title: string;
  content1: string;
  content2: string;
  content3: string;
}

export default function HomeAbout({
  title,
  content2,
  content3,
  content1,
}: HomeAboutTexts) {
  return (
    <div className={"flex flex-col items-center justify-center gap-2 p-2"}>
      <BoxReveal boxColor={"hsl(var(--primary))"} duration={0.4}>
        <h1 className="text-4xl lg:text-6xl font-bold tracking-tighter p-2  ">
          {title}
        </h1>
      </BoxReveal>
      <div className="prose max-w-none mx-auto my-8 px-4 md:px-12 lg:px-24 py-6 text-foreground text-xl">
        <ul className="text-xl md:text-4xl leading-relaxed list-disc pl-5 md:space-y-6 list-outside">
          {[content1, content2, content3].map((text, index) => (
            <li key={text + index} className="text-lg md:text-xl">
              <BoxReveal
                boxColor={"hsl(var(--primary))"}
                duration={0.6}
                delay={0.15 * (index + 1)}
              >
                <p>{text}</p>
              </BoxReveal>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
