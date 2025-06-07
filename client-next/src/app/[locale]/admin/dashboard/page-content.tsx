"use client";

import { ArchiveQueueCardsTexts } from "@/components/common/archive-queue-card";
import RelativeItem, {
  RelativeItemsSummary,
  RelativeItemsSummaryTexts,
  RelativeItemTexts,
} from "@/components/charts/relative-item-wrapper";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { useMemo, useState } from "react";
import { motion } from "framer-motion";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { ArchiveQueuePrefix } from "@/types/dto";
import { Locale } from "@/navigation";
import ArchiveContent, {
  AugmentedArchiveQueuePrefix,
} from "@/components/archive/archive-content";
import TopUsers, { TopUsersTexts } from "@/components/charts/top-users";
import { Separator } from "@/components/ui/separator";
import { TopPlans, TopPlansTexts } from "@/components/charts/top-plans";
import TopTrainers, {
  TopTrainersTexts,
} from "@/components/charts/top-trainers";
import { FindInSiteTexts } from "@/components/nav/find-in-site";
import TopViewedPosts, {
  TopViewedPostsTexts,
} from "@/components/charts/top-viewed-posts";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import { RelativeItems } from "@/components/charts/relative-item-chart";
import { relativeItems } from "@/types/constants";

export interface AdminDashboardPageTexts {
  title: string;
  header: string;
  themeSwitchTexts: ThemeSwitchTexts;
  relativeItemTexts: Record<RelativeItems, RelativeItemTexts>;
  relativeItemsSummaryTexts: RelativeItemsSummaryTexts;
  menuTexts: SidebarMenuTexts;
  archiveTexts: Record<ArchiveQueuePrefix, ArchiveQueueCardsTexts>;
  archiveTitle: string;
  selectItems: Record<AugmentedArchiveQueuePrefix, string>;
  topUsersTexts: TopUsersTexts;
  topPlansTexts: TopPlansTexts;
  topTrainersTexts: TopTrainersTexts;
  findInSiteTexts: FindInSiteTexts;
  topViewedPostsTexts: TopViewedPostsTexts;
}

interface Props extends AdminDashboardPageTexts {
  locale: Locale;
}

export default function AdminDashboardPageContent({
  relativeItemTexts,
  relativeItemsSummaryTexts,
  archiveTexts,
  locale,
  archiveTitle,
  selectItems,
  topUsersTexts,
  topPlansTexts,
  topTrainersTexts,
  topViewedPostsTexts,
}: Props) {
  const { authUser } = useAuthUserMinRole();

  const [relativeItemsCount, setRelativeItemsCount] = useState<
    Record<RelativeItems, number>
  >({
    comments: 0,
    orders: 0,
    plans: 0,
    posts: 0,
    recipes: 0,
  });
  const [itemsFinished, setItemsFinished] = useState<
    Record<RelativeItems, boolean>
  >({
    comments: false,
    orders: false,
    plans: false,
    posts: false,
    recipes: false,
  });

  const itemsTexts: Record<RelativeItems, string> = useMemo(
    () =>
      Object.entries(relativeItemTexts).reduce<Record<RelativeItems, string>>(
        (acc, [key, value]) => ({ ...acc, [key]: value.type }),
        { comments: "", orders: "", plans: "", posts: "", recipes: "" },
      ),
    [relativeItemTexts],
  );

  return (
    <>
      <div className="w-full grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-10 pb-10">
        {relativeItems.map((item, i) => (
          <motion.div
            key={item + i}
            className="col-span-1 h-full"
            initial={{ opacity: 0, y: 50, scale: 0.5 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            transition={{ duration: 0.5, delay: i * 0.25 }}
          >
            <RelativeItem
              authUser={authUser}
              {...relativeItemTexts[item]}
              basePath={`/${item}`}
              updateAboveCount={(count) =>
                setRelativeItemsCount((prev) => ({ ...prev, [item]: count }))
              }
              updateAllFinished={(finished) =>
                setItemsFinished((prev) => ({ ...prev, [item]: finished }))
              }
            />
          </motion.div>
        ))}
        <motion.div
          className="col-span-1 h-full"
          initial={{ opacity: 0, y: 50, scale: 0.5 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          transition={{ duration: 0.5, delay: relativeItems.length * 0.25 }}
        >
          <RelativeItemsSummary
            items={relativeItemsCount}
            texts={itemsTexts}
            summaryTexts={relativeItemsSummaryTexts}
            allFinished={Object.values(itemsFinished).every((v) => v)}
          />
        </motion.div>
      </div>
      <Separator className="mt-2" />
      <div className="my-5 h-full w-full">
        <TopUsers texts={topUsersTexts} locale={locale} />
      </div>
      <Separator className="mt-2" />
      <div className="my-5 h-full w-full">
        <TopPlans
          texts={topPlansTexts}
          locale={locale}
          path="/orders/admin/topPlans"
        />
      </div>
      <Separator className="mt-2" />
      <div className="my-5 h-full w-full">
        <TopTrainers texts={topTrainersTexts} locale={locale} />
      </div>
      <Separator className="mt-2" />
      <div className="my-5 h-full w-full">
        <TopViewedPosts
          path="/posts/admin/viewStats"
          texts={topViewedPostsTexts}
        />
      </div>
      <Separator />
      <div className="w-full h-full mt-5 md:mt-12">
        <ArchiveContent
          locale={locale}
          archiveTexts={archiveTexts}
          archiveTitle={archiveTitle}
          selectItems={selectItems}
        />
      </div>
    </>
  );
}
