"use client";
import { motion } from "framer-motion";
import { cn } from "@/lib/utils";
import { useEffect, useRef, useState } from "react";
import { Bot, Minus, SendHorizontal, StopCircle, Trash } from "lucide-react";
import { Message, useChat } from "ai/react";
import ReactMarkdown from "react-markdown";
import { Link } from "@/navigation";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import ShineBorder from "@/components/magicui/shine-border";
import Logo from "@/components/logo/logo";
import { ChatScrollAnchor } from "@/components/ai-chat/chat-scroll-anchor";
import useAiChatPersist from "@/hoooks/useAiChatPersist";

export interface AiChatBoxTexts {
  loadingContent: string;
  errorContent: string;
  emptyHeader: string;
  emptyContent: string;
  inputPlaceholder: string;
}

interface Props extends AiChatBoxTexts {
  initialMessages: Message[];
}
export default function AiChatBox({
  emptyHeader,
  inputPlaceholder,
  loadingContent,
  errorContent,
  emptyContent,
  initialMessages,
}: Props) {
  const [isOpen, setIsOpen] = useState(false);
  const [showBot, setShowBot] = useState(true);
  const {
    messages,
    input,
    handleInputChange,
    handleSubmit,
    isLoading,
    error,
    stop,
    setMessages,
  } = useChat({
    api: "/api/chat",
    initialMessages,
  });
  const [isAtBottom, setIsAtBottom] = useState<boolean>(false);
  const inputRef = useRef<HTMLInputElement>(null);
  const scrollRef = useRef<HTMLDivElement>(null);
  const { deletePersistedMessages } = useAiChatPersist(
    messages,
    initialMessages,
  );

  const handleScroll = () => {
    if (!scrollRef.current) return;

    const { scrollTop, scrollHeight, clientHeight } = scrollRef.current;
    const atBottom = scrollHeight - clientHeight <= scrollTop + 15;

    setIsAtBottom(atBottom);
  };

  useEffect(() => {
    if (isLoading) {
      if (!scrollRef.current) return;

      const scrollAreaElement = scrollRef.current;

      scrollAreaElement.scrollTop =
        scrollAreaElement.scrollHeight - scrollAreaElement.clientHeight;

      setIsAtBottom(true);
    }
  }, [isLoading]);

  useEffect(() => {
    if (isOpen) {
      inputRef.current?.focus();
    }
    if (scrollRef.current) {
      scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }
  }, [isOpen]);

  const lastMessageIsUser = messages[messages.length - 1]?.role === "user";

  return (
    <aside className="z-20 fixed bottom-6 right-4 ">
      <motion.div
        className={cn(
          `w-20 h-20 bg-background shadow-lg cursor-pointer flex items-center justify-center`,
          isOpen && " h-[34rem] cursor-default w-[85vw] max-w-xl border-2",
          !isOpen
            ? "bg-background"
            : "bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/75",
        )}
        layout
        animate={{
          borderRadius: isOpen ? 15 : 50,
        }}
        initial={{ borderRadius: 50 }}
        whileHover={{ scale: isOpen ? 1 : 1.1 }}
        onClick={() => {
          if (!isOpen) {
            setIsOpen(true);
            setShowBot(false);
            if (scrollRef.current) {
              scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
            }
          }
        }}
        onAnimationComplete={() => {
          if (!isOpen) {
            setShowBot(true);
          }
        }}
      >
        {showBot && <Bot size={36} />}
        {isOpen && (
          <div className="w-full h-full flex flex-col items-center justify-center gap-2">
            <div className="p-5 border-b flex items-center justify-between w-full px-10">
              <div className="flex items-center justify-center gap-2 ">
                <Bot size={30} />
                <h2 className="text-xl font-semibold tracking-tighter">
                  {"Shaormel"}
                </h2>
              </div>
              <Button
                type="button"
                size="icon"
                variant="ghost"
                className="fllex w-10 flex-none items-center justify-center"
              >
                <Minus size={30} onClick={() => setIsOpen(false)} />
              </Button>
            </div>
            <div
              className="h-full w-full overflow-y-auto px-5 overflow-x-hidden "
              onScroll={(e) => {
                handleScroll();
                e.stopPropagation();
              }}
              ref={scrollRef}
            >
              {messages.map((messages) => (
                <ChatMessage message={messages} key={messages.id} />
              ))}
              {isLoading && lastMessageIsUser && (
                <ChatMessage
                  message={{
                    id: "loading",
                    role: "assistant",
                    content: loadingContent,
                  }}
                />
              )}
              {error && (
                <ChatMessage
                  message={{
                    id: "error",
                    role: "assistant",
                    content: errorContent,
                  }}
                />
              )}

              {!error && messages.length === 0 && (
                <ShineBorder
                  className="flex w-full h-full flex-col items-center justify-center gap-3 text-center p-10 bg-background/95"
                  color={["#A07CFE", "#FE8FB5", "#FFBE7B"]}
                  borderWidth={2}
                >
                  <div className="hidden md:block">
                    <Logo width={60} height={60} />
                  </div>
                  <div className="block md:hidden">
                    <Logo width={40} height={40} />
                  </div>
                  <p className="text-xl font-semibold tracking-tight">
                    {emptyHeader}
                  </p>
                  <p className={"font-medium"}>{emptyContent}</p>
                </ShineBorder>
              )}
              <ChatScrollAnchor
                trackVisibility={isLoading}
                isAtBottom={isAtBottom}
                scrollAreaRef={scrollRef}
              />
            </div>
            <form
              onSubmit={(e) => {
                stop();
                handleSubmit(e);
              }}
              className="m-3 flex gap-1 w-full p-1"
            >
              <Button
                type="button"
                size="icon"
                variant="ghost"
                className="fllex w-10 flex-none items-center justify-center"
                onClick={() => {
                  deletePersistedMessages();
                  setMessages([]);
                }}
              >
                <Trash size={24} />
              </Button>
              <Button
                type="button"
                size="icon"
                variant="ghost"
                className="fllex w-10 flex-none items-center justify-center"
                onClick={() => stop()}
              >
                <StopCircle size={24} />
              </Button>
              <Input
                ref={inputRef}
                value={input}
                onChange={handleInputChange}
                placeholder={inputPlaceholder}
                className="grow mx-1"
              />
              <Button
                type="submit"
                size="icon"
                className="flex w-10 flex-none items-center justify-center disabled:opacity-50"
                disabled={input.length === 0}
              >
                <SendHorizontal size={24} />
              </Button>
            </form>
          </div>
        )}
      </motion.div>
    </aside>
  );
}

