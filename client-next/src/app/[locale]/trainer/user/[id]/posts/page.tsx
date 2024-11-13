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
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";

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
}

export default async function UsersPostsPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [{ userPostsPageContentTexts, themeSwitchTexts, menuTexts }, authUser] =
    await Promise.all([getUserPostsPageTexts(), getTheSameUserOrAdmin(id)]);
  const postOptions = getSortingOptions(
    sortingPostsSortingOptionsKeys,
    userPostsPageContentTexts.sortingPostsSortingOptions,
  );

  // console.log("AUTH", authUser);
  return (
    <SidebarContentLayout
      navbarProps={{
        title: userPostsPageContentTexts.title,
        themeSwitchTexts,
        authUser,
        menuTexts,
        mappingKey: "trainer",
      }}
    >
      <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 mx-auto ">
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
    </SidebarContentLayout>
  );
}
