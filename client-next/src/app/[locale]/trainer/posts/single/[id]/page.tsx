import { Locale } from "@/navigation/navigation";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import SinglePostPageContent, {
  SinglePostPageTexts,
} from "@/app/[locale]/(main)/(user)/posts/single/[id]/page-content";
import { unstable_setRequestLocale } from "next-intl/server";
import { getTrainerPostPageTexts } from "@/texts/pages";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import ScrollProgress from "@/components/common/scroll-progress";

import { FindInSiteTexts } from "@/components/nav/find-in-site-content";

interface Props {
  params: { locale: Locale; id: string };
}
export interface TrainerPostPageTexts {
  themeSwitchTexts: ThemeSwitchTexts;
  title: string;
  menuTexts: SidebarMenuTexts;
  singlePostPageTexts: SinglePostPageTexts;
  findInSiteTexts: FindInSiteTexts;
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

  const [texts] = await Promise.all([getTrainerPostPageTexts()]);

  return (
    <SidebarContentLayout
      navbarProps={{
        ...texts,
        mappingKey: "trainer",
        locale,
      }}
    >
      <ScrollProgress />
      <div className="w-full bg-background ">
        <div className="mt-5">
          <SinglePostPageContent
            {...texts.singlePostPageTexts}
            locale={locale}
          />
        </div>
      </div>
    </SidebarContentLayout>
  );
}
