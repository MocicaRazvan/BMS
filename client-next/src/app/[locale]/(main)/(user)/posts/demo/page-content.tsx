"use client";
import { Locale } from "@/navigation/navigation";
import GridList, {
  GridListTexts,
  SortingOption,
} from "@/components/list/grid-list";
import { PostResponse } from "@/types/dto";

import useTagsExtraCriteria, {
  UseTagsExtraCriteriaTexts,
} from "@/components/list/useTagsExtraCriteria";
import { SortingOptionsTexts } from "@/types/constants";
import { useSearchParams } from "next/navigation";
import React, { useCallback, useMemo, useState } from "react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
import {
  ExtraPostCriteria,
  PostExtraHeader,
} from "@/app/[locale]/(main)/(user)/posts/approved/page-content";

export interface ApprovedPostsTexts {
  gridListTexts: GridListTexts;
  sortingPostsSortingOptions: SortingOptionsTexts;
  tagsCriteriaTexts: UseTagsExtraCriteriaTexts;
  likedLabel: string;
  title: string;
  header: string;
}

interface Props extends ApprovedPostsTexts {
  options: SortingOption[];
  locale: Locale;
}

const BATCH_SIZE_OPTIONS = [1, 2, 3, 4, 5, 6] as const;
const DELAY_SIZE_OPTIONS = [0, 1, 2, 3] as const;

export default function DemoPostApprovedPageContent({
  header,
  sortingPostsSortingOptions,
  gridListTexts,
  title,
  options,
  tagsCriteriaTexts,
  likedLabel,
  locale,
}: Props) {
  const [batchSize, setBatchSize] = useState(1);
  const [delaySize, setDelaySize] = useState(0);
  const { extraUpdateSearchParams, extraArrayQueryParam, tags, setTags } =
    useTagsExtraCriteria();
  const searchParams = useSearchParams();

  const initialLiked = searchParams.get("liked");
  const [liked, setLiked] = useState(initialLiked === "true");
  const extraQueryParams = useMemo(
    () => ({
      approved: "true",
      ...(liked ? { liked: "true" } : {}),
      delay: `${delaySize}`,
    }),
    [delaySize, liked],
  );
  const finalExtraUpdateSearchParams = useCallback(
    (p: URLSearchParams) => {
      if (liked) {
        p.set("liked", "true");
      } else {
        p.delete("liked");
      }
      extraUpdateSearchParams(p);
    },
    [liked, extraUpdateSearchParams],
  );
  return (
    <section className="w-full min-h-[calc(100vh-4rem)] transition-all py-5 px-4 max-w-[1300px] mx-auto ">
      <h1 className="text-3xl mb-20">{"Demo Posts page showcase"}</h1>
      <div className="w-full grid grid-cols-2">
        <div className="w-full flex items-center justify-center gap-1">
          <p>{"Batch size"}</p>
          <Select
            value={`${batchSize}`}
            onValueChange={(v) => setBatchSize(parseInt(v))}
          >
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Batch Size" />
            </SelectTrigger>
            <SelectContent>
              {BATCH_SIZE_OPTIONS.map((o) => (
                <SelectItem key={`batch-size-${o}`} value={`${o}`}>
                  {o}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="w-full flex items-center justify-center gap-1">
          <p>{"Delay"}</p>
          <Select
            value={`${delaySize}`}
            defaultValue={`${0}`}
            onValueChange={(v) => setDelaySize(parseInt(v))}
          >
            <SelectTrigger className="w-[180px]">
              <SelectValue placeholder="Delay Size" />
            </SelectTrigger>
            <SelectContent>
              {DELAY_SIZE_OPTIONS.map((o) => (
                <SelectItem key={`delay-size-${o}`} value={`${o}`}>
                  {o}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>

      <Separator className="my-5" />

      <GridList<PostResponse>
        itemLinkCallback={({
          model: {
            content: { id },
          },
        }) => `/posts/single/${id}`}
        batchSize={batchSize}
        path="/posts/demo/withUser"
        sortingOptions={options}
        extraArrayQueryParam={extraArrayQueryParam}
        extraUpdateSearchParams={finalExtraUpdateSearchParams}
        extraQueryParams={extraQueryParams}
        {...gridListTexts}
        extraCriteriaClassname="items-start lg:gap-0"
        ExtraCriteria={(props) => (
          <ExtraPostCriteria
            {...props}
            tagsCriteriaTexts={tagsCriteriaTexts}
            likedLabel={likedLabel}
            setTags={setTags}
            tags={tags}
            liked={liked}
            setLiked={setLiked}
          />
        )}
        ItemExtraHeader={PostExtraHeader}
      />
    </section>
  );
}
