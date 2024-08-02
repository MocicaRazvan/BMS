import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import UserPostsPageContent from "@/app/[locale]/(main)/trainer/user/[id]/posts/page-content";
import { getUserPostsPageContentTexts } from "@/texts/pages";
import { getSortingOptions } from "@/lib/constants";
import { sortingPostsSortingOptionsKeys } from "@/texts/components/list";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { getTheSameUserOrAdmin } from "@/lib/user";
import Heading from "@/components/common/heading";

interface Props {
  params: { locale: Locale; id: string };
}

export default async function UsersPostsPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [userPostsPageContentTexts, authUser] = await Promise.all([
    getUserPostsPageContentTexts(),
    getTheSameUserOrAdmin(id),
  ]);
  const postOptions = getSortingOptions(
    sortingPostsSortingOptionsKeys,
    userPostsPageContentTexts.sortingPostsSortingOptions,
  );

  console.log("AUTH", authUser);
  return (
    <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 max-w-[1250px] mx-auto ">
      <Heading {...userPostsPageContentTexts} />

      <Suspense fallback={<LoadingSpinner />}>
        <div className="">
          <UserPostsPageContent
            id={id}
            sortingOptions={postOptions}
            {...userPostsPageContentTexts}
            authUser={authUser}
          />
        </div>
      </Suspense>
    </div>
  );
}
