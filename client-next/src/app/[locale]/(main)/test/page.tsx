import TestPageContent from "@/app/[locale]/(main)/test/page-content";
import { Locale } from "@/navigation";

interface Props {
  params: {
    locale: Locale;
  };
}
export default async function TestPage({ params }: Props) {
  return <TestPageContent />;
}
