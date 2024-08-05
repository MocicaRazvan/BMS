"use client";

import { StompSessionProvider } from "react-stomp-hooks";
import * as React from "react";
import { Session } from "next-auth";

export const StompProvider = ({
  children,
  url,
  authUser,
}: {
  children: React.ReactNode;
  url: string;
  authUser: Session["user"];
}) => {
  const headers = {
    Authorization: `Bearer ${authUser?.token}`,
  };
  const conRec = authUser ? 1000 : 0;
  return (
    <StompSessionProvider
      url={url + `?authToken=${authUser?.token}`}
      connectHeaders={headers}
      connectionTimeout={conRec}
      reconnectDelay={conRec}
      logRawCommunication={true}
      debug={(str) => console.log(`STOMP debug: ${str}`)}
      onConnect={() => {
        console.log("Connected to STOMP broker with url " + url);
      }}
      onDisconnect={() => {
        console.log("Disconnected from STOMP broker");
      }}
      onStompError={(frame) => {
        console.log("STOMP error: ", frame.headers["message"]);
        // console.error("Detailed STOMP error: ", frame.body);
      }}
      onWebSocketError={(err) => {
        console.log("STOMP error WebSocket error: ", err.message);
        // console.error("Detailed WebSocket error: ", err);
      }}
    >
      {children}
    </StompSessionProvider>
  );
};
