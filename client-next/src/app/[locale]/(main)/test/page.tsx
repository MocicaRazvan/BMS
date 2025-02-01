import TestPageContent from "@/app/[locale]/(main)/test/page-content";
import { Locale } from "@/navigation";
import { getDateRangePickerTexts } from "@/texts/components/ui";
import {
  getPlanItemRendererTexts,
  getPostItemRendererTexts,
  getRecommendationListTexts,
} from "@/texts/components/recommandation";

interface Props {
  params: {
    locale: Locale;
  };
}
export default async function TestPage({ params }: Props) {
  const texts = await getRecommendationListTexts("plan");
  const texts2 = await getPostItemRendererTexts();
  return <TestPageContent texts={texts} texts2={texts2} />;
}
