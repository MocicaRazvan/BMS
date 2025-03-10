import { Message } from "ai/react";
import { useDebounce } from "@/components/ui/multiple-selector";
import { useSession } from "next-auth/react";
import { useStompClient } from "react-stomp-hooks";
import { useCallback, useEffect, useRef } from "react";
import { AiChatMessagePayload } from "@/types/dto";

export const DEFAULT_PERSIST_INTERVAL = 500 as const;
export default function useAiChatPersist(
  messages: Message[],
  initialMessages: Message[],
  intervalMs = DEFAULT_PERSIST_INTERVAL,
) {
  const session = useSession();
  const prevAddedMessageMap = useRef<
    Record<Message["role"], Message | undefined>
  >({
    system: undefined,
    user: undefined,
    assistant: undefined,
    function: undefined,
    data: undefined,
    tool: undefined,
  });
  const messageContentRef = useRef<Map<string, string>>(new Map());
  const debounce = useDebounce(
    messages
      .filter((m) => !initialMessages.some((im) => im.id === m.id))
      .sort((a, b) => {
        if (!a.createdAt || !b.createdAt) return 0;
        return a.createdAt.getTime() - b.createdAt.getTime();
      }),
    intervalMs,
  );
  const stompClient = useStompClient();

  useEffect(() => {
    if (
      session.status === "authenticated" &&
      session?.data?.user?.email &&
      stompClient &&
      stompClient?.connected
    ) {
      const lastMessage = debounce.at(-1);
      if (!lastMessage) return;

      const previousContent = messageContentRef.current.get(lastMessage.id);
      if (
        previousContent !== undefined &&
        previousContent === lastMessage.content
      ) {
        const prevAddedMessage = prevAddedMessageMap.current[lastMessage.role];
        if (prevAddedMessage?.id === lastMessage.id) return;

        prevAddedMessageMap.current[lastMessage.role] = lastMessage;

        const body: AiChatMessagePayload = {
          vercelId: lastMessage.id,
          content: lastMessage.content,
          role: lastMessage.role,
          email: session.data.user.email,
        };

        stompClient.publish({
          destination: "/app/ai-chat/addMessage",
          body: JSON.stringify(body),
        });
        messageContentRef.current.delete(lastMessage.id);
      } else {
        messageContentRef.current.set(lastMessage.id, lastMessage.content);
      }
    }
  }, [
    session.status,
    session?.data?.user?.email,
    stompClient?.connected,
    debounce,
  ]);

  const addPersistMessages = useCallback(
    (messages: Message[]) => {
      if (
        messages &&
        messages.length > 0 &&
        session.status === "authenticated" &&
        session.data.user &&
        session?.data?.user?.email &&
        stompClient &&
        stompClient.connected
      ) {
        const email = session.data.user.email;
        const filteredMessages = messages
          .filter(
            (m) =>
              !initialMessages.some((im) => im.id === m.id) &&
              !messageContentRef.current.has(m.id),
          )
          .map((m) => {
            const payload: AiChatMessagePayload = {
              vercelId: m.id,
              content: m.content,
              role: m.role,
              email,
            };
            return payload;
          });

        stompClient.publish({
          destination: "/app/ai-chat/addMessageBulk",
          body: JSON.stringify(filteredMessages),
        });
      }
    },
    [stompClient?.connected, session.status, session?.data?.user?.email],
  );

  const deletePersistedMessages = useCallback(() => {
    if (
      session.status === "authenticated" &&
      session?.data?.user?.email &&
      stompClient &&
      stompClient?.connected
    ) {
      stompClient.publish({
        destination: `/app/ai-chat/deleteAllMessagesByUserEmail/${session.data.user.email}`,
        body: JSON.stringify({ email: session.data.user.email }),
      });
    }
  }, [session.status, session?.data?.user?.email, stompClient?.connected]);

  const deleteByVercelId = useCallback(
    (message: Message) => {
      if (
        session.status === "authenticated" &&
        session?.data?.user?.email &&
        stompClient &&
        stompClient?.connected
      ) {
        stompClient.publish({
          destination: `/app/ai-chat/deleteAllByVercelId/${message.id}/${session.data.user.email}`,
        });
      }
    },
    [
      session.status,
      stompClient?.connected,
      session.status,
      session?.data?.user?.email,
    ],
  );

  return { deletePersistedMessages, addPersistMessages, deleteByVercelId };
}
