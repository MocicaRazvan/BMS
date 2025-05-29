import { unstable_setRequestLocale } from "next-intl/server";
import { getApprovedPostsPageTexts } from "@/texts/pages";
import { getSortingOptions } from "@/lib/constants";
import { sortingPostsSortingOptionsKeys } from "@/texts/components/list";
import { LocaleProps } from "@/navigation";
import DemoPostApprovedPageContent from "@/app/[locale]/(main)/(user)/posts/demo/page-content";

export default async function DemoPagePosts({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);
  const texts = await getApprovedPostsPageTexts();
  const postOptions = getSortingOptions(
    sortingPostsSortingOptionsKeys,
    texts.sortingPostsSortingOptions,
  );

  return (
    <DemoPostApprovedPageContent
      options={postOptions}
      locale={locale}
      {...texts}
    />
  );
}
