"use client";

import { StompSessionProvider } from "react-stomp-hooks";
import * as React from "react";
import { useSession } from "next-auth/react";

export const StompProvider = ({
  children,
  url,
}: {
  children: React.ReactNode;
  url: string;
}) => {
  const session = useSession();
  const authUser = session.data?.user;
  const headers = {
    Authorization: `Bearer ${authUser?.token}`,
  };
  const conRec = authUser ? 1000 : 0;
  const isProduction = process.env.NODE_ENV === "production";
  // const isProduction = true;
  const newUrl = url + `?authToken=${authUser?.token}`;
  console.log("STOMP URL: ", newUrl);
  return (
    <StompSessionProvider
      url={url}
      connectHeaders={headers}
      connectionTimeout={conRec}
      reconnectDelay={conRec}
      logRawCommunication={isProduction ? undefined : true}
      debug={(str) => {
        if (!isProduction) {
          console.log(`STOMP debug: ${str}`);
        }
      }}
      onConnect={() => {
        if (!isProduction) {
          console.log("Connected to STOMP broker with url " + url);
        }
      }}
      onDisconnect={() => {
        if (!isProduction) {
          console.log("Disconnected from STOMP broker");
        }
      }}
      onStompError={(frame) => {
        if (!isProduction) {
          console.log("STOMP error: ", frame.headers["message"]);
          // console.error("Detailed STOMP error: ", frame.body);
        }
      }}
      onWebSocketError={(err) => {
        if (!isProduction) {
          console.log("STOMP error WebSocket error: ", err.message);
          // console.error("Detailed WebSocket error: ", err);
        }
      }}
      heartbeatIncoming={30000}
      heartbeatOutgoing={30000}
      enabled={authUser?.token !== undefined}
    >
      {children}
    </StompSessionProvider>
  );
};
