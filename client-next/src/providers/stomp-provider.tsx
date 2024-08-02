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
      debug={(str) => console.log(str)}
      onStompError={(err) => {
        console.log("Stomp error");
        console.error(err);
      }}
    >
      {children}
    </StompSessionProvider>
  );
};