interface ChatMessageProps {
  message: Message;
}

function ChatMessage({ message: { role, content } }: ChatMessageProps) {
  const appUrl = process.env.NEXTAUTH_URL!;

  const isAiMessage = role === "assistant";

  return (
    <div
      className={cn(
        "mb-3 flex items-start overflow-y-auto",
        isAiMessage ? "me-10 justify-start" : "ms-10 justify-end",
      )}
    >
      {isAiMessage && (
        <div className="items-start me-2 flex h-full flex-none flex-col">
          <Bot />
        </div>
      )}
      <div
        className={cn(
          "rounded-md border px-3 py-2",
          isAiMessage ? "bg-background" : "bg-foreground text-background",
        )}
      >
        <ReactMarkdown
          components={{
            a: ({ node, ref, ...props }) => (
              // todo see if its internal link else open in new tab
              <Link
                {...props}
                href={props.href ?? ""}
                className="text-primary hover:underline"
                target={props.href?.startsWith(appUrl) ? "_self" : "_blank"}
              >
                {props.children}
              </Link>
            ),
            p: ({ node, ...props }) => (
              <p {...props} className="mt-3 first:mt-0" />
            ),
            ul: ({ node, ...props }) => (
              <ul
                {...props}
                className="mt-3 list-inside list-disc first:mt-0"
              />
            ),
            li: ({ node, ...props }) => <li {...props} className="mt-1" />,
          }}
        >
          {content}
        </ReactMarkdown>
      </div>
    </div>
  );
}
