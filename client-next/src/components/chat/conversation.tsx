"use client";

import {
  ChatMessageResponse,
  ConversationUserBase,
  ConversationUserResponse,
  PageableResponse,
} from "@/types/dto";
import { memo, useCallback, useEffect, useRef, useState } from "react";
import { formatFromUtc, isDeepEqual } from "@/lib/utils";
import { useStompClient, useSubscription } from "react-stomp-hooks";
import { compareAsc, parseISO } from "date-fns";
import { format } from "date-fns";
import { toZonedTime, getTimezoneOffset } from "date-fns-tz";

import ChatMessageForm, {
  ChatMessageFormTexts,
} from "@/components/forms/chat-message-form";
import { fetchStream } from "@/hoooks/fetchStream";
import { WithUser } from "@/lib/user";
import Loader from "@/components/ui/spinner";
import InfiniteScroll from "react-infinite-scroll-component";

export interface ConversationTexts {
  chatMessageFormTexts: ChatMessageFormTexts;
  userInTheSameChat: string;
  loadMore: string;
  loadMoreLoading: string;
  errorLoading: string;
}

interface ConversationProps extends WithUser, ConversationTexts {
  sender: ConversationUserResponse;
  receiver: ConversationUserResponse;
  initialMessages: ChatMessageResponse[];
  initialTotalMessages: number;
  chatRoomId: number;
}

const pageSize = 10;

