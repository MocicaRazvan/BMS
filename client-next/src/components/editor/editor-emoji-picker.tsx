"use client";

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { SmilePlus } from "lucide-react";
import { useState } from "react";
import EmojiPicker, { EmojiStyle, Theme } from "emoji-picker-react";
import { useTheme } from "next-themes";
import { Button } from "@/components/ui/button";

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
  const theme = useTheme();
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
        <EmojiPicker
          className="!bg-background  [&_input]:!bg-background
          [&_h2]:!bg-background/35 [&_h2]:!backdrop-blur
            [&_h2]:!supports-[backdrop-filter]:bg-background/60
            [&_button]:!text-primary
          "
          theme={theme.theme === "dark" ? Theme.DARK : Theme.LIGHT}
          emojiStyle={EmojiStyle.GOOGLE}
          onEmojiClick={(e) => {
            onEmojiSelect(e.emoji);
          }}
          searchPlaceholder={searchPlaceholder}
        />
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
