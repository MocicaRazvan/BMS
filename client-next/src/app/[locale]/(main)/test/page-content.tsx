"use client";

import { Role } from "@/types/fetch-utils";
import useList from "@/hoooks/useList";
import { PostResponse, ResponseWithUserDtoEntity } from "@/types/dto";
import { SortingOption } from "@/components/list/grid-list";
import ArchiveQueueCards, {
  ArchiveQueueCardsTexts,
} from "@/components/archive/archive-queue-card";
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
import useFetchStream from "@/hoooks/useFetchStream";
import { Button } from "@/components/ui/button";

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
  const { messages, isFinished, refetch, manualFetcher } = useFetchStream({
    path: "/posts/tags/withUser",
    authToken: true,
    queryParams: {
      approved: "true",
    },
    method: "PATCH",
    body: {
      page: 0,
      size: 10,
    },
  });
  useEffect(() => {
    if (isFinished) {
      console.log("useFetchStream Initial fetch done, cache has:", messages);
    }
  }, [isFinished, messages]);

  return (
    <div style={{ padding: 20 }}>
      <p>
        <strong>messages:</strong> [{messages.length}] <br />
        <strong>isFinished:</strong> {String(isFinished)}
      </p>
      <Button
        onClick={async () => {
          console.log("useFetchStream Clicking refetch()");
          refetch();
          await manualFetcher({
            localAuthToken: true,
            fetchProps: {
              path: "/posts/tags/withUser",
              method: "PATCH",
              queryParams: { approved: "true" },
              arrayQueryParam: {},
              customHeaders: {},
              body: { page: 0, size: 10 },
            },
          });
        }}
      >
        Refetch
      </Button>
    </div>
  );
}
