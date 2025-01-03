import TestPageContent from "@/app/[locale]/(main)/test/page-content";
import { getDiffusionSchemaTexts } from "@/types/forms";
import { getArchiveQueueCardsTexts } from "@/texts/components/common";

export default async function TestPage() {
  const texts = await getArchiveQueueCardsTexts("post");
  return <TestPageContent texts={texts} />;
}
