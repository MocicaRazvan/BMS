"use client";
import { Locale } from "@/navigation/navigation";
import GridList, {
  CriteriaProps,
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
import React, {
  Dispatch,
  memo,
  SetStateAction,
  useCallback,
  useMemo,
  useState,
} from "react";
import { Checkbox } from "@/components/ui/checkbox";
import { ThumbsUp } from "lucide-react";
import useEstimateReadingTimeText from "@/hoooks/posts/use-estimate-reading-time-text";
import { ExtraProps } from "@/components/list/item-card";
import { Option } from "@/components/ui/multiple-selector";
import { isDeepEqual } from "@/lib/utils";

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
  const extraQueryParams = useMemo(
    () => ({
      approved: "true",
      ...(liked ? { liked: "true" } : {}),
    }),
    [liked],
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
      <Heading title={title} header={header} />

      <GridList<PostResponse>
        itemLinkCallback={({
          model: {
            content: { id },
          },
        }) => `/posts/single/${id}`}
        path="/posts/tags/withUser"
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

export const ExtraPostCriteria = memo(
  ({
    callback,
    tagsCriteriaTexts,
    likedLabel,
    setTags,
    tags,
    liked,
    setLiked,
  }: CriteriaProps & {
    tagsCriteriaTexts: UseTagsExtraCriteriaTexts;
    likedLabel: string;
    setTags: Dispatch<SetStateAction<Option[]>>;
    tags: Option[];
    liked: boolean;
    setLiked: Dispatch<SetStateAction<boolean>>;
  }) => {
    return (
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
    );
  },
  isDeepEqual,
);

ExtraPostCriteria.displayName = "ExtraPostCriteria";

export const PostExtraHeader = memo(
  ({
    item: {
      model: { content: post },
    },
    locale,
  }: ExtraProps<PostResponse>) => {
    const estimatedReadingTime = useEstimateReadingTimeText(locale, post.body);

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
  },
);

PostExtraHeader.displayName = "PostExtraHeader";
