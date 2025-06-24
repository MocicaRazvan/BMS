import { Locale } from "@/navigation/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import UserPostsPageContent, {
  UserPostsPageContentTexts,
} from "@/app/[locale]/trainer/user/[id]/posts/page-content";
import { getUserPostsPageTexts } from "@/texts/pages";
import { getSortingOptions } from "@/types/constants";
import { sortingPostsSortingOptionsKeys } from "@/texts/components/list";
import Heading from "@/components/common/heading";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Separator } from "@/components/ui/separator";
import TopViewedPosts, {
  TopViewedPostsTexts,
} from "@/components/charts/top-viewed-posts";
import IsTheSameUserOrAdmin from "@/app/[locale]/trainer/user/is-the-same-user-or-admin";
import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

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
  ] = await Promise.all([getUserPostsPageTexts()]);
  const postOptions = getSortingOptions(
    sortingPostsSortingOptionsKeys,
    userPostsPageContentTexts.sortingPostsSortingOptions,
  );

  return (
    <IsTheSameUserOrAdmin id={id}>
      <SidebarContentLayout
        navbarProps={{
          title: userPostsPageContentTexts.title,
          themeSwitchTexts,
          menuTexts,
          mappingKey: "trainer",
          findInSiteTexts,
          locale,
        }}
      >
        <div className="space-y-10 lg:space-y-16 w-full transition-all py-5 px-4 mx-auto">
          <Heading {...userPostsPageContentTexts} />
          <div className="space-y-10 pb-5">
            <UserPostsPageContent
              id={id}
              sortingOptions={postOptions}
              {...userPostsPageContentTexts}
            />
            <Separator className="mt-2" />
            <div className=" my-5 h-full w-full">
              <TopViewedPosts
                path={`/posts/viewStats/${id}`}
                texts={topViewedPostsTexts}
                locale={locale}
              />
            </div>
          </div>
        </div>
      </SidebarContentLayout>
    </IsTheSameUserOrAdmin>
  );
}
