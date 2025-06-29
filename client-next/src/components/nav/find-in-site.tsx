"use client";

import {
  useCallback,
  useEffect,
  useRef,
  useState,
  KeyboardEvent as ReactKeyboardEvent,
} from "react";
import { Command } from "lucide-react";
import { Dialog, DialogContent, DialogTitle } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { useRouter } from "@/navigation/client-navigation";
import {
  FindInSiteContentBaseProps,
  SearchMatches,
} from "@/components/nav/find-in-site-content";
import dynamic from "next/dynamic";
import { Skeleton } from "@/components/ui/skeleton";

const DynamicFindInSiteContent = dynamic(
  () => import("@/components/nav/find-in-site-content"),
  {
    ssr: false,
    loading: () => (
      <DialogContent className="sm:max-w-[550px] p-0 rounded-xl z-[110] min-h-96">
        <DialogTitle className="sr-only">{"Find in Site"}</DialogTitle>
        <Skeleton className="size-full" />
      </DialogContent>
    ),
  },
);

const FindInSite = (props: FindInSiteContentBaseProps) => {
  const [open, setOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [results, setResults] = useState<
    (FindInSiteContentBaseProps["metadataValues"][number] & {
      matches: SearchMatches;
    })[]
  >([]);
  const [selectedIndex, setSelectedIndex] = useState(0);
  const inputRef = useRef<HTMLInputElement>(null);
  const router = useRouter();

  const onOpenChange = useCallback((open: boolean) => {
    if (!open) {
      setOpen(false);
      setSearchQuery("");
      setResults([]);
      setSelectedIndex(0);
    } else {
      setOpen(true);
    }
  }, []);

  const toggleSearch = useCallback(() => {
    setOpen((p) => !p);
    setSearchQuery("");
    setResults([]);
    setSelectedIndex(0);
  }, []);

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

  const handleKeyDown = useCallback(
    (e: ReactKeyboardEvent) => {
      if (results.length === 0) return;

      if (e.key === "ArrowDown") {
        e.preventDefault();
        setSelectedIndex((prev) =>
          prev < results.length - 1 ? prev + 1 : prev,
        );
      } else if (e.key === "ArrowUp") {
        e.preventDefault();
        setSelectedIndex((prev) => (prev > 0 ? prev - 1 : 0));
      } else if (e.key === "Enter") {
        e.preventDefault();
        if (results[selectedIndex]) {
          setOpen(false);
          router.push(results[selectedIndex].path);
        }
      }
    },
    [results, router, selectedIndex],
  );

  return (
    <>
      <Button
        variant="outline"
        size="sm"
        className="gap-2 hidden sm:flex px-1.5 py-1.5 bg-muted/50"
        onClick={toggleSearch}
      >
        <span>{props.texts.title}</span>

        <kbd className="pointer-events-none inline-flex h-5 select-none items-center gap-1 rounded border bg-muted px-1.5 font-mono text-[10px] font-medium text-muted-foreground ml-auto">
          <span>
            <Command className="h-4 w-4" />
          </span>
          <span className="text-xs">{"K"}</span>
        </kbd>
      </Button>

      <Dialog open={open} onOpenChange={onOpenChange}>
        <DynamicFindInSiteContent
          {...props}
          searchQuery={searchQuery}
          setSearchQuery={setSearchQuery}
          results={results}
          setResults={setResults}
          selectedIndex={selectedIndex}
          setSelectedIndex={setSelectedIndex}
          inputRef={inputRef}
          handleKeyDown={handleKeyDown}
          setOpen={setOpen}
        />
      </Dialog>
    </>
  );
};

FindInSite.displayName = "FindInSite";

export default FindInSite;
