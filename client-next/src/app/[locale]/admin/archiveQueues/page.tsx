import { Locale } from "@/navigation";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { unstable_setRequestLocale } from "next-intl/server";
import { getAdminArchiveQueuesPageTexts } from "@/texts/pages";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { ArchiveQueuePrefix } from "@/types/dto";
import { ArchiveQueueCardsTexts } from "@/components/common/archive-queue-card";
import { ArchiveQueuesTableTexts } from "@/components/table/archive-queues-table";
import SidebarContentLayout from "@/components/sidebar/sidebar-content-layout";
import Heading from "@/components/common/heading";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import { FindInSiteTexts } from "@/components/nav/find-in-site";
import ArchiveContent, {
  AugmentedArchiveQueuePrefix,
} from "@/components/archive/archive-content";
import { Separator } from "@/components/ui/separator";
import ArchiveQueuesTableWrapper from "@/app/[locale]/admin/archiveQueues/table-wrapper";

interface Props {
  params: { locale: Locale };
}
export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return await getIntlMetadata(
    "admin.ArchiveQueues",
    "/admin/archiveQueues",
    locale,
  );
}

export interface AdminArchiveQueuesPageTexts {
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  menuTexts: SidebarMenuTexts;
  archiveTexts: Record<ArchiveQueuePrefix, ArchiveQueueCardsTexts>;
  archiveTitle: string;
  archiveQueueTableTexts: ArchiveQueuesTableTexts;
  archiveTableTitle: string;
  findInSiteTexts: FindInSiteTexts;
  selectItems: Record<AugmentedArchiveQueuePrefix, string>;
}
export default async function AdminArchiveQueues({
  params: { locale },
}: Props) {
  unstable_setRequestLocale(locale);
  const [texts] = await Promise.all([getAdminArchiveQueuesPageTexts()]);
  return (
    <SidebarContentLayout
      navbarProps={{
        title: texts.title,
        themeSwitchTexts: texts.themeSwitchTexts,
        menuTexts: texts.menuTexts,
        mappingKey: "admin",
        findInSiteTexts: texts.findInSiteTexts,
      }}
    >
      <div className="w-full h-full bg-background">
        <Heading {...texts} />
        <Suspense fallback={<LoadingSpinner />}>
          <h2 className="text-lg md:text-xl tracking-tight font-semibold mt-8 mb-3">
            {texts.archiveTableTitle}
          </h2>
          <ArchiveQueuesTableWrapper texts={texts.archiveQueueTableTexts} />
          <Separator className="my-10" />
          <div className="mt-10 h-full">
            <ArchiveContent
              locale={locale}
              archiveTexts={texts.archiveTexts}
              archiveTitle={texts.archiveTitle}
              selectItems={texts.selectItems}
            />
          </div>
        </Suspense>
      </div>
    </SidebarContentLayout>
  );
}
