import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import UserPostsPageContent, {
  UserPostsPageContentTexts,
} from "@/app/[locale]/trainer/user/[id]/posts/page-content";
import { getUserPostsPageTexts } from "@/texts/pages";
import { getSortingOptions } from "@/lib/constants";
import { sortingPostsSortingOptionsKeys } from "@/texts/components/list";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { getTheSameUserOrAdmin } from "@/lib/user";
import Heading from "@/components/common/heading";
import { Metadata } from "next";
import { getIntlMetadata, getMetadataValues } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { FindInSiteTexts } from "@/components/nav/find-in-site";
import { Separator } from "@/components/ui/separator";
import TopViewedPosts, {
  TopViewedPostsTexts,
} from "@/components/charts/top-viewed-posts";

interface Props {
  params: { locale: Locale; id: string };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata(
      "trainer.TrainerPosts",
      "/trainer/user/" + id + "/posts",
      locale,
    )),
  };
}

export interface UserPostsPageTexts {
  userPostsPageContentTexts: UserPostsPageContentTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
  topViewedPostsTexts: TopViewedPostsTexts;
}

export default async function UsersPostsPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [
    {
      userPostsPageContentTexts,
      themeSwitchTexts,
      menuTexts,
      findInSiteTexts,
      topViewedPostsTexts,
    },
    authUser,
  ] = await Promise.all([getUserPostsPageTexts(), getTheSameUserOrAdmin(id)]);
  const postOptions = getSortingOptions(
    sortingPostsSortingOptionsKeys,
    userPostsPageContentTexts.sortingPostsSortingOptions,
  );
  const metadataValues = await getMetadataValues(authUser, locale);

  return (
    <SidebarContentLayout
      navbarProps={{
        title: userPostsPageContentTexts.title,
        themeSwitchTexts,
        authUser,
        menuTexts,
        mappingKey: "trainer",
        findInSiteTexts,
        metadataValues,
      }}
    >
      <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 mx-auto ">
        <Heading {...userPostsPageContentTexts} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="space-y-10 pb-5">
            <UserPostsPageContent
              id={id}
              sortingOptions={postOptions}
              {...userPostsPageContentTexts}
              authUser={authUser}
            />
            <Separator className="mt-2" />
            <div className=" my-5 h-full w-full">
              <TopViewedPosts
                path={`/posts/viewStats/${authUser.id}`}
                texts={topViewedPostsTexts}
                authUser={authUser}
              />
            </div>
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
