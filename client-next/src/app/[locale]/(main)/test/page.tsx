import TestPageContent from "@/app/[locale]/(main)/test/page-content";
import { Locale } from "@/navigation";
import { getDateRangePickerTexts } from "@/texts/components/ui";
import {
  getPlanItemRendererTexts,
  getPostItemRendererTexts,
  getRecommendationListTexts,
} from "@/texts/components/recommandation";
import {
  getPlanCharacteristicWrapperTexts,
  getPredictionChartTexts,
  getTopChartWrapperTexts,
  getTopTrainersTexts,
} from "@/texts/components/charts";

interface Props {
  params: {
    locale: Locale;
  };
}
export default async function TestPage({ params }: Props) {
  const texts = await getPredictionChartTexts("orders");
  return <TestPageContent texts={texts} />;
}
