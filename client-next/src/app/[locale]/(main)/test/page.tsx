import TestPageContent from "@/app/[locale]/(main)/test/page-content";
import { Locale } from "@/navigation";
import { getDateRangePickerTexts } from "@/texts/components/ui";

interface Props {
  params: {
    locale: Locale;
  };
}
export default async function TestPage({ params }: Props) {
  const texts = await getDateRangePickerTexts();
  return <TestPageContent />;
}
