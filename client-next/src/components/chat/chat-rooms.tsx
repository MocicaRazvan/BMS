"use client";

import { useMemo, useRef } from "react";
import { ChatRoomResponseJoined, JoinedConversationUser } from "@/types/dto";
import { useRouter } from "@/navigation";
import { cn, truncate } from "@/lib/utils";
import SearchInput from "@/components/forms/input-serach";
import {
  DataTablePagination,
  DataTablePaginationTexts,
} from "@/components/table/data-table-pagination";
import { Skeleton } from "@/components/ui/skeleton";
import { useChatNotification } from "@/context/chat-message-notification-context";
import { Button } from "@/components/ui/button";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import DeleteChatRoomDialog from "@/components/dialogs/chat/delete-chat-room";
import { AnimatePresence, motion } from "framer-motion";
import { Trash2 } from "lucide-react";
import { useCurRooms } from "@/context/cur-rooms-context";
import { Avatar, AvatarImage } from "@/components/ui/avatar";
import noImg from "../../../public/noImage.jpg";
import { DeleteDialogTexts } from "@/texts/components/dialog";
import useReceiveTyping from "@/hoooks/chat/use-receive-typing";

export interface ChatRoomsTexts {
  chatRoomContentTexts: ChatRoomContentTexts;
  dataTablePaginationTexts: DataTablePaginationTexts;
  headerText: string;
  errorText: string;
  searchPlaceholder: string;
}

interface Props extends ChatRoomsTexts {}
export default function ChatRooms({
  chatRoomContentTexts,
  dataTablePaginationTexts,
  headerText,
  errorText,
  searchPlaceholder,
}: Props) {
  const {
    isFinished,
    error,
    messages,
    filterEmail,
    handleClearSearch,
    handleChangeSearch,
    pageInfo,
    setPageInfo,
  } = useCurRooms();
  const router = useRouter();
  if (isFinished && error) {
    return (
      <div className="w-full h-full p-20 flex items-center justify-center">
        <p className="text-xl font-bold">{errorText}</p>
      </div>
    );
  }
  return (
    <div className="flex flex-col items-center justify-between gap-10 flex-1 h-full">
      <div className="pt-2 border-b">
        <Button
          variant="link"
          onClick={() => {
            router.push("/chat");
          }}
        >
          <h1 className="text-xl font-bold">{headerText}</h1>
        </Button>
      </div>

      <div
        className={cn(
          "w-full flex items-center md:items-start justify-center ",
          // pageInfo.totalElements === 0 && "hidden",
        )}
      >
        <SearchInput
          value={filterEmail}
          searchInputTexts={{ placeholder: searchPlaceholder }}
          onClear={handleClearSearch}
          onChange={handleChangeSearch}
        />
      </div>
      {!isFinished && <Skeleton className="w-full h-full " />}
      {messages.length > 0 && (
        <div className="flex-1 w-full ">
          <ChatRoomsContent {...chatRoomContentTexts} />
        </div>
      )}

      <div
        className={cn(
          "w-full flex items-center md:items-start justify-center ",
          // pageInfo.totalElements === 0 && "hidden",
        )}
      >
        <DataTablePagination
          pageInfo={pageInfo}
          setPageInfo={setPageInfo}
          sizeOptions={[1, 15, 30, 50]}
          col
          {...dataTablePaginationTexts}
        />
      </div>
    </div>
  );
}

export interface ChatRoomContentTexts {
  chatRoomItemTexts: ChatRoomItemTexts;
  noRoomsTexts: string;
}
interface ChatRoomContentProps extends ChatRoomContentTexts {}
export function ChatRoomsContent({
  noRoomsTexts,
  chatRoomItemTexts,
}: ChatRoomContentProps) {
  const { chatRooms } = useCurRooms();
  if (chatRooms.length === 0) {
    return (
      <div className="w-full h-full p-20 flex items-center justify-center">
        <p className="text-xl font-bold tracking-tight text-center">
          {noRoomsTexts}
        </p>
      </div>
    );
  }
  return (
    <div className="space-y-3 w-full ">
      {chatRooms.map((room) => (
        <div key={room.id} className="w-full h-full">
          <ChatRoomItemWrapper chatRoom={room} {...chatRoomItemTexts} />
        </div>
      ))}
    </div>
  );
}

interface ChatRoomItemWrapperProps
  extends Omit<ChatRoomItemProps, "otherUser" | "stompClient" | "curUser"> {}

function ChatRoomItemWrapper(props: ChatRoomItemWrapperProps) {
  const { authUser } = useCurRooms();

  const otherUser = useMemo(
    () =>
      props.chatRoom.users.find(
        ({ conversationUser: { email } }) => email !== authUser.email,
      ),
    [props.chatRoom.users, authUser.email],
  );
  const curUser = useMemo(
    () =>
      props.chatRoom.users.find(
        ({ conversationUser: { email } }) => email === authUser.email,
      ),
    [props.chatRoom.users, authUser.email],
  );

  if (!otherUser || !curUser) return null;

  return <ChatRoomItem {...props} otherUser={otherUser} curUser={curUser} />;
}

