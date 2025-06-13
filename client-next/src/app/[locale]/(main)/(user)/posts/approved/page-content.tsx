"use client";
import { Locale } from "@/navigation/navigation";
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
import Heading from "@/components/common/heading";
import { useSearchParams } from "next/navigation";
import React, { useState } from "react";
import { Checkbox } from "@/components/ui/checkbox";
import { ThumbsUp } from "lucide-react";
import useEstimateReadingTimeText from "@/hoooks/posts/use-estimate-reading-time-text";

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

export default function PostApprovedPageContent({
  header,
  sortingPostsSortingOptions,
  gridListTexts,
  title,
  options,
  tagsCriteriaTexts,
  likedLabel,
  locale,
}: Props) {
  const { extraUpdateSearchParams, extraArrayQueryParam, tags, setTags } =
    useTagsExtraCriteria();
  const searchParams = useSearchParams();

  const initialLiked = searchParams.get("liked");
  const [liked, setLiked] = useState(initialLiked === "true");

  return (
    <section className="w-full min-h-[calc(100vh-4rem)] transition-all py-5 px-4 max-w-[1300px] mx-auto ">
      <Heading title={title} header={header} />

      <GridList<PostResponse>
        itemLinkCallback={({
          model: {
            content: { id },
          },
        }) => `/posts/single/${id}`}
        sizeOptions={[6, 12, 18]}
        path="/posts/tags/withUser"
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
        passExtraHeader={(p) => <PostExtraHeader post={p.model.content} />}
      />
    </section>
  );
}

export function PostExtraHeader({ post }: { post: PostResponse }) {
  const estimatedReadingTime = useEstimateReadingTimeText(post.body);

  return (
    <div className="flex items-center gap-3.5 justify-start max-w-[300px]">
      <div className="flex items-start justify-center gap-0.5 font-semibold text-success">
        <span className="mt-0.5">{post.userLikes.length}</span>
        <ThumbsUp className="text-success" size={20} />
      </div>
      <p className="text-sm text-muted-foreground font-semibold">
        {estimatedReadingTime}
      </p>
    </div>
  );
}
