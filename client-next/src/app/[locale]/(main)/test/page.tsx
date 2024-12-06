import TestPageContent from "@/app/[locale]/(main)/test/page-content";
import { getDiffusionSchemaTexts } from "@/types/forms";

export default async function TestPage() {
  const texts = await getDiffusionSchemaTexts();
  return <TestPageContent texts={texts} />;
}
