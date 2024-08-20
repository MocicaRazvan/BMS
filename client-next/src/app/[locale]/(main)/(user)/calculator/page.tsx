import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { Locale } from "@/navigation";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import CalculatorPageContent from "@/app/[locale]/(main)/(user)/calculator/page-content";
import {
  ActivitiesTexts,
  CalculatorSchemaTexts,
  GenderText,
} from "@/types/forms";
import { getCalculatorPageTexts, intakeTitles } from "@/texts/pages";
interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.Calculator", "/calculator", locale)),
  };
}

export type IntakeTitle = Record<(typeof intakeTitles)[number], string>;
export type ItemTexts = {
  label: string;
  placeholder: string;
  description?: string;
};

export interface CalculatorPageTexts {
  activitiesTexts: ActivitiesTexts;
  genderText: GenderText;
  calculatorSchemaTexts: CalculatorSchemaTexts;
  intakeTitles: IntakeTitle;
  week: string;
  imperial: string;
  metric: string;
  itemsTexts: Record<
    "activity" | "age" | "gender" | "height" | "weight" | "intake",
    ItemTexts
  >;
  title: string;
  header: string;
  button: string;
  message1: string;
  message2: string;
}

export default async function CalculatorPage() {
  const [texts] = await Promise.all([getCalculatorPageTexts()]);
  return (
    <div className="space-y-10  w-full transition-all mt-5 p-10 max-w-[1350px] mx-auto ">
      <Suspense fallback={<LoadingSpinner />}>
        <div>
          <CalculatorPageContent {...texts} />
        </div>
      </Suspense>
    </div>
  );
}
