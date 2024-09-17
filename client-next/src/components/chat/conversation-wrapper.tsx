"use client";

import {
  ChatMessageResponse,
  ConversationUserResponse,
  PageableResponse,
} from "@/types/dto";
import useFetchStream from "@/hoooks/useFetchStream";
import Conversation, {
  ConversationTexts,
} from "@/components/chat/conversation";
import LoadingSpinner from "@/components/common/loading-spinner";
import { useSession } from "next-auth/react";
import { useLocale } from "next-intl";

interface ConversationWrapperProps extends ConversationTexts {
  chatRoomId: number;
  sender: ConversationUserResponse;
  receiver: ConversationUserResponse;
}

export default function ConversationWrapper({
  chatRoomId,
  sender,
  receiver,
  ...props
}: ConversationWrapperProps) {
  const session = useSession();
  const locale = useLocale();
  const { messages, error, isFinished } = useFetchStream<
    PageableResponse<ChatMessageResponse[]>
  >({
    path: "/ws-http/messages/" + chatRoomId,
    acceptHeader: "application/json",
    authToken: true,
    method: "GET",
    queryParams: {
      offset: "0",
      limit: "10",
    },
  });

  // if (!sender) {
  //   window.location.replace(`/${locale}/chat`);
  // }

  if (isFinished && error) {
    console.error("Error fetching messages:", error);
    return (
      <div className="w-full h-full p-20 flex items-center justify-center">
        <p className="text-xl font-bold">{props.errorLoading}</p>
      </div>
    );
  }
  if (!isFinished) return <LoadingSpinner />;

  if (!session.data?.user) return null;

  console.log("convm", messages);
  console.log("convm", messages?.[0].content.toReversed());

  return (
    <div className="h-full  w-full rounded-lg border-2 ">
      <Conversation
        chatRoomId={chatRoomId}
        initialMessages={messages[0].content}
        sender={sender}
        receiver={receiver}
        initialTotalMessages={messages[0].pageInfo.totalElements}
        authUser={session.data.user}
        {...props}
      />
    </div>
  );
}
