"use client";

import EmojiPicker, {
  EmojiStyle,
  PickerProps,
  Theme,
} from "emoji-picker-react";
import { useTheme } from "next-themes";
import { cn } from "@/lib/utils";

export default function CustomEmojiPicker(props: PickerProps) {
  const theme = useTheme();

  return (
    <EmojiPicker
      className={cn(
        `!bg-background  [&_input]:!bg-background
          [&_h2]:!bg-background/35 [&_h2]:!backdrop-blur
        [&_h2]:!supports-[backdrop-filter]:bg-background/60
        [&_button]:!text-primary`,
        props.reactionsDefaultOpen && "!border-none",
      )}
      theme={theme.theme === "dark" ? Theme.DARK : Theme.LIGHT}
      emojiStyle={EmojiStyle.GOOGLE}
      // onEmojiClick={(e) => {
      //   onEmojiSelect(e.emoji);
      // }}
      // searchPlaceholder={searchPlaceholder}
      reactions={["1f602", "1f44d", "2764-fe0f", "1f44e", "1f620"]}
      {...props}
    />
  );
}
