"use client";

import Image from "next/image";
import { DiffusionSchemaTexts } from "@/types/forms";
import useGetQueueArchive from "@/hoooks/useGetQueueArchive";
import { ArchiveQueue } from "@/types/dto";
import ArchiveQueueCards, {
  ArchiveQueueCardsTexts,
} from "@/components/common/archive-queue-card";
import { useLocale } from "next-intl";
import { Locale } from "@/navigation";

interface DiffusionImage {
  name: string;
  url: string;
}
interface Props {
  texts: ArchiveQueueCardsTexts;
}
export default function TestPageContent({ texts }: Props) {
  const locale = useLocale();
  return (
    <div className="w-full h-full flex items-center justify-center p-20">
      {/*<ArchiveQueueCards prefix="post" locale={locale as Locale} {...texts} />*/}
    </div>
  );
}
