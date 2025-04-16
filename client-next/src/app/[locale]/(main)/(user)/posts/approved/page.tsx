import { unstable_setRequestLocale } from "next-intl/server";
import { Locale } from "@/navigation";
import PostApprovedPageContent from "@/app/[locale]/(main)/(user)/posts/approved/page-content";
import { getApprovedPostsPageTexts } from "@/texts/pages";
import { getSortingOptions } from "@/lib/constants";
import { sortingPostsSortingOptionsKeys } from "@/texts/components/list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.ApprovedPosts", "/posts/approved", locale)),
  };
}

export default async function PostApprovedPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const texts = await getApprovedPostsPageTexts();
  const postOptions = getSortingOptions(
    sortingPostsSortingOptionsKeys,
    texts.sortingPostsSortingOptions,
  );

  return (
    <PostApprovedPageContent options={postOptions} locale={locale} {...texts} />
  );
}
