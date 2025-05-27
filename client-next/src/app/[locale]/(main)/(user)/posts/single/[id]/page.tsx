import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { getSinglePostPageTexts } from "@/texts/pages";
import SinglePostPageContent from "@/app/[locale]/(main)/(user)/posts/single/[id]/page-content";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import ScrollProgress from "@/components/common/scroll-progress";

interface Props {
  params: { locale: Locale; id: string };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "user.SinglePost",
      "/posts/single/" + id,
      locale,
    )),
  };
}
export default async function SinglePostPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [singlePostPageTexts] = await Promise.all([getSinglePostPageTexts()]);
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <ScrollProgress />
      <SinglePostPageContent
        {...singlePostPageTexts}
        showRecommendations={true}
        trackViews={true}
      />
    </Suspense>
  );
}
