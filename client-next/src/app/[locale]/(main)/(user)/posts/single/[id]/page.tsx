import { getUserWithMinRole } from "@/lib/user";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { getSinglePostPageTexts } from "@/texts/pages";
import SinglePostPageContent from "@/app/[locale]/(main)/(user)/posts/single/[id]/page-content";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
interface Props {
  params: { locale: Locale };
}
export default async function SinglePostPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [user, singlePostPageTexts] = await Promise.all([
    getUserWithMinRole("ROLE_USER"),
    getSinglePostPageTexts(),
  ]);
  return (
    <Suspense fallback={<LoadingSpinner />}>
      <SinglePostPageContent authUser={user} {...singlePostPageTexts} />
    </Suspense>
  );
}
