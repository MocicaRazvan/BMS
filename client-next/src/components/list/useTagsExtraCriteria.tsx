import { useSearchParams } from "next/navigation";
import { useCallback, useMemo, useState } from "react";
import MultipleSelector, {
  Option,
  useDebounce,
} from "@/components/ui/multiple-selector";
import { tagsOptions } from "@/lib/constants";

export interface UseTagsExtraCriteriaTexts {
  tagsEmpty: string;
  tagsPlaceholder: string;
}

export default function useTagsExtraCriteria({
  tagsPlaceholder,
  tagsEmpty,
}: UseTagsExtraCriteriaTexts) {
  const currentSearchParams = useSearchParams();
  const searchTags = currentSearchParams.get("tags");
  const [tags, setTags] = useState<Option[]>(
    searchTags
      ? searchTags.split(",").reduce<Option[]>((acc, tag) => {
          const found = tagsOptions.find((t) => t.value === tag);
          if (found) {
            acc.push(found);
          }
          return acc;
        }, [])
      : [],
  );

  const debouncedTags = useDebounce(tags, 500);

  const extraArrayQueryParam = useMemo(
    () => ({
      tags: debouncedTags.map((tag) => tag.value),
    }),
    [debouncedTags],
  );
  const extraUpdateSearchParams = useCallback(
    (searchParams: URLSearchParams) => {
      if (tags.length == 0) {
        searchParams.delete("tags");
      } else {
        searchParams.set("tags", tags.map((tag) => tag.value).join(","));
      }
    },
    [tags],
  );

  const extraCriteria = useMemo(
    () => (
      <div className="flex-1">
        <MultipleSelector
          className="w-full h-full"
          value={tags}
          onChange={setTags}
          defaultOptions={tagsOptions}
          placeholder={tagsPlaceholder}
          emptyIndicator={
            <p className="text-center text-lg leading-10 text-gray-600 dark:text-gray-400">
              {tagsEmpty}
            </p>
          }
        />
      </div>
    ),
    [tags, tagsEmpty, tagsPlaceholder],
  );
  const extraCriteriaWithCallBack = useCallback(
    (callback: () => void) => (
      <div className="flex-1">
        <MultipleSelector
          className="w-full h-full max-w-[400px] overflow-y-auto"
          value={tags}
          onChange={(e) => {
            setTags(e);
            callback();
          }}
          defaultOptions={tagsOptions}
          placeholder={tagsPlaceholder}
          emptyIndicator={
            <p className="text-center text-lg leading-10 text-gray-600 dark:text-gray-400">
              {tagsEmpty}
            </p>
          }
        />
      </div>
    ),
    [tags, tagsEmpty, tagsPlaceholder],
  );
  return {
    extraCriteria,
    extraArrayQueryParam,
    extraUpdateSearchParams,
    extraCriteriaWithCallBack,
  };
}
