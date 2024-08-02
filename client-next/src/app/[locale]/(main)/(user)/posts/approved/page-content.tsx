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

export interface ApprovedPostsTexts {
  gridListTexts: GridListTexts;
  sortingPostsSortingOptions: SortingOptionsTexts;
  tagsCriteriaTexts: UseTagsExtraCriteriaTexts;
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
}: Props) {
  const router = useRouter();
  const {
    extraUpdateSearchParams,
    extraCriteria,
    extraArrayQueryParam,
    extraCriteriaWithCallBack,
  } = useTagsExtraCriteria(tagsCriteriaTexts);
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
        extraUpdateSearchParams={extraUpdateSearchParams}
        // extraCriteria={extraCriteria}
        extraQueryParams={{
          approved: "true",
        }}
        {...gridListTexts}
        extraCriteriaWithCallBack={extraCriteriaWithCallBack}
      />
    </section>
  );
}
