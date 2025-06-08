"use client";

import {
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Link } from "@/navigation";
import { ArrowDown, ArrowUp } from "lucide-react";
import React, {
  Dispatch,
  RefObject,
  SetStateAction,
  useEffect,
  useMemo,
} from "react";
import Fuse, { FuseResultMatch } from "fuse.js";
import { useDebounceWithCallBack } from "@/hoooks/useDebounceWithCallback";
import { IntlMetadata } from "@/texts/metadata";
import { Role } from "@/types/fetch-utils";
import { cn } from "@/lib/utils";

const CUTOFF = 0.35;
const TITLE_WEIGHT = 0.7;
const DESCRIPTION_WEIGHT = 0.4;
const KEYWORDS_WEIGHT = 0.2;

export interface FindInSiteTexts {
  title: string;
  noResults: string;
  navigateText: string;
  selectText: string;
  closeText: string;
  placeholder: string;
  pressText: string;
}

export interface MetadataValue {
  metadata: IntlMetadata;
  key: string;
  path: string;
  role: Role | "ROLE_PUBLIC";
}
export interface FindInSiteContentBaseProps {
  texts: FindInSiteTexts;
  metadataValues: MetadataValue[];
}

const DEBOUNCE_TIME = 100 as const;
const highlightMatchFuseMatch = (
  text: string,
  words: string[],
  minLength?: number,
) => {
  const regex = new RegExp(
    `(${words.map((word) => word.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")).join("|")})`,
    "gi",
  );

  const parts = text.split(regex).map((word) => ({
    word,
    highlight: regex.test(word) && word.length > (minLength ?? 0),
  }));

  return (
    <>
      {parts.map(({ word, highlight }, i) => (
        <span
          key={i}
          className={cn("transition-colors", highlight && "text-success")}
        >
          {word}
        </span>
      ))}
    </>
  );
};

const highlightMatch = (text: string, query: string) => {
  if (!query.trim()) return text;

  const regex = new RegExp(
    `(${query.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")})`,
    "gi",
  );
  const parts = text.split(regex);

  return (
    <>
      {parts.map((part, i) => (
        <span
          key={i}
          className={cn(
            "transition-colors",
            regex.test(part) && "text-success",
          )}
        >
          {part}
        </span>
      ))}
    </>
  );
};

export type SearchMatches = Record<
  "metadata.title" | "metadata.description" | "metadata.keywords",
  string[]
>;

export type FindInSiteContentResult = MetadataValue & {
  matches: SearchMatches;
};

