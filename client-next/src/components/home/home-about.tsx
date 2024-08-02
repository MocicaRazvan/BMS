"use client";

import BoxReveal from "@/components/magicui/box-reveal";

export interface HomeAboutTexts {
  title: string;
}

const infoTexts = [
  'Contrary adto popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of "de Finibus Bonorum et Malorum" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, "Lorem ipsum dolor sit amet..", comes from a line in section 1.10.32.',
  'Contrary dasto popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of "de Finibus Bonorum et Malorum" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, "Lorem ipsum dolor sit amet..", comes from a line in section 1.10.32.',
  'Contrary to popular belief, Lorem Ipsum is not simply random text. It has roots in a piece of classical Latin literature from 45 BC, making it over 2000 years old. Richard McClintock, a Latin professor at Hampden-Sydney College in Virginia, looked up one of the more obscure Latin words, consectetur, from a Lorem Ipsum passage, and going through the cites of the word in classical literature, discovered the undoubtable source. Lorem Ipsum comes from sections 1.10.32 and 1.10.33 of "de Finibus Bonorum et Malorum" (The Extremes of Good and Evil) by Cicero, written in 45 BC. This book is a treatise on the theory of ethics, very popular during the Renaissance. The first line of Lorem Ipsum, "Lorem ipsum dolor sit amet..", comes from a line in section 1.10.32.',
];
export default function HomeAbout({ title }: HomeAboutTexts) {
  return (
    <div className={"flex flex-col items-center justify-center gap-2 p-2"}>
      <BoxReveal boxColor={"hsl(var(--primary))"} duration={0.4}>
        <h1 className="text-4xl lg:text-6xl font-bold tracking-tighter p-2  ">
          {title}
        </h1>
      </BoxReveal>
      <div className="prose max-w-none mx-auto my-8 px-4 md:px-12 lg:px-24 py-6 text-foreground text-xl">
        <ul className="text-xl md:text-4xl leading-relaxed list-disc pl-5 md:space-y-6 list-outside">
          {infoTexts.map((text, index) => (
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
