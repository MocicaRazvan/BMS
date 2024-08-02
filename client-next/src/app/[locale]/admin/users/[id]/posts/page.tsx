import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { PostTableTexts } from "@/components/table/posts-table";
import { getSortingOptions, SortingOptionsTexts } from "@/lib/constants";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { getUserPostsAdminPageTexts } from "@/texts/pages";
import { getUserWithMinRole } from "@/lib/user";
import { notFound } from "next/navigation";
import UserPageContent from "@/app/[locale]/(main)/(user)/users/single/[id]/page-content";
import UserPostsAdminPageContent from "@/app/[locale]/admin/users/[id]/posts/page-content";
import { sortingPostsSortingOptionsKeys } from "@/texts/components/list";
import { AdminMenuTexts } from "@/components/admin/menu-list";

export interface UserPostsAdminPageTexts {
  postTableTexts: PostTableTexts;
  sortingPostsSortingOptions: SortingOptionsTexts;
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  header: string;
  menuTexts: AdminMenuTexts;
}

interface Props {
  params: { locale: Locale; id: string };
}

export default async function UserPostsAdminPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);

  const [texts, authUser] = await Promise.all([
    getUserPostsAdminPageTexts(),
    getUserWithMinRole("ROLE_ADMIN"),
  ]);
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
      authUser={authUser}
      {...texts}
      sortingOptions={postOptions}
      path={`/posts/trainer/tags/${id}`}
    />
  );
}
