import { Locale } from "@/navigation";
import React, { Suspense } from "react";

import TestPageContent from "./page-content";
import { unstable_setRequestLocale } from "next-intl/server";
import { getEditorTexts } from "@/texts/components/editor";

interface Props {
  params: {
    locale: Locale;
  };
}

export default async function TestPage({ params }: Props) {
  unstable_setRequestLocale(params.locale);
  const texts = await getEditorTexts();
  return (
    <Suspense fallback={<div className="bg-red-600 min-h-52">Loading</div>}>
      <TestPageContent editorTexts={texts} />
    </Suspense>
  );
}
