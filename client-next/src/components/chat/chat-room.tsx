"use client";

import { ChatRoomResponse, ConversationUserResponse } from "@/types/dto";
import { Dispatch, memo, SetStateAction, useCallback } from "react";
import { useSearchParams } from "next/navigation";
import { cn, isDeepEqual } from "@/lib/utils";
import { ScrollArea } from "@/components/ui/scroll-area";
import { useStompClient } from "react-stomp-hooks";
import { useChatNotification } from "@/context/chat-message-notification-context";
import { Button } from "@/components/ui/button";
import { Trash2 } from "lucide-react";
import DeleteChatRoomDialog from "@/components/dialogs/chat/delete-chat-room";
import { WithUser } from "@/lib/user";
import { useRouter } from "@/navigation";

export interface ChatRoomTexts {
  numberUnread: string;
}

interface BaseProps extends WithUser, ChatRoomTexts {
  activeRoom: ChatRoomResponse | null;
  handleRoomDelete: (chatRoomId: number) => Promise<void>;
}

interface ChatRoomProps extends BaseProps {
  chatRooms: ChatRoomResponse[];
  setActiveRoomId: Dispatch<SetStateAction<number | null>>;
  baseChat: () => void;
}

export const ChatRoom = memo(
  ({
    chatRooms,
    setActiveRoomId,
    authUser,
    activeRoom,
    handleRoomDelete,
    baseChat,
    numberUnread,
  }: ChatRoomProps) => {
    const router = useRouter();

    const stompClient = useStompClient();
    const { removeBySender } = useChatNotification();
    const searchParams = useSearchParams();
    useCallback(
      (otherUser: ConversationUserResponse) => {
        if (stompClient && stompClient.connected) {
          stompClient.publish({
            destination:
              "/app/chatMessageNotification/deleteAllByReceiverEmailSenderEmail",
            body: JSON.stringify({
              receiverEmail: authUser.email,
              senderEmail: otherUser.email,
            }),
          });
          removeBySender({
            stompClient,
            senderEmail: otherUser.email,
            receiverEmail: authUser.email,
          });
        }
      },
      [authUser.email, removeBySender, stompClient],
    );

    const callback = useCallback(
      (room: ChatRoomResponse, otherUser: ConversationUserResponse) => {
        const params = new URLSearchParams(searchParams.toString());
        params.set("chatId", room.id.toString());
        window.history.pushState(null, "", `?${params.toString()}`);
      },
      [searchParams],
    );

    return (
      <ScrollArea className="w-full h-[350px] min-w-[200px] md:h-[calc(1000px-4rem-105px)]  space-y-4 ">
        <div className="w-full h-full space-y-4 pr-4 pb-6 ">
          {chatRooms.map((room) => (
            <div key={room.id} className="w-full h-full ">
              <ChatRoomItem
                authUser={authUser}
                callback={callback}
                activeRoom={activeRoom}
                room={room}
                handleRoomDelete={handleRoomDelete}
                numberUnread={numberUnread}
              />
            </div>
          ))}{" "}
        </div>
      </ScrollArea>
    );
  },
  (prevProps, nextProps) =>
    isDeepEqual(prevProps.chatRooms, nextProps.chatRooms) &&
    isDeepEqual(prevProps.authUser, nextProps.authUser) &&
    isDeepEqual(prevProps.activeRoom, nextProps.activeRoom),
);

ChatRoom.displayName = "ChatRoom";

interface ChatRoomItemProps extends BaseProps {
  callback: (
    room: ChatRoomResponse,
    otherUser: ConversationUserResponse,
  ) => void;
  room: ChatRoomResponse;
}

const ChatRoomItem = memo(
  ({
    authUser,
    callback,
    activeRoom,
    room,
    handleRoomDelete,
    numberUnread,
  }: ChatRoomItemProps) => {
    const otherUser = room.users.find(({ email }) => email !== authUser.email);
    const isActive = activeRoom?.id === room.id;
    const { getByReference } = useChatNotification();
    const notifications = getByReference(room.id);

    if (!otherUser) return null;

    return (
      <div
        className={cn(
          "cursor-pointer p-3 rounded-md space-x-4 transition-all hover:bg-accent hover:text-accent-foreground hover:scale-[1.01]",
          isActive && "bg-accent text-accent-foreground hover:scale-100",
        )}
        onClick={() => {
          callback(room, otherUser);
        }}
      >
        <div className="flex flex-col items-between justify-center gap-2 min-h-11">
          <div className="flex items-center justify-between gap-2">
            <div
              className={cn(
                "w-5 h-5 rounded-full  backdrop-blur ",
                otherUser.connectedStatus === "ONLINE"
                  ? "supports-[backdrop-filter]:bg-success/75"
                  : "supports-[backdrop-filter]:bg-destructive/75",
              )}
            />
            <div className="flex flex-1 items-center justify-between">
              <p className=" font-bold">{otherUser.email}</p>
              {otherUser.connectedChatRoom?.id !== room.id && (
                <DeleteChatRoomDialog
                  receiverEmail={otherUser?.email || ""}
                  handleDelete={() => handleRoomDelete(room.id)}
                  anchor={
                    <Button
                      variant="destructive"
                      size="sm"
                      className=" transition-all hover:shadow-sm hover:scale-110"
                      onClick={(e) => e.stopPropagation()}
                    >
                      <Trash2 className="w-4 h-7" />
                    </Button>
                  }
                />
              )}
            </div>
          </div>
          <div>
            <p
              className={cn(
                "font-bold hidden",
                notifications.length > 0 && "inline",
              )}
            >
              {numberUnread}{" "}
              <span className="font-normal">{notifications.length}</span>
              {/*{notifications.length > 0 && notifications.length} unread messages*/}
            </p>
          </div>
        </div>
      </div>
    );
  },
  (prevProps, nextProps) => isDeepEqual(prevProps, nextProps),
);

ChatRoomItem.displayName = "ChatRoomItem";
