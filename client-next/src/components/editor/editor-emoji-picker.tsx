"use client";

import { SmilePlus } from "lucide-react";
import { useState } from "react";
import { Button } from "@/components/ui/button";
import { ComposedEmojiPickerTexts } from "@/components/ui/emoji-picker";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import dynamicWithPreload from "@/lib/dynamic-with-preload";
import { Skeleton } from "@/components/ui/skeleton";
import usePreloadDynamicComponents from "@/hoooks/use-prelod-dynamic-components";

export interface EditorEmojiPickerTexts extends ComposedEmojiPickerTexts {}

interface Props {
  onEmojiSelect: (e: string) => void;
  texts: EditorEmojiPickerTexts;
}

const DynamicComposedPicker = dynamicWithPreload(
  () => import("@/components/ui/emoji-picker").then((m) => m.ComposedPicker),
  {
    loading: () => <Skeleton className="h-36 w-36 md:h-[352px] md:w-[274px]" />,
  },
);

export default function EditorEmojiPicker({ onEmojiSelect, texts }: Props) {
  const [mousePreload, setMousePreload] = useState(false);
  usePreloadDynamicComponents(DynamicComposedPicker, mousePreload);
  return (
    <Popover modal={true}>
      <PopoverTrigger asChild>
        <Button
          variant="ghost"
          size="sm"
          type="button"
          className="py-1 px-2.5 outline-none"
          onMouseEnter={() => {
            setMousePreload((prev) => prev || true);
          }}
        >
          <SmilePlus className="h-4 w-4" />
        </Button>
      </PopoverTrigger>
      <PopoverContent className="w-fit p-0">
        <DynamicComposedPicker
          texts={texts}
          pickerProps={{
            onEmojiSelect: (e) => {
              onEmojiSelect(e.emoji);
            },
          }}
        />
      </PopoverContent>
    </Popover>
  );
}
