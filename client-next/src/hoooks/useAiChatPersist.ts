import { Message } from "ai/react";
import { useDebounce } from "@/components/ui/multiple-selector";
import { useSession } from "next-auth/react";
import { useStompClient } from "react-stomp-hooks";
import { useCallback, useEffect } from "react";
import { AiChatMessagePayload } from "@/types/dto";

export default function useAiChatPersist(messages: Message[]) {
  const session = useSession();
  const debounce = useDebounce(messages, 100);
  const stompClient = useStompClient();

  useEffect(() => {
    if (
      session.status === "authenticated" &&
      session?.data?.user?.email &&
      stompClient &&
      stompClient?.connected
    ) {
      const lastMessage = debounce.at(-1);
      console.log("lastMessage", lastMessage);
      if (!lastMessage) return;
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
