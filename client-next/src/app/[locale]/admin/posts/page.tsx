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
import { getIntlMetadata } from "@/texts/metadata";

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
    },
    authUser,
  ] = await Promise.all([
    getAdminPostsPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);

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
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading title={title} header={header} />
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-10 h-full">
            <PostsTable
              path={`/posts/tags`}
              forWhom="admin"
              {...postTableTexts}
              sortingOptions={postOptions}
              sizeOptions={[10, 20, 30, 40]}
              authUser={authUser}
              mainDashboard={true}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
