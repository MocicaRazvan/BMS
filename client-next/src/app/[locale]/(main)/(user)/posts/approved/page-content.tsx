"use client";
import { useRouter } from "@/navigation";
import GridList, {
  GridListTexts,
  SortingOption,
} from "@/components/list/grid-list";
import { PostResponse } from "@/types/dto";

import useTagsExtraCriteria, {
  UseTagsExtraCriteriaTexts,
} from "@/components/list/useTagsExtraCriteria";
import { SortingOptionsTexts } from "@/lib/constants";
import Heading from "@/components/common/heading";
import { useSearchParams } from "next/navigation";
import { useState } from "react";
import { Checkbox } from "@/components/ui/checkbox";

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
}

export default function PostApprovedPageContent({
  header,
  sortingPostsSortingOptions,
  gridListTexts,
  title,
  options,
  tagsCriteriaTexts,
  likedLabel,
}: Props) {
  const router = useRouter();
  const {
    extraUpdateSearchParams,
    extraCriteria,
    extraArrayQueryParam,
    extraCriteriaWithCallBack,
  } = useTagsExtraCriteria(tagsCriteriaTexts);
  const searchParams = useSearchParams();

  const initialLiked = searchParams.get("liked");
  const [liked, setLiked] = useState(initialLiked === "true");

  return (
    <section className="w-full min-h-[calc(100vh-4rem)] transition-all py-5 px-4 max-w-[1300px] mx-auto ">
      <Heading title={title} header={header} />

      <GridList<PostResponse>
        onItemClick={({
          model: {
            content: { id },
          },
        }) => {
          router.push(`/posts/single/${id}`);
        }}
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
        {...gridListTexts}
        extraCriteriaWithCallBack={(callback) => (
          <div className="flex items-start justify-center flex-wrap gap-10  flex-1">
            {extraCriteriaWithCallBack(callback)}
            <div className="flex items-center space-x-2 pt-2">
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
      />
    </section>
  );
}
