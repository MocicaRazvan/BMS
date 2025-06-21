"use client";
import { Session } from "next-auth";
import { useEffect, useMemo, useRef, useState } from "react";
import { NotifyContainerAction } from "@/types/dto";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import useWebSocket from "react-use-websocket";
import { isNotifyContainerAction } from "@/context/archive-queue-update-context";

export default function useArchiveContainerNotifications(
  authUser: NonNullable<Session["user"]>,
) {
  const [items, setItems] = useState<NotifyContainerAction[]>([]);
  const lastProcessedMessageId = useRef<string | null>(null);
  const { messages, isFinished, error } = useFetchStream<NotifyContainerAction>(
    {
      path: "/archive/container/notifications",
      authToken: true,
      batchSize: 20,
      refetchOnFocus: false,
    },
  );
  const wsUrl =
    authUser?.role === "ROLE_ADMIN"
      ? `${process.env.NEXT_PUBLIC_SPRING_CLIENT_WEBSOCKET}/archive/queue/batch/update`
      : null;
  const { lastMessage } = useWebSocket(wsUrl, {
    shouldReconnect: (_) => true,
    share: true,
  });

  useEffect(() => {
    if (lastMessage) {
      const parsedMessage = JSON.parse(lastMessage.data);
      if (
        isNotifyContainerAction(parsedMessage) &&
        parsedMessage?.id !== lastProcessedMessageId.current
      ) {
        lastProcessedMessageId.current = parsedMessage.id;
        setItems((prev) => [...prev, parsedMessage]);
      }
    }
  }, [lastMessage]);

  const notifications = useMemo(
    () => (messages.length === 0 ? items : messages.concat(items)),
    [items, messages],
  );
  return {
    notifications,
    isFinished,
    error,
  };
}
