import { useSearchParams } from "next/navigation";
import {
  Dispatch,
  SetStateAction,
  useCallback,
  useMemo,
  useState,
} from "react";
import MultipleSelector, {
  Option,
  useDebounce,
} from "@/components/ui/multiple-selector";
import { tagsOptions } from "@/types/constants";

export interface UseTagsExtraCriteriaTexts {
  tagsEmpty: string;
  tagsPlaceholder: string;
}

export const TAGS_KEY = "tags" as const;

export default function useTagsExtraCriteria() {
  const currentSearchParams = useSearchParams();
  const searchTags = currentSearchParams.get(TAGS_KEY);
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

  return {
    extraArrayQueryParam,
    extraUpdateSearchParams,
    tags,
    setTags,
  };
}
interface TagsExtraCriteriaWithCallbackProps {
  texts: UseTagsExtraCriteriaTexts;
  setTags: Dispatch<SetStateAction<Option[]>>;
  tags: Option[];
  callback: () => void;
}
export function TagsExtraCriteriaWithCallback({
  texts: { tagsPlaceholder, tagsEmpty },
  tags,
  setTags,
  callback,
}: TagsExtraCriteriaWithCallbackProps) {
  return (
    <div className="w-full max-w-[400px]">
      <MultipleSelector
        className="max-w-[400px] overflow-y-auto"
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
  );
}
