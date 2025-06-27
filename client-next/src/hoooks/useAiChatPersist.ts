import { Message } from "ai/react";
import { useSession } from "next-auth/react";
import { useStompClient } from "react-stomp-hooks";
import { useCallback } from "react";
import { AiChatMessagePayload } from "@/types/dto";

export default function useAiChatPersist() {
  const session = useSession();

  const stompClient = useStompClient();

  const publishMessage = useCallback(
    (message: Message) => {
      const userEmail = session?.data?.user?.email;
      if (!stompClient?.connected || !userEmail) {
        return;
      }
      const body: AiChatMessagePayload = {
        vercelId: message.id,
        content: message.content,
        role: message.role,
        email: userEmail,
      };
      stompClient.publish({
        destination: "/app/ai-chat/addMessage",
        body: JSON.stringify(body),
      });
    },
    [session?.data?.user?.email, stompClient, stompClient?.connected],
  );

  const publishMessages = useCallback(
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
        const filteredMessages = messages.map((m) => {
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
    [
      stompClient?.connected,
      session.status,
      session?.data?.user?.email,
      stompClient,
    ],
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
  }, [
    session.status,
    session?.data?.user?.email,
    stompClient?.connected,
    stompClient,
  ]);

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
      stompClient,
    ],
  );

  return {
    deletePersistedMessages,
    publishMessages,
    deleteByVercelId,
    publishMessage,
  };
}
