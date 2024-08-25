import { Locale } from "@/navigation";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SinglePostPageContent, {
  SinglePostPageTexts,
} from "@/app/[locale]/(main)/(user)/posts/single/[id]/page-content";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUser } from "@/lib/user";
import { getTrainerPostPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  params: { locale: Locale; id: string };
}
export interface TrainerPostPageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  menuTexts: SidebarMenuTexts;
  singlePostPageTexts: SinglePostPageTexts;
}
export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "trainer.SinglePost",
    "/trainer/posts/single/" + id,
    locale,
  );
}

export default async function TrainerPostPage({
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);

  const [authUser, texts] = await Promise.all([
    getUser(),
    getTrainerPostPageTexts(),
  ]);

  return (
    <SidebarContentLayout
      navbarProps={{
        ...texts,
        authUser,
        mappingKey: "trainer",
      }}
    >
      <div className="w-full bg-background ">
        <Suspense fallback={<LoadingSpinner />}>
          <div className="mt-5">
            <SinglePostPageContent
              authUser={authUser}
              {...texts.singlePostPageTexts}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
