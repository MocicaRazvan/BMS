"use client";
import { Locale, useRouter } from "@/navigation";
import GridList, {
  GridListTexts,
  SortingOption,
} from "@/components/list/grid-list";
import { PostResponse } from "@/types/dto";

import useTagsExtraCriteria, {
  TagsExtraCriteriaWithCallback,
  UseTagsExtraCriteriaTexts,
} from "@/components/list/useTagsExtraCriteria";
import { SortingOptionsTexts } from "@/types/constants";
import { useSearchParams } from "next/navigation";
import React, { useState } from "react";
import { Checkbox } from "@/components/ui/checkbox";
import { readingTime } from "reading-time-estimator";
import { ThumbsUp } from "lucide-react";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Separator } from "@/components/ui/separator";
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
  const router = useRouter();
  const { extraUpdateSearchParams, extraArrayQueryParam, tags, setTags } =
    useTagsExtraCriteria();
  const searchParams = useSearchParams();

  const initialLiked = searchParams.get("liked");
  const [liked, setLiked] = useState(initialLiked === "true");

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
        onItemClick={({
          model: {
            content: { id },
          },
        }) => {
          router.push(`/posts/single/${id}`);
        }}
        sizeOptions={[6, 12, 18]}
        batchSize={batchSize}
        path="/posts/demo/withUser"
        sortingOptions={options}
        extraArrayQueryParam={extraArrayQueryParam}
        extraUpdateSearchParams={(p) => {
          if (liked) {
            p.set("liked", "true");
          } else {
            p.delete("liked");
          }
          extraUpdateSearchParams(p);
        }}
        // extraCriteria={extraCriteria}
        extraQueryParams={{
          approved: "true",
          ...(liked ? { liked: "true" } : {}),
          delay: `${delaySize}`,
        }}
        // passExtraContent={(item) => (
        //   <div>
        //     {item.model.content.userLikes.length}
        //     <div>{item.model.content.userDislikes.length}</div>
        //   </div>
        // )}
        {...gridListTexts}
        extraCriteriaClassname="items-start lg:gap-0"
        extraCriteriaWithCallBack={(callback) => (
          <div className="flex items-start justify-end flex-wrap gap-5 lg:gap-8 flex-1">
            <TagsExtraCriteriaWithCallback
              texts={tagsCriteriaTexts}
              setTags={setTags}
              tags={tags}
              callback={callback}
            />

            <div className="flex items-center space-x-2 pt-2 ">
              <Checkbox
                id="liked"
                checked={liked}
                onCheckedChange={(checked) => {
                  setLiked(checked === true);
                  callback();
                }}
              />
              <label
                htmlFor="liked"
                className=" font-medium leading-none capitalize"
              >
                {likedLabel}
              </label>
            </div>
          </div>
        )}
        passExtraHeader={(p) => (
          <div className="flex items-center gap-3.5 justify-start max-w-[300px]">
            <div className="flex items-start justify-center gap-0.5 font-semibold text-success">
              <span className="mt-0.5">{p.model.content.userLikes.length}</span>
              <ThumbsUp className="text-success" size={20} />
            </div>
            <p className="text-sm text-muted-foreground font-semibold">
              {readingTime(p.model.content.body, 200, locale).text}
            </p>
          </div>
        )}
      />
    </section>
  );
}
