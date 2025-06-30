"use client";

import { IFrame, StompSessionProvider } from "react-stomp-hooks";
import { useSession } from "next-auth/react";
import { ReactNode, useCallback, useMemo } from "react";

const isProduction = process.env.NODE_ENV === "production";
// const isProduction = true;
const CONNECTION_TIMEOUT = 6_000;
const RECONNECT_DELAY = 5_000;

export const StompProvider = ({
  children,
  url,
}: {
  children: ReactNode;
  url: string;
}) => {
  const session = useSession();
  const authUser = session.data?.user;
  const headers = useMemo(
    () => ({
      Authorization: `Bearer ${authUser?.token}`,
    }),
    [authUser?.token],
  );

  const debug = useCallback((str: string) => {
    if (!isProduction) {
      console.log(`STOMP debug: ${str}`);
    }
  }, []);

  const onConnect = useCallback(() => {
    if (!isProduction) {
      console.log("Connected to STOMP broker with url " + url);
    }
  }, [url]);

  const onDisconnect = useCallback(() => {
    if (!isProduction) {
      console.log("Disconnected from STOMP broker");
    }
  }, []);

  const onStompError = useCallback((frame: IFrame) => {
    if (!isProduction) {
      console.log("STOMP error: ", frame.headers["message"]);
      // console.error("Detailed STOMP error: ", frame.body);
    }
  }, []);

  const onWebSocketError = useCallback((err: any) => {
    if (!isProduction) {
      console.log("STOMP error WebSocket error: ", err.message);
      // console.error("Detailed WebSocket error: ", err);
    }
  }, []);

  return (
    <StompSessionProvider
      url={url}
      connectHeaders={headers}
      connectionTimeout={authUser?.token ? CONNECTION_TIMEOUT : 0}
      reconnectDelay={authUser?.token ? RECONNECT_DELAY : 0}
      logRawCommunication={isProduction ? undefined : true}
      debug={debug}
      onConnect={onConnect}
      onDisconnect={onDisconnect}
      onStompError={onStompError}
      onWebSocketError={onWebSocketError}
      heartbeatIncoming={30000}
      heartbeatOutgoing={30000}
      enabled={authUser?.token !== undefined}
    >
      {children}
    </StompSessionProvider>
  );
};
