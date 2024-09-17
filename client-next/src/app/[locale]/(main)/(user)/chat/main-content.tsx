"use client";
import { useStompClient, useSubscription } from "react-stomp-hooks";
import { useCallback, useEffect, useState } from "react";
import { ChatRoomResponse, ConversationUserResponse } from "@/types/dto";
import { useSearchParams } from "next/navigation";
import ConversationWrapper from "@/components/chat/conversation-wrapper";
import { ChatRoom, ChatRoomTexts } from "@/components/chat/chat-room";
import { useChatNotification } from "@/context/chat-message-notification-context";
import { parseQueryParamAsInt } from "@/lib/utils";
import { fetchStream } from "@/hoooks/fetchStream";
import { WithUser } from "@/lib/user";
import { useRouter } from "@/navigation";
import { ConversationTexts } from "@/components/chat/conversation";

export interface MainContentTexts {
  chatRoomsLabel: string;
  noSelectedChatRoom: string;
  conversationTexts: ConversationTexts;
  chatRoomTexts: ChatRoomTexts;
}

interface ChatMainContentProps extends WithUser, MainContentTexts {
  initialConnectedUsers: ConversationUserResponse[];
  initialChatRooms: ChatRoomResponse[];
  deleteCallBack: () => void;
}

// todo search by email in chat rooms si pagination
export default function ChatMainContent({
  initialConnectedUsers,
  initialChatRooms,
  authUser,
  noSelectedChatRoom,
  chatRoomsLabel,
  conversationTexts,
  chatRoomTexts,
  deleteCallBack,
}: ChatMainContentProps) {
  const stompClient = useStompClient();
  const router = useRouter();

  const searchParams = useSearchParams();
  console.log("searchParams", searchParams.toString());
  const chatId = searchParams.get("chatId");
  console.log("chatIdS", chatId);
  const email = searchParams.get("email");

  const { getNotificationState, removeBySender } = useChatNotification();

  // const [convUsers, setConvUsers] = useState<ConversationUserResponse[]>(
  //   initialConnectedUsers,
  // );
  const [chatRooms, setChatRooms] =
    useState<ChatRoomResponse[]>(initialChatRooms);

  const [activeRoom, setActiveRoom] = useState<ChatRoomResponse | null>(null);
  // const { activeChatId, setActiveChatId } = useChatContext();
  const [activeRoomId, setActiveRoomId] = useState<number | null>(
    chatId ? parseQueryParamAsInt(chatId, null) : null,
    // activeChatId,
    // null,
  );

  console.log("chatIdSARI", activeRoomId);
  const [initialChatId, setInitialChatId] = useState<number | null>(null);
  console.log("chatIdSINIT", initialChatId);
  console.log("chatIdSROOM", activeRoom);
  console.log("chatIdSROOMSender", authUser.email);
  console.log(
    "chatIdSROOMUsers",
    activeRoom?.users.map((user) => user.email),
  );

  useEffect(() => {
    if (email) {
      fetchStream<ChatRoomResponse>({
        token: authUser.token,
        path: "/ws-http/chatRooms/findAllByEmails",
        acceptHeader: "application/json",
        arrayQueryParam: {
          emails: [authUser.email, email],
        },
      }).then(({ messages, error, isFinished }) => {
        if (isFinished && messages.length > 0) {
          const room = chatRooms.find((room) => room.id === messages[0].id);
          if (!room) {
            setChatRooms((prev) => [...prev, messages[0]]);
          }
          const params = new URLSearchParams(searchParams.toString());
          params.set("chatId", messages[0].id.toString());
          params.delete("email");
          window.history.pushState(null, "", `?${params.toString()}`);
        }
      });
    }
  }, [
    authUser.email,
    authUser.token,
    email,
    // searchParams,
    JSON.stringify(chatRooms),
  ]);

  useEffect(() => {
    const initialId = searchParams.get("chatId");
    if (initialId) {
      setInitialChatId(parseQueryParamAsInt(initialId, null));
    }
  }, []);

  useEffect(() => {
    // setActiveChatId(activeRoomId);
    if (activeRoomId) {
      const room = chatRooms.find((room) => room.id === activeRoomId);
      if (room) {
        const users = [
          ...room.users.filter(({ email }) => email !== authUser.email),
          {
            ...room.users.find(({ email }) => email === authUser.email),
            connectedChatRoom: room,
          },
        ] as ConversationUserResponse[];

        console.log(
          "rue",
          room.users.map((user) => user?.connectedChatRoom?.id),
        );
        setActiveRoom({ ...room, users });
      } else {
        setActiveRoom(null);
        setActiveRoomId(null);
      }
    } else {
      setActiveRoom(null);
    }
  }, [activeRoomId, JSON.stringify(chatRooms)]);

  useEffect(() => {
    const params = new URLSearchParams(searchParams.toString());

    console.log("chatIdS params", params.toString());

    if (activeRoomId) {
      params.set("chatId", activeRoomId.toString());
      params.delete("email");
    } else {
      if (params.has("chatId")) {
        params.delete("chatId");
        params.delete("email");
      }
    }
    window.history.pushState(null, "", `?${params.toString()}`);
    // router.replace(`/chat?${params.toString()}`);
  }, [activeRoomId, searchParams]);

  // useSubscription(`/user/${authUser.email}/chatRooms`, (message) => {
  //   const newMessage = JSON.parse(message.body);
  //   setChatRooms((prev) => {
  //     const isRoomPresent = prev.findIndex((room) => room.id === newMessage.id);
  //     if (isRoomPresent === -1) {
  //       return prev;
  //     }
  //     // return [...prev.filter((room) => room.id !== newMessage.id), newMessage];
  //     return prev.map((room) =>
  //       room.id === newMessage.id ? newMessage : room,
  //     );
  //   });
  // });
  useSubscription(`/queue/chatRooms-${authUser.email}`, (message) => {
    const newMessage = JSON.parse(message.body);
    setChatRooms((prev) => {
      const isRoomPresent = prev.findIndex((room) => room.id === newMessage.id);
      if (isRoomPresent === -1) {
        return prev;
      }
      // return [...prev.filter((room) => room.id !== newMessage.id), newMessage];
      return prev.map((room) =>
        room.id === newMessage.id ? newMessage : room,
      );
    });
  });
  // useSubscription(`/user/${authUser.email}/chatRooms/delete`, (message) => {
  //   const newMessage = JSON.parse(message.body);
  //   setChatRooms((prev) => prev.filter((room) => room.id !== newMessage.id));
  // });
  useSubscription(`/queue/chatRooms-delete-${authUser.email}`, (message) => {
    const newMessage = JSON.parse(message.body);
    setChatRooms((prev) => prev.filter((room) => room.id !== newMessage.id));
  });

  useSubscription(`/queue/chatRooms-create-${authUser.email}`, (message) => {
    const newMessage = JSON.parse(message.body);
    setChatRooms((prev) => {
      const isRoomPresent = prev.findIndex((room) => room.id === newMessage.id);
      if (isRoomPresent === -1) {
        return [...prev, newMessage];
      }
      return prev;
    });
  });

  const [clientInitialized, setClientInitialized] = useState(false);

  useEffect(() => {
    ////

    if (initialChatId !== null && stompClient?.connected) {
      const validId = initialChatRooms.find(
        (room) => room.id === initialChatId,
      );
      setInitialChatId(null);
      if (validId) {
        stompClient.publish({
          destination: "/app/changeRoom",
          body: JSON.stringify({
            chatId: initialChatId,
            userEmail: authUser.email,
          }),
        });
      }
    }
    ///

    if (!clientInitialized && stompClient?.connected) {
      console.log("USE: Connecting user:", authUser.email);
      stompClient.publish({
        destination: `/app/connectUser/${authUser.email}`,
        body: JSON.stringify({
          email: authUser.email,
        }),
      });
      setClientInitialized(true);
    }

    return () => {
      console.log("USE: Cleanup called");
    };
  }, [
    authUser.email,
    stompClient?.connected,
    clientInitialized,
    initialChatId,
    JSON.stringify(initialChatRooms),
  ]);

  useEffect(() => {
    // console.log("USE2: useEffect triggered " + chatId);
    if (stompClient && stompClient.connected && chatId) {
      const roomId = parseQueryParamAsInt(chatId, null);
      const room = chatRooms.find((r) => r.id === roomId);

      if (room) {
        setActiveRoomId(room.id);
        // setActiveChatId(room.id);
        if (activeRoomId !== room.id) {
          stompClient.publish({
            destination: "/app/changeRoom",
            body: JSON.stringify({
              chatId: room.id,
              userEmail: authUser.email,
            }),
          });
          const otherUser = room.users.find(
            (user) => user.email !== authUser.email,
          );
          if (otherUser) {
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
        }
      }
    }
  }, [
    authUser.email,
    chatId,
    JSON.stringify(chatRooms),
    removeBySender,
    // setActiveChatId,
    !!stompClient?.connected,
    activeRoomId,
  ]);

  const handleRoomDelete = useCallback(
    async (chatRoomId: number) => {
      console.log("HERE");
      if (!authUser?.email || !authUser?.token) return;
      const { messages, error, isFinished } = await fetchStream({
        path: "/ws-http/chatRooms",
        method: "DELETE",
        body: { chatRoomId, senderEmail: authUser.email },
        acceptHeader: "application/json",
        token: authUser.token,
      });
      if (error) {
        console.error("Error deleting chat room:", error);
        return;
      } else {
        console.log("Chat room deleted");
        // setChatRooms((prev) => prev.filter((room) => room.id !== chatRoomId));
        deleteCallBack();
        router.push("/chat");
      }
    },
    [authUser.email, authUser.token, deleteCallBack, router],
  );

  return (
    <div className=" min-h-[1000px] w-full ">
      {/*<h1 className="text-4xl font-bold tracking-tighter mb-10 text-center">*/}
      {/*  Chat*/}
      {/*</h1>*/}
      {/*<h1>Connected Users</h1>*/}
      {/*<UserList users={convUsers} authUser={authUser} />*/}
      <div className="flex md:flex-row flex-col justify-center items-center md:items-start w-full gap-6 h-full ">
        <div className="flex-1 md:flex-1 h-full border-2 p-4 rounded-md py-6 ">
          {/*<Link href={"/chat"}>CHAT</Link>*/}
          <h1
            className="font-bold text-xl tracking-tighter text-center h-[40px] "
            onClick={() => {
              if (stompClient && stompClient?.connected) {
                setActiveRoomId(null);
                setActiveRoom(null);
                setInitialChatId(null);

                stompClient.publish({
                  destination: "/app/changeRoom",
                  body: JSON.stringify({
                    chatId: null,
                    userEmail: authUser.email,
                  }),
                });

                const params = new URLSearchParams(searchParams.toString());
                //
                // console.log("searchParams SET");
                // params.set("random", Math.random().toString());
                // params.set("random", "null");
                params.delete("chatId");
                // router.push("/chat?" + params.toString());
                // router.push(`/chat?${params.toString()}`);

                window.history.pushState(null, "", `?${params.toString()}`);
              }
            }}
          >
            {chatRoomsLabel}
          </h1>
          <hr className="my-2" />
          <ChatRoom
            chatRooms={chatRooms}
            setActiveRoomId={setActiveRoomId}
            // setActiveRoomId={setActiveChatId}
            activeRoom={activeRoom}
            authUser={authUser}
            handleRoomDelete={handleRoomDelete}
            baseChat={() => {
              setActiveRoomId(null);
              setActiveRoom(null);
              setInitialChatId(null);
            }}
            {...chatRoomTexts}
          />
        </div>

        <div className=" flex-1 w-[91%] mx-auto  md:flex-[2.5_2.5_0%]">
          {activeRoom ? (
            <ConversationWrapper
              chatRoomId={activeRoom.id}
              sender={
                activeRoom.users.find(
                  ({ email }) => email === authUser.email,
                ) as ConversationUserResponse
              }
              receiver={
                activeRoom.users.find(
                  ({ email }) => email !== authUser.email,
                ) as ConversationUserResponse
              }
              {...conversationTexts}
            />
          ) : (
            <div className="flex items-center justify-center h-full min-h-[500px] w-full ">
              <h1 className="text-4xl font-bold tracking-tighter">
                {noSelectedChatRoom}
              </h1>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
