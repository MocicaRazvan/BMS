"use client";

import React, {
  useState,
  useEffect,
  useRef,
  useCallback,
  useMemo,
} from "react";
import { ArrowDown, ArrowUp, Command } from "lucide-react";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { ScrollArea } from "@/components/ui/scroll-area";
import { IntlMetadata } from "@/texts/metadata";
import { Role } from "@/types/fetch-utils";
import Fuse, { FuseResultMatch } from "fuse.js";
import { Link } from "@/navigation";
import { useDebounceWithCallBack } from "@/hoooks/useDebounceWithCallback";

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

interface Props {
  texts: FindInSiteTexts;
  metadataValues: MetadataValue[];
}

const DEBOUNCE_TIME = 100 as const;
const highlightMatchFuseMatch = (text: string, words: string[]) => {
  const regex = new RegExp(
    `(${words.map((word) => word.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")).join("|")})`,
    "gi",
  );

  const parts = text.split(regex);

  return (
    <>
      {parts.map((part, i) =>
        regex.test(part) ? (
          <span key={i} className="text-success">
            {part}
          </span>
        ) : (
          part
        ),
      )}
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
      {parts.map((part, i) =>
        regex.test(part) ? (
          <span key={i} className="text-success">
            {part}
          </span>
        ) : (
          part
        ),
      )}
    </>
  );
};

type SearchMatches = Record<
  "metadata.title" | "metadata.description" | "metadata.keywords",
  string[]
>;
export default function FindInSite({
  metadataValues,
  texts: {
    closeText,
    navigateText,
    selectText,
    placeholder,
    noResults,
    title,
    pressText,
  },
}: Props) {
  const [open, setOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [results, setResults] = useState<
    (Props["metadataValues"][number] & { matches: SearchMatches })[]
  >([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);
  const debouncedQuery = useDebounceWithCallBack(searchQuery, DEBOUNCE_TIME);

  const fuse = useMemo(
    () =>
      new Fuse(metadataValues, {
        keys: [
          { name: "metadata.title", weight: 0.7 },
          { name: "metadata.description", weight: 0.4 },
          {
            name: "metadata.keywords",
            weight: 0.2,
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

  const toggleSearch = useCallback(() => {
    setOpen((p) => !p);
    setSearchQuery("");
    setResults([]);
    setSelectedIndex(0);
  }, []);
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
      const filteredResults: typeof results = fuse
        .search(filteredQuery)
        .filter((i) => (i.score ?? 1) <= 0.3 && i.matches !== undefined)
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

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (results.length === 0) return;

    if (e.key === "ArrowDown") {
      e.preventDefault();
      setSelectedIndex((prev) => (prev < results.length - 1 ? prev + 1 : prev));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setSelectedIndex((prev) => (prev > 0 ? prev - 1 : 0));
    } else if (e.key === "Enter") {
      e.preventDefault();
      if (results[selectedIndex]) {
        window.location.href = results[selectedIndex].path;
        setOpen(false);
      }
    }
  };

  useEffect(() => {
    const handleKeyPress = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key === "k") {
        e.preventDefault();
        toggleSearch();
      }
    };

    window.addEventListener("keydown", handleKeyPress);
    return () => window.removeEventListener("keydown", handleKeyPress);
  }, [toggleSearch]);

  useEffect(() => {
    if (open && inputRef.current) {
      setTimeout(() => {
        inputRef.current?.focus();
      }, 100);
    }
  }, [open]);

  return (
    <>
      <Button
        variant="outline"
        size="sm"
        className="gap-2 hidden sm:flex px-1.5 py-1.5 bg-muted/50"
        onClick={toggleSearch}
      >
        <span>{title}</span>

        <kbd className="pointer-events-none inline-flex h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium text-muted-foreground ml-auto">
          <span>
            <Command className="h-4 w-4" />
          </span>
          <span className="text-xs">{"K"}</span>
        </kbd>
      </Button>

      <Dialog open={open} onOpenChange={setOpen}>
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
                      {/*{highlightMatchFuseMatch(*/}
                      {/*  result.metadata.title,*/}
                      {/*  result.matches["metadata.title"],*/}
                      {/*)}*/}
                      {highlightMatch(result.metadata.title, searchQuery)}
                    </div>
                    <div
                      className={`text-xs ${
                        index === selectedIndex
                          ? "text-muted-foreground font-medium scale-[1.01] transition-transform duration-150 ease-out"
                          : "text-muted-foreground"
                      }`}
                    >
                      {/*{highlightMatchFuseMatch(*/}
                      {/*  result.metadata.description,*/}
                      {/*  result.matches["metadata.description"],*/}
                      {/*)}*/}
                      {highlightMatch(result.metadata.description, searchQuery)}
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
            {selectText}{" "}
            <kbd className="px-1 rounded border bg-muted">{"Esc"}</kbd>{" "}
            {closeText}
          </div>
        </DialogContent>
      </Dialog>
    </>
  );
}
