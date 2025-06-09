"use client";

import { useStompClient } from "react-stomp-hooks";
import { useEffect } from "react";
import ChatRooms, { ChatRoomsTexts } from "@/components/chat/chat-rooms";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

export interface ChatPageTexts {
  chatRoomsTexts: ChatRoomsTexts;
  noChatSelectedText: string;
  headingText: string;
}

interface Props extends ChatPageTexts {}
export default function ChatPageMainPageContent({
  chatRoomsTexts,
  noChatSelectedText,
  headingText,
}: Props) {
  const { authUser } = useAuthUserMinRole();

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
        <ChatRooms {...chatRoomsTexts} />
      </div>
    </section>
  );
}
