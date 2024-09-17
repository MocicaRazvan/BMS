import { Message } from "ai/react";
import { useDebounce } from "@/components/ui/multiple-selector";
import { useSession } from "next-auth/react";
import { useStompClient } from "react-stomp-hooks";
import { useCallback, useEffect, useRef } from "react";
import { AiChatMessagePayload } from "@/types/dto";

export default function useAiChatPersist(
  messages: Message[],
  initialMessages: Message[],
) {
  const session = useSession();
  const prevAddedMessage = useRef<Message>();
  const debounce = useDebounce(
    messages
      .filter((m) => !initialMessages.some((im) => im.id === m.id))
      .sort((a, b) => {
        if (!a.createdAt || !b.createdAt) return 0;
        return a.createdAt.getTime() - b.createdAt.getTime();
      }),
    100,
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

      if (prevAddedMessage.current?.id === lastMessage.id) return;

      prevAddedMessage.current = lastMessage;

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
    }
  }, [
    session.status,
    session?.data?.user?.email,
    stompClient?.connected,
    debounce,
  ]);

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

  return { deletePersistedMessages };
}
