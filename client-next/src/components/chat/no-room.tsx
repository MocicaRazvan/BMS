"use client";

import { WithUser } from "@/lib/user";
import { useStompClient } from "react-stomp-hooks";
import { useEffect } from "react";

interface Props extends WithUser {}
export default function NoRoom({ authUser }: Props) {
  const stompClient = useStompClient();

  useEffect(() => {
    return () => {
      if (stompClient?.connected) {
        stompClient?.publish({
          destination: "/app/changeRoom",
          body: JSON.stringify({
            chatId: null,
            userEmail: authUser.email,
          }),
        });
      }
    };
  }, [stompClient?.connected, authUser.email]);
  return <></>;
}
