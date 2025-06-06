import { Locale } from "@/navigation";
import React, { Suspense } from "react";

import TestPageContent from "./page-content";
import { unstable_setRequestLocale } from "next-intl/server";
import { getApprovedPostsPageTexts } from "@/texts/pages";
import { getSortingOptions } from "@/lib/constants";
import { sortingPostsSortingOptionsKeys } from "@/texts/components/list";

interface Props {
  params: {
    locale: Locale;
  };
}

export default async function TestPage({ params }: Props) {
  unstable_setRequestLocale(params.locale);
  const texts = await getApprovedPostsPageTexts();
  const postOptions = getSortingOptions(
    sortingPostsSortingOptionsKeys,
    texts.sortingPostsSortingOptions,
  );
  return (
    <Suspense fallback={<div className="bg-red-600 min-h-52">Loading</div>}>
      <TestPageContent options={postOptions} />
    </Suspense>
  );
}