interface FindInSiteContentProps extends FindInSiteContentBaseProps {
  setOpen: Dispatch<SetStateAction<boolean>>;
  searchQuery: string;
  results: FindInSiteContentResult[];
  setResults: Dispatch<SetStateAction<FindInSiteContentResult[]>>;
  selectedIndex: number;
  setSelectedIndex: Dispatch<SetStateAction<number>>;
  handleKeyDown: (e: React.KeyboardEvent) => void;
  setSearchQuery: Dispatch<SetStateAction<string>>;
  inputRef: RefObject<HTMLInputElement>;
}
export default function FindInSiteContent({
  metadataValues,
  setOpen,
  searchQuery,
  setResults,
  setSelectedIndex,
  selectedIndex,
  handleKeyDown,
  setSearchQuery,
  inputRef,
  results,
  texts: {
    closeText,
    navigateText,
    selectText,
    placeholder,
    noResults,
    title,
    pressText,
  },
}: FindInSiteContentProps) {
  const debouncedQuery = useDebounceWithCallBack(searchQuery, DEBOUNCE_TIME);

  const fuse = useMemo(
    () =>
      new Fuse(metadataValues, {
        keys: [
          { name: "metadata.title", weight: TITLE_WEIGHT },
          { name: "metadata.description", weight: DESCRIPTION_WEIGHT },
          {
            name: "metadata.keywords",
            weight: KEYWORDS_WEIGHT,
          },
        ],
        isCaseSensitive: false,
        ignoreDiacritics: true,
        includeScore: true,
        threshold: 0.4,
        fieldNormWeight: 0.7,
        ignoreLocation: true,
        includeMatches: true,
        findAllMatches: true,
      }),
    [metadataValues],
  );
  useEffect(() => {
    const handleSearch = () => {
      const filteredQuery = debouncedQuery
        .replace(/\s+/g, " ")
        .trim()
        .normalize("NFD")
        .toLowerCase();

      if (filteredQuery === "") {
        setResults([]);
        return;
      }
      // console.log(
      //   "scores",
      //   fuse.search(filteredQuery).map((i) => i.score),
      // );
      const filteredResults: FindInSiteContentResult[] = fuse
        .search(filteredQuery)
        .filter((i) => (i.score ?? 1) <= CUTOFF && i.matches !== undefined)
        .map((i) => ({
          ...i.item,
          matches: (i.matches as FuseResultMatch[]).reduce((acc, cur) => {
            if (!cur?.value || !cur?.key) return acc;

            const matchedChars = cur.indices.map(([start, end]) =>
              (cur.value as string).slice(start, end + 1),
            );

            const key = cur.key as keyof SearchMatches;
            acc[key] = [...(acc[key] ?? []), ...matchedChars];

            return acc;
          }, {} as Partial<SearchMatches>),
        }))
        .map((i) => ({
          ...i,
          matches: {
            "metadata.description": i.matches["metadata.description"] ?? [],
            "metadata.keywords": i.matches["metadata.keywords"] ?? [],
            "metadata.title": i.matches["metadata.title"] ?? [],
          },
        }));
      // console.log("scores", filteredResults);
      setResults(filteredResults);
      setSelectedIndex(0);
    };
    handleSearch();
  }, [debouncedQuery, fuse]);

  return (
    <DialogContent className="sm:max-w-[550px] p-0 rounded-xl z-[110]">
      <DialogHeader className="px-4 pt-4 pb-0">
        <DialogTitle>{title}</DialogTitle>
      </DialogHeader>
      <div className="px-4 py-3">
        <div className="relative">
          <Input
            ref={inputRef}
            placeholder={placeholder}
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={handleKeyDown}
            className="w-full"
          />
        </div>
      </div>

      {results.length > 0 && (
        <ScrollArea className="max-h-[315px] overflow-y-auto border-t ">
          <div className="py-2">
            {results.map((result, index) => (
              <Link
                key={result.path}
                href={result.path}
                className={`block px-[18px] py-3 text-sm rounded ${
                  index === selectedIndex
                    ? "bg-muted text-muted-foreground-foreground scale-[1.01] transition-transform duration-150 ease-out"
                    : "hover:bg-muted"
                }`}
                onClick={() => setOpen(false)}
                onMouseEnter={() => setSelectedIndex(index)}
              >
                <div
                  className={`font-medium ${
                    index === selectedIndex
                      ? "font-semibold scale-[1.01] transition-transform duration-150 ease-out"
                      : "font-medium"
                  }`}
                >
                  {highlightMatchFuseMatch(
                    result.metadata.title,
                    result.matches["metadata.title"],
                    searchQuery.length / 2,
                  )}
                  {/*{highlightMatch(result.metadata.title, searchQuery)}*/}
                </div>
                <div
                  className={`text-xs ${
                    index === selectedIndex
                      ? "text-muted-foreground font-medium scale-[1.01] transition-transform duration-150 ease-out"
                      : "text-muted-foreground"
                  }`}
                >
                  {highlightMatchFuseMatch(
                    result.metadata.description,
                    result.matches["metadata.description"],
                    searchQuery.length / 2.5,
                  )}
                  {/*{highlightMatch(*/}
                  {/*  result.metadata.description,*/}
                  {/*  searchQuery,*/}
                  {/*)}*/}
                </div>
              </Link>
            ))}
          </div>
        </ScrollArea>
      )}

      {debouncedQuery && results.length === 0 && (
        <div className="px-4 py-6 text-center text-sm text-muted-foreground">
          {`${noResults} "${searchQuery}"`}
        </div>
      )}

      <div className="p-4 border-t text-xs text-muted-foreground">
        {pressText}
        <div className="inline space-x-0.5">
          <kbd className="px-1 rounded border bg-muted">
            <ArrowUp className="w-3 h-3 font-bold inline " />
          </kbd>
          <kbd className="px-1 rounded border bg-muted">
            <ArrowDown className="w-3 h-3 font-bold inline " />
          </kbd>
        </div>
        {navigateText}
        <kbd className="px-1 rounded border bg-muted">{"Enter"}</kbd>{" "}
        {selectText} <kbd className="px-1 rounded border bg-muted">{"Esc"}</kbd>{" "}
        {closeText}
      </div>
    </DialogContent>
  );
}
