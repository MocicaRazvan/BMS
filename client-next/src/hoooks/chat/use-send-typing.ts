"use client";

import { useCallback, useEffect, useRef } from "react";
import { useStompClient } from "react-stomp-hooks";
import {
  ChatMessagePayload,
  ChatRoomResponseJoined,
  JoinedConversationUser,
} from "@/types/dto";

interface Args {
  curUser: JoinedConversationUser;
  otherUser: JoinedConversationUser;
  curRoom: ChatRoomResponseJoined;
}
export const SENDING_TYPING_INTERVAL = 300 as const;
export default function useSendTyping({ curRoom, curUser, otherUser }: Args) {
  const stompClient = useStompClient();
  const typingTimeoutRef = useRef<NodeJS.Timeout | null>(null);
  const changesMadeTypingRef = useRef<boolean>(false);

  const sendTyping = useCallback(() => {
    changesMadeTypingRef.current = false;
    if (!stompClient?.connected) return;
    const payload: ChatMessagePayload = {
      chatRoomId: curRoom.id,
      senderEmail: curUser.conversationUser.email,
      receiverEmail: otherUser.conversationUser.email,
      content: "typing",
    };
    stompClient.publish({
      destination: "/app/sendTyping",
      body: JSON.stringify(payload),
    });
  }, [
    curRoom.id,
    curUser.conversationUser.email,
    otherUser.conversationUser.email,
    stompClient?.connected,
  ]);

  const onValueChange = useCallback((value: string) => {
    if (value.replace(/\s/g, "").trim() !== "") {
      changesMadeTypingRef.current = true;
    }
  }, []);

  useEffect(() => {
    typingTimeoutRef.current = setInterval(() => {
      if (changesMadeTypingRef.current) {
        sendTyping();
      }
    }, SENDING_TYPING_INTERVAL);

    return () => {
      if (typingTimeoutRef.current) clearInterval(typingTimeoutRef.current);
    };
  }, [SENDING_TYPING_INTERVAL, sendTyping]);

  return {
    onValueChange,
  };
}
