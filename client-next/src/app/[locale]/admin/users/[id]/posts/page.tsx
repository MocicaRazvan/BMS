import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { PostTableTexts } from "@/components/table/posts-table";
import { getSortingOptions, SortingOptionsTexts } from "@/types/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getUserPostsAdminPageTexts } from "@/texts/pages";
import { notFound } from "next/navigation";
import UserPostsAdminPageContent from "@/app/[locale]/admin/users/[id]/posts/page-content";
import { sortingPostsSortingOptionsKeys } from "@/texts/components/list";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { FindInSiteTexts } from "@/components/nav/find-in-site";

export interface UserPostsAdminPageTexts {
  postTableTexts: PostTableTexts;
  sortingPostsSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: SidebarMenuTexts;
  findInSiteTexts: FindInSiteTexts;
}

interface Props {
  params: { locale: Locale; id: string };
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.UserPosts",
    "/admin/users/" + id + "/posts",
    locale,
  );
}

export default async function UserPostsAdminPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);

  const [texts] = await Promise.all([getUserPostsAdminPageTexts()]);

  const postOptions = getSortingOptions(
    sortingPostsSortingOptionsKeys,
    texts.sortingPostsSortingOptions,
  );
  if (!id) {
    notFound();
  }
  return (
    <UserPostsAdminPageContent
      id={id}
      {...texts}
      sortingOptions={postOptions}
      path={`/posts/trainer/tags/${id}`}
    />
  );
}
