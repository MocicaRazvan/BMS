"use client";

import { Session } from "next-auth";
import { ReactNode, useEffect } from "react";
import { useStompClient } from "react-stomp-hooks";

export default function ChatConnectContext({
  authUser,
  children,
}: {
  authUser: Session["user"];
  children: ReactNode;
}) {
  const stompClient = useStompClient();
  const userEmail = authUser?.email;
  useEffect(() => {
    if (!stompClient || !stompClient?.connected || !userEmail) return;
    const handleDisconnect = () => {
      if (!stompClient || !stompClient?.connected || !userEmail) return;
      stompClient.publish({
        destination: `/app/disconnectUser/${userEmail}`,
        body: JSON.stringify({
          email: userEmail,
        }),
      });
    };

    const handleConnect = () => {
      if (!stompClient || !stompClient?.connected || !userEmail) return;
      stompClient.publish({
        destination: `/app/connectUser/${userEmail}`,
        body: JSON.stringify({
          email: userEmail,
        }),
      });
    };

    handleConnect();

    window.addEventListener("beforeunload", handleDisconnect);

    return () => {
      handleDisconnect();
      window.removeEventListener("beforeunload", handleDisconnect);
    };
  }, [stompClient?.connected]);

  return children;
}
