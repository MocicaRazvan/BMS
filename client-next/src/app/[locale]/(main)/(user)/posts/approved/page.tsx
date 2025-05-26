import { unstable_setRequestLocale } from "next-intl/server";
import PostApprovedPageContent from "@/app/[locale]/(main)/(user)/posts/approved/page-content";
import { getApprovedPostsPageTexts } from "@/texts/pages";
import { getSortingOptions } from "@/lib/constants";
import { sortingPostsSortingOptionsKeys } from "@/texts/components/list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { LocaleProps } from "@/navigation";

export async function generateMetadata({
  params: { locale },
}: LocaleProps): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.ApprovedPosts", "/posts/approved", locale)),
  };
}

export default async function PostApprovedPage({
  params: { locale },
}: LocaleProps) {
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
