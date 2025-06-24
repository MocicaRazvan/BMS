"use client";
import { useCurRooms } from "@/context/cur-rooms-context";
import {
  ChatMessagePayload,
  ChatMessageResponse,
  ChatRoomResponseJoined,
  JoinedConversationUser,
  PageableResponse,
} from "@/types/dto";
import useFetchStream from "@/lib/fetchers/useFetchStream";
import { Skeleton } from "@/components/ui/skeleton";
import { cn, fromDistanceToNowUtc } from "@/lib/utils";
import { memo, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { useStompClient, useSubscription } from "react-stomp-hooks";
import { Avatar, AvatarImage } from "@/components/ui/avatar";
import noImg from "@/assets/noImage.jpg";
import { Link, Locale } from "@/navigation/navigation";
import { Button } from "@/components/ui/button";
import { ArrowLeft, RefreshCw } from "lucide-react";
import Loader from "@/components/ui/spinner";
import InfiniteScroll from "react-infinite-scroll-component";
import { AnimatePresence, motion } from "framer-motion";
import { compareAsc, parseISO } from "date-fns";
import ChatMessageForm, {
  ChatMessageFormTexts,
} from "@/components/forms/chat-message-form";
import { useChatNotification } from "@/context/chat-message-notification-context";
import useSendTyping from "@/hoooks/chat/use-send-typing";
import useReceiveTyping from "@/hoooks/chat/use-receive-typing";
import TypingIndicator from "@/components/chat/typing-indicator";
import { WithUser } from "@/lib/user";

interface Props {
  conversationTexts: ConversationTexts;
  locale: Locale;
}

export default function ConversationWrapper({
  conversationTexts,
  locale,
}: Props) {
  const { curRoom, otherUser, curUser, authUser } = useCurRooms();

  if (!curRoom || !otherUser || !curUser) {
    return (
      <div className="p-2 w-full h-full ">
        <Skeleton className="w-full h-full" />
      </div>
    );
  }

  return (
    <div className="p-2 w-full h-full ">
      <Conversation
        chatRoom={curRoom}
        curUser={curUser}
        otherUser={otherUser}
        authUser={authUser}
        locale={locale}
        {...conversationTexts}
      />
    </div>
  );
}

export interface ConversationTexts {
  conversationContentPropsTexts: ConversationContentTexts;
  errorText: string;
}
interface ConversationsProps extends ConversationTexts, WithUser {
  chatRoom: ChatRoomResponseJoined;
  otherUser: JoinedConversationUser;
  curUser: JoinedConversationUser;
  locale: Locale;
}
function Conversation({
  chatRoom,
  otherUser,
  curUser,
  conversationContentPropsTexts,
  errorText,
  authUser,
  locale,
}: ConversationsProps) {
  const { messages, error, isAbsoluteFinished, isFinished, refetch } =
    useFetchStream<PageableResponse<ChatMessageResponse[]>>({
      path: "/ws-http/messages/" + chatRoom.id,
      acceptHeader: "application/json",
      authToken: true,
      method: "GET",
      queryParams: {
        offset: "0",
        limit: "10",
      },
      refetchOnFocus: false,
    });
  const stompClient = useStompClient();
  const { getByReference, removeBySender } = useChatNotification();
  const notifs = getByReference(chatRoom.id);

  useEffect(() => {
    if (notifs.length && stompClient && stompClient?.connected) {
      removeBySender({
        senderEmail: otherUser.conversationUser.email,
        receiverEmail: curUser.conversationUser.email,
        stompClient: stompClient,
      });
    }
  }, [
    stompClient?.connected,
    otherUser.conversationUser.email,
    curUser.conversationUser.email,
    notifs.length,
  ]);

  if (isFinished && error) {
    console.error("Error fetching messages:", error);
    return (
      <div className="w-full h-full p-20 flex items-center justify-center">
        <p className="text-xl font-bold">{errorText}</p>
      </div>
    );
  }
  if (!isFinished)
    return (
      <div className="p-2 w-full h-full ">
        <Skeleton className="w-full h-full" />
      </div>
    );

  const initialTotalElements = messages?.at(0)?.pageInfo.totalElements || 0;
  const initialMessages = messages?.at(0)?.content || [];

  return (
    <ConversationContentTypingWrapper
      initialMessages={initialMessages}
      initialTotalElements={initialTotalElements}
      curRoom={chatRoom}
      curUser={curUser}
      otherUser={otherUser}
      isAbsoluteFinished={isAbsoluteFinished}
      refetch={refetch}
      authUser={authUser}
      locale={locale}
      {...conversationContentPropsTexts}
    />
  );
}

export interface ConversationContentTexts {
  chatMessageFormTexts: ChatMessageFormTexts;
  sameChatText: string;
  typingText: string;
}

interface ConversationContentProps extends ConversationContentTexts, WithUser {
  initialMessages: ChatMessageResponse[];
  curRoom: ChatRoomResponseJoined;
  curUser: JoinedConversationUser;
  otherUser: JoinedConversationUser;
  initialTotalElements: number;
  isAbsoluteFinished: boolean;
  refetch: () => void;
  removeTimeoutAndTypingRoom: (roomId: number) => void;
  isOtherTyping: ChatMessagePayload;
  locale: Locale;
}

const pageSize = 10;
const SCROLL_BOTTOM_THRESHOLD = 135;

const ConversationContentTypingWrapper = (
  props: Omit<
    ConversationContentProps,
    "removeTimeoutAndTypingRoom" | "isOtherTyping"
  >,
) => {
  const { removeTimeoutAndTypingRoom, typingRooms } = useReceiveTyping({
    curUser: props.curUser,
  });
  const isOtherTyping = useMemo(
    () => typingRooms[props.curRoom.id],
    [typingRooms, props.curRoom.id],
  );
  return (
    <ConversationContent
      {...props}
      removeTimeoutAndTypingRoom={removeTimeoutAndTypingRoom}
      isOtherTyping={isOtherTyping}
    />
  );
};

const ConversationContent = memo(
  ({
    initialMessages,
    curRoom,
    otherUser,
    curUser,
    initialTotalElements,
    isAbsoluteFinished,
    chatMessageFormTexts,
    sameChatText,
    typingText,
    refetch,
    authUser,
    isOtherTyping,
    removeTimeoutAndTypingRoom,
    locale,
  }: ConversationContentProps) => {
    const [chatMessages, setChatMessages] = useState<ChatMessageResponse[]>(
      initialMessages.toReversed(),
    );
    const { onValueChange } = useSendTyping({
      otherUser,
      curUser,
      curRoom,
    });

    useEffect(() => {
      setChatMessages(initialMessages.toReversed());
    }, [initialMessages]);

    const [totalMessages, setTotalMessages] =
      useState<number>(initialTotalElements);

    useEffect(() => {
      setTotalMessages(initialTotalElements);
    }, [initialTotalElements]);

    const [scrollPosition, setScrollPosition] = useState<boolean>(false);

    const [isLoading, setIsLoading] = useState<boolean>(false);

    const [hasMore, setHasMore] = useState<boolean>(
      initialMessages.length < initialTotalElements,
    );

    useEffect(() => {
      setHasMore(chatMessages.length < totalMessages);
    }, [chatMessages.length, totalMessages]);

    const chatContainerRef = useRef<HTMLDivElement>(null);

    const areInTheSameChat = useMemo(
      () =>
        otherUser.conversationUser?.connectedChatRoom?.id &&
        curUser.conversationUser?.connectedChatRoom?.id &&
        otherUser.conversationUser?.connectedChatRoom?.id ===
          curUser.conversationUser?.connectedChatRoom?.id,
      [
        curUser.conversationUser?.connectedChatRoom?.id,
        otherUser.conversationUser?.connectedChatRoom?.id,
      ],
    );

    const handleLoadMore = useCallback(async () => {
      if (isLoading || chatMessages.length >= totalMessages) return;

      setIsLoading(true);
      setScrollPosition(false);
      const { messages, error, isFinished } = await fetchStream<
        PageableResponse<ChatMessageResponse[]>
      >({
        path: "/ws-http/messages/" + curRoom.id,
        acceptHeader: "application/json",
        token: authUser.token,
        method: "GET",
        queryParams: {
          offset: chatMessages.length.toString(),
          limit:
            totalMessages - chatMessages.length >= pageSize
              ? pageSize.toString()
              : ((totalMessages - chatMessages.length) % pageSize).toString(),
        },
      });
      try {
        if (error) {
          console.error("Error fetching messages:", error);
          return;
        }
        if (isFinished && messages.length > 0) {
          setTotalMessages(messages[0].pageInfo.totalElements);
          setChatMessages((prev) => [
            ...messages[0].content.toReversed(),
            ...prev,
          ]);
          setHasMore(
            chatMessages.length + messages[0].content.length < totalMessages,
          );
        }
      } catch (e) {
        console.log("error", e);
      } finally {
        setIsLoading(false);
      }
    }, [
      authUser.token,
      chatMessages.length,
      curRoom.id,
      isLoading,
      totalMessages,
    ]);

    useSubscription(
      `/topic/messages-${curUser.conversationUser.email}`,
      (message) => {
        const newMessage = JSON.parse(message.body) as ChatMessageResponse;
        setTotalMessages((prev) => ++prev);
        // setChatMessages((prev) => updateMessages(prev, newMessage));
        setChatMessages((prev) => [...prev, newMessage]);
        setScrollPosition(true);
        removeTimeoutAndTypingRoom(newMessage.chatRoom.id);
      },
    );

    useSubscription(
      `/topic/messages-${otherUser.conversationUser.email}`,
      (message) => {
        const newMessage = JSON.parse(message.body) as ChatMessageResponse;
        if (
          newMessage.receiver.email === otherUser.conversationUser.email &&
          newMessage.sender.email === curUser.conversationUser.email
        ) {
          setTotalMessages((prev) => ++prev);
          // setChatMessages((prev) => updateMessages(prev, newMessage));
          setChatMessages((prev) => [...prev, newMessage]);
          setScrollPosition(true);
        }
      },
    );

    useEffect(() => {
      if (isOtherTyping && chatContainerRef.current) {
        setScrollPosition(true);
      }
    }, [isOtherTyping]);

    useEffect(() => {
      if (scrollPosition && chatContainerRef.current) {
        const isNearBottom =
          Math.abs(chatContainerRef.current.scrollTop) <
          SCROLL_BOTTOM_THRESHOLD;
        if (isNearBottom) {
          chatContainerRef.current.scrollTop =
            chatContainerRef.current.scrollHeight;
        }
        setScrollPosition(false);
      }
    }, [scrollPosition]);

    return (
      <div className="w-full h-full flex flex-col scroll-smooth">
        <header className="border-b px-4 py-3 relative ">
          <AnimatePresence mode="wait">
            {!isAbsoluteFinished && (
              <motion.div
                className="absolute -bottom-11 left-0 right-0 z-[10] flex items-center justify-center "
                key="loading-isAbsoluteFinished"
                initial={{ opacity: 0, scale: 0 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0.5, scale: 0 }}
                transition={{ delay: 0.7, duration: 0.35 }}
              >
                <Loader className="p-0 m-0" />
              </motion.div>
            )}
          </AnimatePresence>
          <div className="flex-col md:flex-row flex items-center justify-between gap-3">
            <div className="flex items-center  justify-start md:justify-center gap-3">
              <Link href="/chat">
                <Button variant="ghost" size="sm">
                  <ArrowLeft className="w-5 h-5" />
                </Button>
              </Link>
              <Avatar className="w-12 h-12">
                <AvatarImage
                  src={otherUser.reactiveUser.image || noImg}
                  alt={otherUser.reactiveUser.email?.substring(0, 2)}
                />
              </Avatar>
              <div>
                <Link
                  className="font-bold hover:underline text-lg"
                  href={`/users/single/${otherUser.reactiveUser.id}`}
                >
                  {otherUser.conversationUser.email}
                </Link>
                <p
                  className={cn(
                    "text-xs font-medium",
                    otherUser.conversationUser.connectedStatus === "ONLINE"
                      ? "text-success"
                      : "text-destructive",
                  )}
                >
                  {otherUser.conversationUser.connectedStatus}
                </p>
              </div>
            </div>
            <div>
              {isOtherTyping && (
                <p className=" font-medium text-success">{typingText}</p>
              )}
            </div>
            <div className="flex items-center justify-end gap-2">
              <AnimatePresence mode="wait">
                {areInTheSameChat && (
                  <motion.div
                    initial={{ opacity: 0.5, scale: 0 }}
                    animate={{ opacity: 1, scale: 1 }}
                    exit={{ opacity: 0.5, scale: 0 }}
                    transition={{
                      duration: 0.35,
                      ease: "easeInOut",
                      delay: 0.5,
                    }}
                    className="w-full py-1.5 px-2 mx-auto border-border/40 bg-primary/95 backdrop-blur supports-[backdrop-filter]:bg-primary/20 rounded flex-1
                  flex items-center justify-end"
                    key="same-chat"
                  >
                    <p className="text-center text-sm font-bold ">
                      {sameChatText}
                    </p>
                  </motion.div>
                )}
              </AnimatePresence>
              <Button variant="ghost" size="sm" onClick={refetch}>
                <RefreshCw className="w-5 h-5" />
              </Button>
            </div>
          </div>
        </header>
        <div
          className="w-full flex-1 bg-background/95 backdrop-blur
                    supports-[backdrop-filter]:bg-background/65  relative overflow-auto flex flex-col-reverse h-[calc(100%-310px)] md:h-[calc(100%-225px)]"
          id="scrollableDiv"
          ref={chatContainerRef}
        >
          <InfiniteScroll
            dataLength={chatMessages.length}
            next={handleLoadMore}
            hasMore={hasMore}
            inverse={true}
            loader={<Loader className="mx-auto my-6 h-12 w-12" />}
            scrollableTarget="scrollableDiv"
            scrollThreshold={"200px"}
            className={cn(
              `flex flex-col-reverse h-full transition-all duration-300 relative`,
            )}
          >
            <div
              className={cn(
                "flex flex-col flex-1 overflow-hidden transition-all duration-300",
              )}
            >
              <div className="flex-1  w-full p-6 gap-6 flex-col h-full relative">
                <div className="grid gap-7">
                  {chatMessages.length > 0 &&
                    chatMessages.map((chatMessage) => (
                      <motion.div key={chatMessage.id}>
                        <ChatMessageItem
                          chatMessage={chatMessage}
                          curUser={curUser}
                          otherUser={otherUser}
                          locale={locale}
                        />
                      </motion.div>
                    ))}
                  {isOtherTyping && (
                    <TypingIndicator user={otherUser.reactiveUser} />
                  )}
                </div>
              </div>
            </div>
          </InfiniteScroll>
        </div>
        <div className=" h-[310px] md:h-[225px]">
          <ChatMessageForm
            chatRoomId={curRoom.id}
            sender={curUser.conversationUser}
            receiver={otherUser.conversationUser}
            {...chatMessageFormTexts}
            onValueChange={onValueChange}
            wrapperClassName="h-full"
          />
        </div>
      </div>
    );
  },
);
ConversationContent.displayName = "ConversationContent";

interface ChatMessageProps {
  chatMessage: ChatMessageResponse;
  curUser: JoinedConversationUser;
  otherUser: JoinedConversationUser;
  locale: Locale;
}

const ChatMessageItem = ({
  chatMessage,
  curUser,
  otherUser,
  locale,
}: ChatMessageProps) => {
  const isSender = chatMessage.sender?.email === curUser.conversationUser.email;
  const formatDate = useCallback(
    (d: string) =>
      fromDistanceToNowUtc(
        parseISO(d || ""),
        Intl.DateTimeFormat().resolvedOptions().timeZone,
        locale as Locale,
      ),
    [locale],
  );
  const [chatTimestamp, setChatTimestamp] = useState<string>(
    formatDate(chatMessage.timestamp),
  );

  useEffect(() => {
    const interval = setInterval(() => {
      setChatTimestamp(formatDate(chatMessage.timestamp));
    }, 20_000);
    return () => clearInterval(interval);
  }, [chatMessage.timestamp, formatDate]);

  return isSender ? (
    <div className="w-full flex ml-auto justify-end gap-2.5 items-end">
      <div className="flex flex-col ml-auto w-full justify-end gap-3 items-end">
        <div className="rounded-lg bg-muted px-2.5 py-0.5 max-w-[95%] md:max-w-[85%] backdrop-blur shadow shadow-shadow_color">
          <div
            className="prose w-full [&_ol]:list-decimal [&_ul]:list-disc dark:prose-invert text-wrap "
            dangerouslySetInnerHTML={{ __html: chatMessage.content ?? "" }}
          />
        </div>
        <p className="text-xs font-semibold text-muted-foreground">
          {chatTimestamp}
        </p>
      </div>
      <Avatar className="w-12 h-12">
        <AvatarImage
          src={curUser.reactiveUser.image || noImg}
          alt={curUser.reactiveUser.email?.substring(0, 2)}
        />
      </Avatar>
    </div>
  ) : (
    <div className="flex gap-2.5 items-end ">
      <Avatar className="w-12 h-12">
        <AvatarImage
          src={otherUser.reactiveUser.image || noImg}
          alt={otherUser.reactiveUser.email?.substring(0, 2)}
        />
      </Avatar>
      <div className="flex flex-col w-full justify-start gap-3 items-start">
        <div className="rounded-lg bg-primary/85 px-2.5 py-0.5 max-w-[95%] md:max-w-[85%] backdrop-blur shadow shadow-shadow_color">
          <div
            className="prose  w-full [&_ol]:list-decimal [&_ul]:list-disc dark:prose-invert text-wrap text-primary-foreground"
            dangerouslySetInnerHTML={{ __html: chatMessage.content ?? "" }}
          />
        </div>
        <p className="text-xs font-semibold">{chatTimestamp}</p>
      </div>
    </div>
  );
};

ChatMessageItem.displayName = "ChatMessageItem";

const updateMessages = (
  messages: ChatMessageResponse[],
  newMessage: ChatMessageResponse,
): ChatMessageResponse[] => {
  if (!newMessage.timestamp) {
    console.error("New message has invalid timestamp:", newMessage);
    return messages;
  }

  const validMessages = messages.filter((m) => m.timestamp);
  if (validMessages.length !== messages.length) {
    console.warn("Some messages have invalid timestamps and were filtered out");
  }

  return [...validMessages, newMessage].sort((a, b) => {
    const dateA = parseISO(a.timestamp);
    const dateB = parseISO(b.timestamp);
    if (!dateA || !dateB) {
      console.error("Invalid date encountered during sorting:", a, b);
    }
    return compareAsc(dateA, dateB);
  });
};
const useThrottleWithImmediateSend = (
  value: string,
  delay: number,
  callback: (val: string) => void,
) => {
  const lastSentRef = useRef<number>(0);

  useEffect(() => {
    const now = Date.now();

    if (now - lastSentRef.current > delay) {
      callback(value);
      lastSentRef.current = now;
    }
  }, [value, delay, callback]);
};
