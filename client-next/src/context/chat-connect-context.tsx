"use client";

import { ReactNode, useEffect } from "react";
import { useStompClient } from "react-stomp-hooks";
import { useSession } from "next-auth/react";

export default function ChatConnectContext({
  children,
}: {
  children: ReactNode;
}) {
  const session = useSession();
  const authUser = session.data?.user;
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
