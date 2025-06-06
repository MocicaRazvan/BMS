"use client";

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { SmilePlus } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import dynamic from "next/dynamic";
import { Skeleton } from "@/components/ui/skeleton";

export interface EditorEmojiPickerTexts {
  searchPlaceholder: string;
}

interface Props {
  onEmojiSelect: (e: string) => void;
  texts: EditorEmojiPickerTexts;
}

const DynamicEmojiPicker = dynamic(
  () => import("@/components/common/custom-emoji-picker"),
  {
    ssr: false,
    loading: () => <Skeleton className="h-36 w-36 md:h-[450px] md:w-[350px]" />,
  },
);

export default function EditorEmojiPicker({
  onEmojiSelect,
  texts: { searchPlaceholder },
}: Props) {
  const [open, setOpen] = useState(false);
  return (
    <DropdownMenu open={open} onOpenChange={setOpen}>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="sm"
          type="button"
          className="py-1 px-2.5 outline-none"
        >
          <SmilePlus className="h-4 w-4" />
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-full md:w-fit">
        <DynamicEmojiPicker
          onEmojiClick={(e) => {
            onEmojiSelect(e.emoji);
          }}
          searchPlaceholder={searchPlaceholder}
        />
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
