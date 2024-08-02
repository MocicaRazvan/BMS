import { unstable_setRequestLocale } from "next-intl/server";
import { Locale } from "@/navigation";
import PostApprovedPageContent from "@/app/[locale]/(main)/(user)/posts/approved/page-content";
import { getApprovedPostsPageTexts } from "@/texts/pages";
import { SortingOption } from "@/components/list/grid-list";
import { Option } from "@/components/ui/multiple-selector";
import { getSortingOptions } from "@/lib/constants";
import { sortingPostsSortingOptionsKeys } from "@/texts/components/list";

interface Props {
  params: { locale: Locale };
}

export default async function PostApprovedPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const texts = await getApprovedPostsPageTexts();
  const postOptions = getSortingOptions(
    sortingPostsSortingOptionsKeys,
    texts.sortingPostsSortingOptions,
  );

  return <PostApprovedPageContent options={postOptions} {...texts} />;
}