export default function Conversation({
  initialMessages,
  sender,
  receiver,
  chatRoomId,
  initialTotalMessages,
  authUser,
  userInTheSameChat,
  chatMessageFormTexts,
  loadMoreLoading,
  loadMore,
}: ConversationProps) {
  // console.log("initialMessages", initialMessages);
  const [chatMessages, setChatMessages] = useState<ChatMessageResponse[]>(
    initialMessages.toReversed(),
  );
  const [totalMessages, setTotalMessages] =
    useState<number>(initialTotalMessages);
  const [scrollPosition, setScrollPosition] = useState<boolean>(true);

  const [isLoading, setIsLoading] = useState<boolean>(false);

  const [hasMore, setHasMore] = useState<boolean>(
    initialMessages.length < initialTotalMessages,
  );
  //
  console.log("HAS MORE", hasMore);
  console.log("HAS MORE chatMessages", chatMessages.length);
  // console.log("HAS MORE initialMessages", initialMessages.length);
  // console.log("HAS MORE initialTotalMessages", initialTotalMessages);

  // console.log("chatm leng", chatMessages.length);
  const chatContainerRef = useRef<HTMLDivElement>(null);
  const loaderRef = useRef<HTMLDivElement>(null);
  const observerRef = useRef<IntersectionObserver | null>(null);

  const areInTheSameChat =
    sender?.connectedChatRoom?.id &&
    receiver?.connectedChatRoom?.id &&
    sender?.connectedChatRoom?.id === receiver?.connectedChatRoom?.id;

  // console.log("Same chat", areInTheSameChat);
  // console.log("Same chat rec", receiver.connectedChatRoom?.id);
  // console.log("Same chat send", sender.connectedChatRoom?.id);
  // console.log("Same chat send email", sender.email);

  // todo remove e useless
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
      console.warn(
        "Some messages have invalid timestamps and were filtered out",
      );
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

  const handleLoadMore = useCallback(async () => {
    console.log("handleLoadMore");
    if (isLoading || chatMessages.length >= totalMessages) return;

    setIsLoading(true);
    setScrollPosition(false);
    const { messages, error, isFinished } = await fetchStream<
      PageableResponse<ChatMessageResponse[]>
    >({
      path: "/ws-http/messages/" + chatRoomId,
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
    chatRoomId,
    isLoading,
    totalMessages,
  ]);

  // useSubscription(`/user/${sender.email}/queue/messages`, (message) => {
  //   const newMessage = JSON.parse(message.body);
  //   console.log("sender queue", newMessage);
  //   setTotalMessages((prev) => ++prev);
  //   setChatMessages((prev) => updateMessages(prev, newMessage));
  //   setScrollPosition(true);
  // });
  // useSubscription(`/user/${receiver.email}/queue/messages`, (message) => {
  //   const newMessage = JSON.parse(message.body) as ChatMessageResponse;
  //   console.log("receiver queue", newMessage);
  //   console.log("rec email", receiver.email);
  //   console.log("sender email", sender.email);
  //   console.log("newmsg rec", newMessage.receiver.email);
  //   console.log("newmsg send", newMessage.sender.email);
  //   if (
  //     newMessage.receiver.email === receiver.email &&
  //     newMessage.sender.email === sender.email
  //   ) {
  //     setTotalMessages((prev) => ++prev);
  //     setChatMessages((prev) => updateMessages(prev, newMessage));
  //     setScrollPosition(true);
  //   }
  // });
  useSubscription(`/topic/messages-${sender.email}`, (message) => {
    const newMessage = JSON.parse(message.body);
    console.log("sender queue", newMessage);
    setTotalMessages((prev) => ++prev);
    setChatMessages((prev) => updateMessages(prev, newMessage));
    setScrollPosition(true);
  });
  // useSubscription(`/user/${receiver.email}/queue/messages`, (message) => {
  //   const newMessage = JSON.parse(message.body) as ChatMessageResponse;
  //   console.log("receiver queue", newMessage);
  //   console.log("rec email", receiver.email);
  //   console.log("sender email", sender.email);
  //   console.log("newmsg rec", newMessage.receiver.email);
  //   console.log("newmsg send", newMessage.sender.email);
  //   if (
  //     newMessage.receiver.email === receiver.email &&
  //     newMessage.sender.email === sender.email
  //   ) {
  //     setTotalMessages((prev) => ++prev);
  //     setChatMessages((prev) => updateMessages(prev, newMessage));
  //     setScrollPosition(true);
  //   }
  // });
  useSubscription(`/topic/messages-${receiver.email}`, (message) => {
    const newMessage = JSON.parse(message.body) as ChatMessageResponse;
    console.log("receiver queue", newMessage);
    console.log("rec email", receiver.email);
    console.log("sender email", sender.email);
    console.log("newmsg rec", newMessage.receiver.email);
    console.log("newmsg send", newMessage.sender.email);
    if (
      newMessage.receiver.email === receiver.email &&
      newMessage.sender.email === sender.email
    ) {
      setTotalMessages((prev) => ++prev);
      setChatMessages((prev) => updateMessages(prev, newMessage));
      setScrollPosition(true);
    }
  });

  useEffect(() => {
    if (scrollPosition && chatContainerRef.current) {
      chatContainerRef.current.scrollTop =
        chatContainerRef.current.scrollHeight;
      setScrollPosition(false);
    }
  }, [scrollPosition]);

  return (
    <div className="w-full h-full p-2 relative">
      {areInTheSameChat && (
        <div className=" absolute top-0 right-0 bg-opacity-60 z-[1] w-full ">
          <div
            className="w-1/3 py-3 mx-auto border-border/40 bg-background/95 backdrop-blur
         supports-[backdrop-filter]:bg-background/60 rounded "
          >
            <p className="text-center text-sm font-bold">{userInTheSameChat}</p>
          </div>
        </div>
      )}{" "}
      <div
        id="scrollableDiv"
        className="w-full h-[600px] relative overflow-auto flex flex-col-reverse"
        // viewportRef={chatContainerRef}
        ref={chatContainerRef}
      >
        <InfiniteScroll
          dataLength={chatMessages.length}
          next={handleLoadMore}
          hasMore={hasMore}
          inverse={true}
          loader={<Loader className="mx-auto my-6 h-12 w-12" />}
          scrollableTarget="scrollableDiv"
          className="flex flex-col-reverse"
        >
          <div className="flex flex-col h-full ">
            <div className="flex-1 grid w-full p-6 gap-6 flex-col ">
              <div className="grid gap-2">
                {chatMessages.length > 0 &&
                  chatMessages.map((chatMessage) => (
                    <div key={chatMessage.id}>
                      <ChatMessageItem
                        chatMessage={chatMessage}
                        sender={sender}
                        receiver={receiver}
                      />
                    </div>
                  ))}
                {/*{totalMessages} {chatMessages.length}*/}
              </div>
              {/*{chatMessages.length < totalMessages && (*/}
              {/*  <Button*/}
              {/*    onClick={() => handleLoadMore()}*/}
              {/*    className="absolute top-0 right-0 z-10 bg-success hover:bg-muted hover:text-muted-foreground"*/}
              {/*  >*/}
              {/*    {isLoading ? loadMoreLoading : loadMore}*/}
              {/*  </Button>*/}
              {/*)}*/}
            </div>
          </div>
        </InfiniteScroll>
      </div>
      <div className="flex-1 ">
        <ChatMessageForm
          chatRoomId={chatRoomId}
          sender={sender}
          receiver={receiver}
          {...chatMessageFormTexts}
        />
      </div>
    </div>
  );
}

interface ChatMessageProps {
  chatMessage: ChatMessageResponse;
  sender: ConversationUserBase;
  receiver: ConversationUserBase;
}

const ChatMessageItem = memo(
  ({ chatMessage, sender, receiver }: ChatMessageProps) => {
    const isSender = chatMessage.sender?.email === sender.email;
    const formatDate = (d: string) => {
      const utcDate = parseISO(d);
      const userTimeZone = Intl.DateTimeFormat().resolvedOptions().timeZone;
      const adjustedDate = formatFromUtc(utcDate, userTimeZone);
      return format(adjustedDate, "dd-MM-yy HH:mm:ss");
    };
    return isSender ? (
      <div className="flex flex-row-reverse gap-2 items-end">
        <div className="rounded-lg bg-gray-100 dark:bg-gray-900 p-4 max-w-[75%] backdrop-blur">
          <p className="text-sm text-gray-500 dark:text-gray-400">
            {formatDate(chatMessage.timestamp)}
          </p>
          {/*<p className="text-sm font-medium">You, {sender.email}</p>*/}
          <div
            className="prose max-w-none [&_ol]:list-decimal [&_ul]:list-disc dark:prose-invert text-wrap"
            dangerouslySetInnerHTML={{ __html: chatMessage.content ?? "" }}
          />
        </div>
        {/*<img*/}
        {/*  alt="Avatar"*/}
        {/*  className="w-10 h-10 rounded-full border"*/}
        {/*  height="40"*/}
        {/*  src="/placeholder.svg"*/}
        {/*  style={{*/}
        {/*    aspectRatio: "40/40",*/}
        {/*    objectFit: "cover",*/}
        {/*  }}*/}
        {/*  width="40"*/}
        {/*/>*/}
      </div>
    ) : (
      <div className="flex gap-2 items-end ">
        {/*<img*/}
        {/*  alt="Avatar"*/}
        {/*  className="w-10 h-10 rounded-full border"*/}
        {/*  height="40"*/}
        {/*  src="/placeholder.svg"*/}
        {/*  style={{*/}
        {/*    aspectRatio: "40/40",*/}
        {/*    objectFit: "cover",*/}
        {/*  }}*/}
        {/*  width="40"*/}
        {/*/>*/}
        <div className="rounded-lg bg-gray-600 dark:bg-white p-4 max-w-[75%] backdrop-blur dark:text-black text-white ">
          <p className="text-sm text-gray-100 dark:text-gray-950 ">
            {formatDate(chatMessage.timestamp)}
          </p>
          {/*<p className="text-sm font-medium">Other {receiver.email}</p>*/}
          <div
            className="prose max-w-none [&_ol]:list-decimal [&_ul]:list-disc dark:prose-invert text-wrap text-white dark:text-black"
            dangerouslySetInnerHTML={{ __html: chatMessage.content ?? "" }}
          />
        </div>
      </div>
    );
  },
  (prev, next) => isDeepEqual(prev, next),
);

ChatMessageItem.displayName = "ChatMessageItem";
