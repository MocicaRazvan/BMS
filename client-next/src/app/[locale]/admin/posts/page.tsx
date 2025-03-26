import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import PostsTable, { PostTableTexts } from "@/components/table/posts-table";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { getAdminPostsPageTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { sortingPostsSortingOptionsKeys } from "@/texts/components/list";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata, getMetadataValues } from "@/texts/metadata";
import ArchiveQueueCards, {
  ArchiveQueueCardsTexts,
} from "@/components/common/archive-queue-card";
import { Separator } from "@/components/ui/separator";
import { FindInSiteTexts } from "@/components/nav/find-in-site";
import TopViewedPosts, {
  TopViewedPostsTexts,
} from "@/components/charts/top-viewed-posts";

interface Props {
  params: { locale: Locale };
}

export interface AdminPostsPageTexts {
  postTableTexts: PostTableTexts;
  sortingPostsSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
  archivePostsTexts: ArchiveQueueCardsTexts;
  archiveCommentsTexts: ArchiveQueueCardsTexts;
  findInSiteTexts: FindInSiteTexts;
  topViewedPostsTexts: TopViewedPostsTexts;
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata("admin.Posts", "/admin/posts", locale);
}

export default async function AdminPostsPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const [
    {
      title,
      themeSwitchTexts,
      postTableTexts,
      sortingPostsSortingOptions,
      header,
      menuTexts,
      archivePostsTexts,
      archiveCommentsTexts,
      findInSiteTexts,
      topViewedPostsTexts,
    },
    authUser,
  ] = await Promise.all([
    getAdminPostsPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);
  const metadataValues = await getMetadataValues(authUser, locale);

  const postOptions = getSortingOptions(
    sortingPostsSortingOptionsKeys,
    sortingPostsSortingOptions,
  );
  return (
    <SidebarContentLayout
      navbarProps={{
        title,
        themeSwitchTexts,
        authUser,
        menuTexts,
        mappingKey: "admin",
        findInSiteTexts,
        metadataValues,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading title={title} header={header} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full space-y-10">
            <PostsTable
              path={`/posts/tags`}
              forWhom="admin"
              {...postTableTexts}
              sortingOptions={postOptions}
              sizeOptions={[10, 20, 30, 40]}
              authUser={authUser}
              mainDashboard={true}
              extraQueryParams={{
                admin: "true",
              }}
            />
            <Separator />
            <TopViewedPosts
              path="/posts/admin/viewStats"
              texts={topViewedPostsTexts}
              authUser={authUser}
            />
            <Separator />
            <div className="space-y-5">
              <ArchiveQueueCards
                prefix={"post"}
                locale={locale}
                showHeader={true}
                {...archivePostsTexts}
                authUser={authUser}
              />
              <ArchiveQueueCards
                prefix={"comment"}
                locale={locale}
                showHeader={false}
                {...archiveCommentsTexts}
                authUser={authUser}
              />
            </div>
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
