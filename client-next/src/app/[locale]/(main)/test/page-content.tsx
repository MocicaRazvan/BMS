"use client";

import { WithUser } from "@/lib/user";

import ArchiveQueuesTable, {
  ArchiveQueuesTableTexts,
} from "@/components/table/archive-queues-table";
import CustomEmojiPicker from "@/components/common/custom-emoji-picker";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { SmilePlus } from "lucide-react";
import { useState } from "react";
import { Emoji, EmojiStyle } from "emoji-picker-react";

interface Props extends WithUser {
  texts: ArchiveQueuesTableTexts;
}
export default function TestPage({ authUser, texts }: Props) {
  const [emoji, setEmoji] = useState<string | null>(null);
  return (
    <div className="flex flex-col items-center justify-center mt-20">
      {/*{emoji && (*/}
      {/*  <Emoji unified={emoji} size={25} emojiStyle={EmojiStyle.GOOGLE} />*/}
      {/*)}*/}
      {/*<DropdownMenu>*/}
      {/*  <DropdownMenuTrigger asChild>*/}
      {/*    <Button*/}
      {/*      variant="ghost"*/}
      {/*      size="sm"*/}
      {/*      type="button"*/}
      {/*      className="py-1 px-2.5 outline-none"*/}
      {/*    >*/}
      {/*      <SmilePlus className="h-4 w-4" />*/}
      {/*    </Button>*/}
      {/*  </DropdownMenuTrigger>*/}
      {/*  <DropdownMenuContent className="w-full md:w-fit">*/}
      {/*    <CustomEmojiPicker*/}
      {/*      onEmojiClick={(e) => {*/}
      {/*        console.log("Emoji clicked", e);*/}
      {/*        setEmoji(e.unified);*/}
      {/*      }}*/}
      {/*      reactionsDefaultOpen={true}*/}
      {/*      allowExpandReactions={true}*/}
      {/*      reactions={["1f602", "1f44d", "2764-fe0f", "1f44e", "1f620"]}*/}
      {/*    />*/}
      {/*  </DropdownMenuContent>*/}
      {/*</DropdownMenu>*/}
    </div>
  );
}
