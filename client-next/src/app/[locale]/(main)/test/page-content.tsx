"use client";

import { Role } from "@/types/fetch-utils";
import useList from "@/hoooks/useList";
import { PostResponse, ResponseWithUserDtoEntity } from "@/types/dto";
import { SortingOption } from "@/components/list/grid-list";
import ArchiveQueueCards, {
  ArchiveQueueCardsTexts,
} from "@/components/common/archive-queue-card";
import { useLocale } from "next-intl";
import { Locale } from "@/navigation";
import { AuthUserMinRoleProvider } from "@/context/auth-user-min-role-context";
import ArchiveQueueUpdateProvider, {
  ArchiveQueueUpdateTexts,
} from "@/context/archive-queue-update-context";
import dynamic from "next/dynamic";
import { Skeleton } from "@/components/ui/skeleton";
import React, { useEffect } from "react";
import dynamicWithPreload from "@/lib/dynamic-with-preload";
import usePreloadDynamicComponents from "@/hoooks/use-prelod-dynamic-components";

interface Props {
  options: SortingOption[];
  archivePostsTexts: ArchiveQueueCardsTexts;
  queueTexts: ArchiveQueueUpdateTexts;
}
const minRole: Role = "ROLE_ADMIN";

const DynamicRatioPieChart = dynamicWithPreload(
  () =>
    import("@/components/charts/plans-ratio-pie-chart").then(
      (mod) => mod.RatioPieChart,
    ),
  {
    loading: () => (
      <div className="w-80 mx-auto py-16">
        <Skeleton className="aspect-auto h-[450px] w-full" />
      </div>
    ),
  },
);

export default function TestPage({}: Props) {
  const [show, setShow] = React.useState(false);
  usePreloadDynamicComponents(DynamicRatioPieChart, true);

  useEffect(() => {
    const timer = setTimeout(() => {
      setShow(true);
    }, 2200);
    return () => clearTimeout(timer);
  }, []);
  return (
    <div className="flex flex-col items-center justify-center mt-20">
      {show ? (
        <DynamicRatioPieChart
          innerLabel={"planCardTexts.ratioLabel"}
          chartData={[{ ratio: 0, fill: "var(--color-plan)" }]}
        />
      ) : (
        <div>Loading...</div>
      )}
    </div>
  );
}
