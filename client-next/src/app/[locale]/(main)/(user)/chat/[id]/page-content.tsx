"use client";
import { WithUser } from "@/lib/user";
import { useStompClient } from "react-stomp-hooks";
import { useEffect } from "react";
import ConversationWrapper, {
  ConversationTexts,
} from "@/components/chat/conversation";

interface Props extends WithUser {
  id: string;
  conversationTexts: ConversationTexts;
}
export default function SingleChatPageContent({
  authUser,
  id,
  conversationTexts,
}: Props) {
  const stompClient = useStompClient();

  useEffect(() => {
    let mounted = true;
    if (stompClient?.connected && mounted) {
      stompClient?.publish({
        destination: "/app/changeRoom",
        body: JSON.stringify({
          chatId: id,
          userEmail: authUser.email,
        }),
      });
    }
    return () => {
      mounted = false;
    };
  }, [stompClient?.connected, authUser.email, id]);
  return (
    <div className="w-full h-full">
      <ConversationWrapper conversationTexts={conversationTexts} />
    </div>
  );
}
