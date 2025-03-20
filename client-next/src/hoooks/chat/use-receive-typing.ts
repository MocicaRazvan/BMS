"use client";
import { useRef, useState } from "react";
import { ChatMessagePayload, JoinedConversationUser } from "@/types/dto";
import { useSubscription } from "react-stomp-hooks";
import { SENDING_TYPING_INTERVAL } from "@/hoooks/chat/use-send-typing";

interface Args {
  curUser: JoinedConversationUser;
}
const RECEIVE_TYPING_INTERVAL = SENDING_TYPING_INTERVAL + 50;
export default function useReceiveTyping({ curUser }: Args) {
  const otherTypingRefTimeouts = useRef<Record<number, NodeJS.Timeout | null>>(
    {},
  );
  const [typingRooms, setTypingRooms] = useState<
    Record<number, ChatMessagePayload>
  >({});

  const addTypingRoom = (room: ChatMessagePayload) => {
    setTypingRooms((prevState) => ({
      ...prevState,
      [room.chatRoomId]: room,
    }));
  };

  const removeTypingRoom = (roomId: number) => {
    setTypingRooms((prevState) => {
      const newState = { ...prevState };
      delete newState[roomId];
      return newState;
    });
  };

  const removeTimeoutAndTypingRoom = (roomId: number) => {
    removeTypingRoom(roomId);
    const otherTypingRefTimeout = otherTypingRefTimeouts.current[roomId];
    if (otherTypingRefTimeout) {
      clearTimeout(otherTypingRefTimeout);
    }
  };

  useSubscription(
    `/topic/typing-${curUser.conversationUser.email}`,
    (message) => {
      const newMessage = JSON.parse(message.body) as ChatMessagePayload;
      if (newMessage.senderEmail === curUser.conversationUser.email) return;
      const otherTypingRefTimeout =
        otherTypingRefTimeouts.current[newMessage.chatRoomId];
      if (otherTypingRefTimeout) {
        clearTimeout(otherTypingRefTimeout);
      }
      addTypingRoom(newMessage);
      otherTypingRefTimeouts.current[newMessage.chatRoomId] = setTimeout(() => {
        removeTypingRoom(newMessage.chatRoomId);
      }, RECEIVE_TYPING_INTERVAL);
    },
  );
  return {
    typingRooms,
    removeTimeoutAndTypingRoom,
  };
}
