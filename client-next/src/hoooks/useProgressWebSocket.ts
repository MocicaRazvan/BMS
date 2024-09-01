"use client";

import { useEffect, useState } from "react";
import useWebSocket, { ReadyState } from "react-use-websocket";

interface ProgressUpdateDto {
  index: number;
  message: string;
  fileType: "IMAGE" | "VIDEO";
}

const useProgressWebSocket = (
  authToken: string,
  clientId: string,
  fileType: string,
) => {
  const [messages, setMessages] = useState<ProgressUpdateDto[]>([]);

  const wsUrl = `${process.env.NEXT_PUBLIC_SPRING_CLIENT_WEBSOCKET}/files/ws/progress/${clientId}?fileType=${fileType}&authToken=${authToken}`;

  const { sendMessage, lastMessage, readyState } = useWebSocket(wsUrl, {
    onOpen: () => {
      console.log(" useProgressWebSocket WebSocket connection established");
    },
    onClose: () => {
      console.log(" useProgressWebSocket WebSocket connection closed");
    },
    onError: (error) => {
      console.error("useProgressWebSocket WebSocket error:", error);
    },
    shouldReconnect: (closeEvent) => true,
  });

  useEffect(() => {
    if (lastMessage !== null) {
      const message: ProgressUpdateDto = JSON.parse(lastMessage.data);
      console.log("useProgressWebSocketData", message);
      if (message.index >= 0) {
        setMessages((prevMessages) => [...prevMessages, message]);
      }
    }
  }, [lastMessage]);

  return { messages, sendMessage, readyState };
};

export default useProgressWebSocket;
