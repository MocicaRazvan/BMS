"use client";
import React, {
  createContext,
  useState,
  useContext,
  ReactNode,
  useEffect,
} from "react";
import { useSearchParams } from "next/navigation";
import { useStompClient } from "react-stomp-hooks";
import { Session } from "next-auth";
import { usePathname } from "@/navigation";
import { revalidatePath } from "next/cache";

interface ChatContextType {
  activeChatId: number | null;
  setActiveChatId: (chatId: number | null) => void;
}

const ChatContext = createContext<ChatContextType | undefined>(undefined);

export const ChatProvider: React.FC<{
  children: ReactNode;
  authUser: Session["user"];
}> = ({ children, authUser }) => {
  const pathname = usePathname();
  const stompClient = useStompClient();
  const searchParams = useSearchParams();
  const chatId = searchParams.get("chatId");

  console.log("chatId context", chatId);
  console.log("searchParams context", searchParams.toString());

  const [activeChatId, setActiveChatId] = useState<number | null>(null);
  const [oldPathname, setOldPathname] = useState<string>(pathname);

  // disconnect user when leaving chat page
  useEffect(() => {
    console.log("USE CC: handleBeforeUnload called");

    if (
      pathname !== "/chat" &&
      oldPathname === "/chat" &&
      stompClient?.connected &&
      authUser?.email
    ) {
      console.log("USE CC: Disconnecting user:", authUser.email);
      stompClient.publish({
        destination: `/app/disconnectUser/${authUser.email}`,
        body: JSON.stringify({
          email: authUser.email,
        }),
      });
      setActiveChatId(null);
    }

    setOldPathname(pathname);
  }, [authUser?.email, oldPathname, pathname, stompClient?.connected]);

  return (
    <ChatContext.Provider value={{ activeChatId, setActiveChatId }}>
      {children}
    </ChatContext.Provider>
  );
};

export const useChatContext = (): ChatContextType => {
  const context = useContext(ChatContext);
  if (!context) {
    throw new Error("useChat must be used within a ChatProvider");
  }
  return context;
};
