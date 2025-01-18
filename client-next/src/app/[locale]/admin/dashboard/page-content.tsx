"use client";

import { WithUser } from "@/lib/user";
import { ArchiveQueueCardsTexts } from "@/components/common/archive-queue-card";
import RelativeItem, {
  relativeItems,
  RelativeItems,
  RelativeItemsSummary,
  RelativeItemsSummaryTexts,
  RelativeItemTexts,
} from "@/components/charts/relative-item";
import { ThemeSwitchTexts } from "@/texts/components/nav";
import { useMemo, useState } from "react";
import { motion } from "framer-motion";
import { SidebarMenuTexts } from "@/components/sidebar/menu-list";
import { ArchiveQueuePrefix } from "@/types/dto";
import { Locale } from "@/navigation";
import ArchiveContent, {
  AugmentedArchiveQueuePrefix,
} from "@/app/[locale]/admin/dashboard/archive-content";
import TopUsers, { TopUsersTexts } from "@/components/charts/top-users";
import { Separator } from "@/components/ui/separator";

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
}

interface Props extends WithUser, AdminDashboardPageTexts {
  locale: Locale;
}

export default function AdminDashboardPageContent({
  authUser,
  relativeItemTexts,
  relativeItemsSummaryTexts,
  archiveTexts,
  locale,
  archiveTitle,
  selectItems,
  topUsersTexts,
}: Props) {
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
      <div className=" my-5 h-full w-full">
        <TopUsers texts={topUsersTexts} locale={locale} />
      </div>
      <Separator />
      <div className="w-full h-full mt-5 md:mt-12">
        <ArchiveContent
          locale={locale}
          archiveTexts={archiveTexts}
          archiveTitle={archiveTitle}
          authUser={authUser}
          selectItems={selectItems}
        />
      </div>
    </>
  );
}
