"use client";

import { WithUser } from "@/lib/user";
import { useStompClient } from "react-stomp-hooks";
import { Suspense, useEffect } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import ChatRooms, { ChatRoomsTexts } from "@/components/chat/chat-rooms";

export interface ChatPageTexts {
  chatRoomsTexts: ChatRoomsTexts;
  noChatSelectedText: string;
  headingText: string;
}

interface Props extends WithUser, ChatPageTexts {}
export default function ChatPageMainPageContent({
  authUser,
  chatRoomsTexts,
  noChatSelectedText,
  headingText,
}: Props) {
  const stompClient = useStompClient();

  useEffect(() => {
    let mounted = true;
    if (stompClient?.connected && mounted) {
      stompClient?.publish({
        destination: "/app/changeRoom",
        body: JSON.stringify({
          chatId: null,
          userEmail: authUser.email,
        }),
      });
    }
    return () => {
      mounted = false;
    };
  }, [stompClient?.connected, authUser.email]);
  return (
    <section className="w-full h-full">
      <div className="hidden w-full h-full p-2 md:p-20 md:flex flex-col items-center justify-center gap-7">
        <div className="text-4xl font-bold tracking-tighter">
          {noChatSelectedText}
        </div>
        <p>{headingText}</p>
      </div>
      <div className="md:hidden h-full ">
        <Suspense fallback={<LoadingSpinner />}>
          <ChatRooms {...chatRoomsTexts} />
        </Suspense>
      </div>
    </section>
  );
}
