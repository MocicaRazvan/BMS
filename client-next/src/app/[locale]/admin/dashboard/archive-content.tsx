"use client";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { ArchiveQueuePrefix, archiveQueuePrefixes } from "@/types/dto";
import { memo, useMemo, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import ArchiveQueueCards, {
  ArchiveQueueCardsTexts,
} from "@/components/common/archive-queue-card";
import { Locale } from "@/navigation";
import { WithUser } from "@/lib/user";
import { isDeepEqual } from "@/lib/utils";

const augmentedArchiveQueuePrefixes = ["all" as const, ...archiveQueuePrefixes];
export type AugmentedArchiveQueuePrefix =
  (typeof augmentedArchiveQueuePrefixes)[number];
export interface ArchiveContentTexts {
  selectItems: Record<AugmentedArchiveQueuePrefix, string>;
}

interface Props extends WithUser, ArchiveContentTexts {
  archiveTexts: Record<ArchiveQueuePrefix, ArchiveQueueCardsTexts>;
  archiveTitle: string;
  locale: Locale;
}

const ArchiveContent = memo(
  ({ archiveTexts, archiveTitle, locale, authUser, selectItems }: Props) => {
    const [selected, setSelected] =
      useState<AugmentedArchiveQueuePrefix>("all");
    const selectedPrefixes = useMemo(
      () => (selected === "all" ? archiveQueuePrefixes : [selected]),
      [selected],
    ) as ArchiveQueuePrefix[];
    return (
      <div className="w-full h-full space-y-2 md:space-y-5 py-2">
        <motion.div
          initial={{ opacity: 0 }}
          whileInView={{ opacity: 1 }}
          viewport={{ once: true }}
          className="flex items-center justify-between w-full  mb-12"
        >
          <h2 className="text-2xl lg:text-3xl font-bold tracking-tight capitalize inline">
            {archiveTitle}
          </h2>
          <Select
            value={selected}
            onValueChange={(v) => setSelected(v as AugmentedArchiveQueuePrefix)}
          >
            <SelectTrigger className="w-36">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {augmentedArchiveQueuePrefixes.map((prefix) => (
                <SelectItem
                  key="prefix"
                  value={prefix}
                  className="cursor-pointer capitalize"
                >
                  {selectItems[prefix]}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </motion.div>
        <AnimatePresence>
          {selectedPrefixes.map((p) => (
            <motion.div layout={"position"} key={p} className="w-full h-full">
              <ArchiveQueueCards
                prefix={p}
                locale={locale}
                {...archiveTexts[p]}
                authUser={authUser}
              />
            </motion.div>
          ))}
        </AnimatePresence>
      </div>
    );
  },
  isDeepEqual,
);

ArchiveContent.displayName = "ArchiveContent";

export default ArchiveContent;
