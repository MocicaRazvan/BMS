"use client";

import { useSession } from "next-auth/react";
import { Message } from "ai/react";
import { useEffect, useState } from "react";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { AiChatMessageResponse } from "@/types/dto";
import AiChatBox, { AiChatBoxTexts } from "@/components/ai-chat/ai-chat-box";
import { motion } from "framer-motion";

export function AiChatBoxWrapper(props: AiChatBoxTexts) {
  const session = useSession();
  const [initialMessages, setInitialMessages] = useState<Message[]>([]);
  const [isFinished, setIsFinished] = useState(false);

  useEffect(() => {
    if (
      session.status === "authenticated" &&
      session?.data?.user?.token &&
      session?.data?.user?.email
    ) {
      const abortController = new AbortController();
      fetchStream<AiChatMessageResponse[]>({
        path: `/ws-http/ai-chat/${session.data.user.email}`,
        method: "GET",
        acceptHeader: "application/json",
        token: session.data.user.token,
        aboveController: abortController,
      })
        .then(({ messages, isFinished, error }) => {
          if (abortController.signal.aborted) {
            return;
          }
          if (error) {
            setIsFinished(true);
            return;
          }
          if (messages[0]?.length > 0) {
            setInitialMessages(
              messages[0].map((m) => ({
                id: m.vercelId,
                content: m.content,
                role: m.role,
                createdAt: new Date(m.createdAt),
              })),
            );
          }
          setIsFinished(isFinished);
        })
        .finally(() => {
          if (abortController.signal.aborted) {
            return;
          }
          setIsFinished(true);
        });
      return () => {
        abortController.abort();
      };
    } else if (!(session.status === "loading")) {
      setIsFinished(true);
    }
  }, [session.status, session?.data?.user?.token, session?.data?.user?.email]);

  if (!isFinished) {
    return null;
  }
  return (
    <motion.div
      initial={{ opacity: 0, scale: 0 }}
      animate={{ opacity: 1, scale: 1 }}
      transition={{
        duration: 0.5,
        type: "tween",
        ease: "easeInOut",
        delay: 0.5,
      }}
    >
      <AiChatBox {...props} initialMessages={initialMessages} />
    </motion.div>
  );
}
