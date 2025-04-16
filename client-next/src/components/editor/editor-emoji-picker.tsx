"use client";

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { SmilePlus } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import CustomEmojiPicker from "@/components/common/custom-emoji-picker";

export interface EditorEmojiPickerTexts {
  searchPlaceholder: string;
}

interface Props {
  onEmojiSelect: (e: string) => void;
  texts: EditorEmojiPickerTexts;
}

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
        <CustomEmojiPicker
          onEmojiClick={(e) => {
            onEmojiSelect(e.emoji);
          }}
          searchPlaceholder={searchPlaceholder}
        />
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