export interface ChatRoomItemTexts {
  deleteChatDialogTexts: DeleteDialogTexts;
  unreadMessagesText: string;
  typingText: string;
}
interface ChatRoomItemProps extends ChatRoomItemTexts {
  chatRoom: ChatRoomResponseJoined;
  otherUser: JoinedConversationUser;
  curUser: JoinedConversationUser;
}

const MOUSE_HOVER_TIMEOUT = 300 as const;
export function ChatRoomItem({
  chatRoom,
  deleteChatDialogTexts,
  unreadMessagesText,
  otherUser,
  curUser,
  typingText,
}: ChatRoomItemProps) {
  const { handleRoomDelete, handleRoomChange } = useCurRooms();
  const { getByReference } = useChatNotification();
  const notifications = getByReference(chatRoom.id);
  const { curRoom, handleRoomHover } = useCurRooms();
  const { typingRooms } = useReceiveTyping({
    curUser,
  });
  const hoverTimerRef = useRef<NodeJS.Timeout | null>(null);

  const handleMouseEnter = () => {
    if (hoverTimerRef.current) {
      clearTimeout(hoverTimerRef.current);
    }

    hoverTimerRef.current = setTimeout(() => {
      console.log("hoverTimer", hoverTimerRef.current);
      handleRoomHover(chatRoom);
      hoverTimerRef.current = null;
    }, MOUSE_HOVER_TIMEOUT);
  };

  const handleMouseLeave = () => {
    if (hoverTimerRef.current) {
      clearTimeout(hoverTimerRef.current);
      hoverTimerRef.current = null;
    }
  };

  const isTypingRoomAndNotCurrent =
    typingRooms[chatRoom.id] && chatRoom.id !== curRoom?.id;

  return (
    <div
      className={cn(
        "hover:bg-muted hover:scale-[1.02] rounded-md transition-[background-color,transform] duration-200 w-full hover:no-underline px-1 py-2",
        curRoom?.id === chatRoom.id && "bg-muted",
        isTypingRoomAndNotCurrent && "outline outline-success outline-1",
      )}
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      <Button
        variant="link"
        className={`flex items-center gap-3 p-2 rounded-md  w-full hover:no-underline`}
        onClick={() => {
          handleRoomChange(chatRoom, otherUser);
        }}
      >
        <div className="relative">
          <Avatar className="w-10 h-10">
            <AvatarImage
              src={otherUser?.reactiveUser.image || noImg}
              alt={otherUser?.reactiveUser.email?.substring(0, 2)}
            />
          </Avatar>
          {otherUser?.conversationUser.connectedStatus === "ONLINE" && (
            <span className="absolute bottom-0 right-0 h-3 w-3 rounded-full bg-success border-2 border-background" />
          )}
        </div>
        <div className="flex-1 min-w-0 flex items-center justify-between">
          <div className="flex-1">
            <div className="flex items-center justify-start">
              {otherUser.conversationUser.email.length < 30 ? (
                <p className=" font-bold">{otherUser.conversationUser.email}</p>
              ) : (
                <TooltipProvider delayDuration={1000} skipDelayDuration={500}>
                  <Tooltip>
                    <TooltipTrigger asChild>
                      <p className=" font-bold">
                        {truncate(otherUser.conversationUser.email, 30)}
                      </p>
                    </TooltipTrigger>
                    <TooltipContent
                      className="z-10"
                      side="top"
                      onClick={(e) => e.stopPropagation()}
                    >
                      <h3 className="font-medium truncate">
                        {otherUser.conversationUser.email}
                      </h3>
                    </TooltipContent>
                  </Tooltip>
                </TooltipProvider>
              )}
            </div>
            <div className="flex items-center justify-between w-full">
              {notifications.length > 0 && (
                <span className="text-sm text-amber font-medium">
                  {`${notifications.length} ${unreadMessagesText}`}
                </span>
              )}
              {isTypingRoomAndNotCurrent && (
                <span className="text-sm text-success ml-auto font-medium">
                  {typingText}
                </span>
              )}
            </div>
          </div>
          <AnimatePresence mode="wait">
            {otherUser.conversationUser.connectedChatRoom?.id !==
              chatRoom.id && (
              <DeleteChatRoomDialog
                deleteChatDialogTexts={deleteChatDialogTexts}
                receiverEmail={otherUser?.conversationUser.email || ""}
                handleDelete={async () => {
                  await handleRoomDelete(chatRoom, otherUser);
                }}
                anchor={
                  <motion.div
                    key={`delete-${chatRoom.id}-${otherUser.conversationUser.email}`}
                    className="z-10"
                    initial={{ scale: 0 }}
                    animate={{
                      scale: 1,
                      transition: { delay: 0.5, duration: 0.3 },
                    }}
                    exit={{
                      scale: 0,
                      transition: {
                        delay: 0.0,
                        duration: 0.15,
                      },
                    }}
                  >
                    <div
                      className="flex items-center justify-center transition-all
                       bg-background hover:bg-accent hover:text-destructive/70
                       border-destructive text-destructive border
                       h-9 rounded-md px-3 hover:scale-[1.02]"
                      // onClick={(e) => e.stopPropagation()}
                    >
                      <Trash2 className="w-5 h-6" />
                    </div>
                  </motion.div>
                }
              />
            )}
          </AnimatePresence>
        </div>
      </Button>
    </div>
  );
}
