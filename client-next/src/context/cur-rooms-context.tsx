"use client";
import {
  ChangeEvent,
  createContext,
  Dispatch,
  ReactNode,
  SetStateAction,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import {
  ChatMessageResponse,
  ChatRoomResponse,
  ChatRoomResponseJoined,
  JoinedConversationUser,
  PageableResponse,
  PageInfo,
} from "@/types/dto";
import {
  useFetchStream,
  UseFetchStreamProps,
  UseFetchStreamReturn,
} from "@/lib/fetchers/useFetchStream";
import { BaseError } from "@/types/responses";
import { WithUser } from "@/lib/user";
import { useDebounce } from "@/components/ui/multiple-selector";
import { useRouter } from "@/navigation/client-navigation";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { useStompClient, useSubscription } from "react-stomp-hooks";
import { useParams, useSearchParams } from "next/navigation";
import { wrapItemToString } from "@/lib/utils";
import { useChatNotification } from "@/context/chat-message-notification-context";
import { useCacheInvalidator } from "@/providers/cache-provider";
import { CustomAbortController } from "@/lib/fetchers/custom-abort-controller";

export interface ChatRoomsState
  extends Omit<
      UseFetchStreamReturn<
        PageableResponse<ChatRoomResponseJoined[]>,
        BaseError
      >,
      | "resetFinishes"
      | "isRefetchClosure"
      | "removeKeyFromHistory"
      | "addKeyToHistory"
    >,
    WithUser {
  curRoom: ChatRoomResponseJoined | undefined;
  filterEmail: string;
  setFilterEmail: (email: string) => void;
  debouncedFilterEmail: string;
  pageInfo: PageInfo;
  setPageInfo: Dispatch<SetStateAction<PageInfo>>;
  handleChangeSearch: (e: ChangeEvent<HTMLInputElement>) => void;
  handleClearSearch: () => void;
  chatRooms: ChatRoomResponseJoined[];
  handleRoomDelete: (
    room: ChatRoomResponseJoined,
    otherUser: JoinedConversationUser,
  ) => Promise<void>;
  handleRoomChange: (
    room: ChatRoomResponseJoined,
    otherUser: JoinedConversationUser,
  ) => void;
  handleRoomHover: (room: ChatRoomResponseJoined) => void;
  otherUser: JoinedConversationUser | undefined;
  curUser: JoinedConversationUser | undefined;
}

export const CurRoomsContext = createContext<ChatRoomsState | null>(null);

interface Props extends WithUser {
  children: ReactNode;
}
const messagesArgs: UseFetchStreamProps = {
  path: "",
  acceptHeader: "application/json",
  authToken: true,
  method: "GET",
  body: null,
  arrayQueryParam: {},
  customHeaders: {},
  queryParams: {
    offset: "0",
    limit: "10",
  },
  trigger: false,
};

const initialPageInfo: PageInfo = {
  currentPage: 0,
  totalPages: 0,
  totalElements: 0,
  pageSize: 15,
};
export const CurRoomsProvider = ({ children, authUser }: Props) => {
  const [filterEmail, setFilterEmail] = useState("");
  const debouncedFilter = useDebounce(filterEmail, 300);
  const params = useParams<{ id: string }>();
  const initialCacheKey = useRef<string | null>(null);

  const { removeFromCache } = useCacheInvalidator();
  const [pageInfo, setPageInfo] = useState<PageInfo>(initialPageInfo);
  const {
    messages,
    error,
    isFinished,
    refetch,
    cacheKey,
    refetchState,
    isAbsoluteFinished,
    manualFetcher,
    removeKeyFromHistory,
  } = useFetchStream<PageableResponse<ChatRoomResponseJoined[]>>({
    path: `/ws-http/chatRooms/filter-joined/${authUser.email}`,
    method: "PATCH",
    acceptHeader: "application/json",
    authToken: true,
    body: {
      page: pageInfo.currentPage,
      size: pageInfo.pageSize,
    },
    queryParams: {
      filterReceiver: debouncedFilter,
    },
  });

  const { manualFetcher: messagesManualFetcher } =
    useFetchStream<PageableResponse<ChatMessageResponse[]>>(messagesArgs);

  const stompClient = useStompClient();
  const router = useRouter();
  const searchParams = useSearchParams();
  const [chatRooms, setChatRooms] = useState<ChatRoomResponseJoined[]>([]);
  const [curRoom, setCurRoom] = useState<ChatRoomResponseJoined | undefined>(
    undefined,
  );
  const { removeBySender } = useChatNotification();
  const messagesManualFetcherAbort = useRef(new CustomAbortController());
  const isInitialCacheDirty = useRef(false);

  useEffect(() => {
    return () => {
      if (messagesManualFetcherAbort.current) {
        messagesManualFetcherAbort.current.abort();
      }
    };
  }, []);

  useEffect(() => {
    return () => {
      if (isInitialCacheDirty.current && initialCacheKey.current) {
        removeFromCache(initialCacheKey.current);
        isInitialCacheDirty.current = false;
      }
    };
  }, [removeFromCache]);

  const otherUser = useMemo(
    () =>
      curRoom?.users.find((u) => u.conversationUser.email !== authUser.email),
    [authUser.email, curRoom?.users],
  );

  const curUser = useMemo(
    () =>
      curRoom?.users.find((u) => u.conversationUser.email === authUser.email),
    [authUser.email, curRoom?.users],
  );

  useEffect(() => {
    if (messages.length > 0) {
      setChatRooms(messages[0].content);
    }
  }, [messages]);

  // useEffect(() => {
  //   console.log("CHAT ROOMS CONTEXT", chatRooms);
  // }, [JSON.stringify(chatRooms)]);

  useEffect(() => {
    if (initialCacheKey.current === null) {
      initialCacheKey.current = cacheKey;
    }
  }, [cacheKey]);

  useEffect(() => {
    if (messages && messages.length > 0 && messages[0].pageInfo) {
      setPageInfo((prev) => ({
        ...prev,
        totalPages: messages[0].pageInfo.totalPages,
        totalElements: messages[0].pageInfo.totalElements,
      }));
    }
  }, [messages]);

  const handleChangeSearch = useCallback(
    (e: ChangeEvent<HTMLInputElement>) => {
      router.push("/chat");
      setPageInfo((prev) => ({ ...prev, currentPage: 0 }));
      setFilterEmail(e.target.value);
    },
    [router],
  );
  const handleClearSearch = useCallback(() => {
    router.push("/chat");
    setPageInfo((prev) => ({ ...prev, currentPage: 0 }));
    setFilterEmail("");
  }, [router]);

  useEffect(() => {
    const searchEmail = searchParams.get("email");
    if (!searchEmail) return;

    const abortController = new AbortController();
    fetchStream<ChatRoomResponseJoined>({
      token: authUser.token,
      path: "/ws-http/chatRooms/findAllByEmails-joined",
      acceptHeader: "application/json",
      arrayQueryParam: { emails: [authUser.email, searchEmail] },
      aboveController: abortController,
    })
      .then(({ messages, isFinished }) => {
        if (abortController.signal.aborted) {
          return;
        }

        if (!isFinished || messages.length === 0) return;
        const newRoom = messages[0];

        setChatRooms((prev) =>
          prev.some((r) => r.id === newRoom.id) ? prev : [...prev, newRoom],
        );
        const params = new URLSearchParams(searchParams.toString());
        params.delete("email");

        router.replace(`/chat/${newRoom.id}?${params.toString()}`);

        // setCurEmail(searchEmail);
      })
      .catch(console.log);
    return () => {
      abortController.abort();
    };
  }, [authUser, router, searchParams]);

  useSubscription(`/queue/chatRooms-joined-${authUser.email}`, (message) => {
    const newMessage = JSON.parse(message.body) as ChatRoomResponseJoined;
    setChatRooms((prev) => {
      const isRoomPresent = prev.findIndex((room) => room.id === newMessage.id);
      // console.log(
      //   "Chat main content useSubscription",
      //   `/queue/chatRooms-${authUser.email}`,
      //   isRoomPresent,
      //   newMessage.id.toString(),
      //   newMessage,
      // );
      if (isRoomPresent === -1) {
        return prev;
      }
      return prev.map((room) =>
        room.id === newMessage.id ? newMessage : room,
      );
    });
  });

  useSubscription(`/queue/chatRooms-${authUser.email}`, (message) => {
    const newMessage = JSON.parse(message.body) as ChatRoomResponse;
    setChatRooms((prev) => {
      const room = prev.find((room) => room.id === newMessage.id);
      if (!room) {
        return prev;
      }
      const newUsers = newMessage.users.reduce((acc, cur) => {
        const oldUser = room.users.find(
          (user) => user.conversationUser.email === cur.email,
        );
        if (oldUser) {
          acc.push({
            reactiveUser: oldUser.reactiveUser,
            conversationUser: cur,
          });
        }
        return acc;
      }, [] as JoinedConversationUser[]);

      if (newUsers.length !== 2) {
        return prev;
      }

      return prev.map((room) =>
        room.id === newMessage.id
          ? {
              ...newMessage,
              users: newUsers,
            }
          : room,
      );
    });
  });

  useSubscription(
    `/queue/chatRooms-joined-delete-${authUser.email}`,
    (message) => {
      const newMessage = JSON.parse(message.body) as ChatRoomResponseJoined;

      setChatRooms((prev) => prev.filter((room) => room.id !== newMessage.id));
      if (initialCacheKey.current) {
        isInitialCacheDirty.current = true;
        removeKeyFromHistory(initialCacheKey.current);
      }
    },
  );

  useSubscription(
    `/queue/chatRooms-joined-create-${authUser.email}`,
    (message) => {
      const newMessage = JSON.parse(message.body) as ChatRoomResponseJoined;
      setChatRooms((prev) => {
        const room = prev.find((room) => room.id === newMessage.id);
        if (!room) {
          return [...prev, newMessage];
        }
        return prev;
      });
      if (initialCacheKey.current) {
        isInitialCacheDirty.current = true;
        removeKeyFromHistory(initialCacheKey.current);
      }
    },
  );

  // useSubscription(`/queue/chat-changed-${authUser?.email}`, (message) => {
  //   const newMessage = JSON.parse(message.body) as ConversationUserResponse;
  //   console.log("Chat changed useSubscription", newMessage);
  // });

  const handleRoomDelete = useCallback(
    async (room: ChatRoomResponseJoined, otherUser: JoinedConversationUser) => {
      if (!stompClient || !stompClient.connected) return;
      const { messages, error, isFinished } = await fetchStream({
        path: "/ws-http/chatRooms",
        method: "DELETE",
        body: { chatRoomId: room.id, senderEmail: authUser.email },
        acceptHeader: "application/json",
        token: authUser.token,
      });

      if (error) {
        console.error("Error deleting chat room:", error);
        return;
      } else if (isFinished) {
        setChatRooms((prev) => prev.filter((pr) => pr.id !== room.id));
        removeBySender({
          senderEmail: otherUser.conversationUser.email,
          receiverEmail: authUser.email,
          stompClient,
        });
        if (initialCacheKey.current) {
          isInitialCacheDirty.current = true;
          removeKeyFromHistory(initialCacheKey.current);
        }
        if (curRoom?.id === room.id) {
          setCurRoom(undefined);
          router.replace("/chat");
        }
      }
    },
    [
      authUser.email,
      authUser.token,
      curRoom?.id,
      router,
      stompClient?.connected,
      initialCacheKey,
      removeKeyFromHistory,
    ],
  );

  const handleRoomChange = useCallback(
    (room: ChatRoomResponseJoined, otherUser: JoinedConversationUser) => {
      if (!stompClient || !stompClient.connected) return;
      removeBySender({
        stompClient,
        senderEmail: otherUser.conversationUser.email,
        receiverEmail: authUser.email,
      });
      router.push(`/chat/${room.id}`);
    },
    [authUser.email, removeBySender, router, stompClient],
  );

  const handleRoomHover = useCallback(
    (room: ChatRoomResponseJoined) => {
      if (room.id === curRoom?.id) {
        return;
      }
      if (messagesManualFetcherAbort.current) {
        messagesManualFetcherAbort.current.abort();
      }
      messagesManualFetcherAbort.current = new CustomAbortController();
      messagesManualFetcher({
        fetchProps: {
          ...messagesArgs,
          path: "/ws-http/messages/" + room.id,
        },
        localAuthToken: true,
        aboveController: messagesManualFetcherAbort.current,
        errorCallback: () => {
          if (
            messagesManualFetcherAbort.current &&
            !messagesManualFetcherAbort.current?.signal.aborted
          ) {
            messagesManualFetcherAbort.current?.abort();
          }
        },
      }).catch((e: any) => {
        if ("name" in e && e?.name === "AbortError") {
          return;
        }
        console.log("Error manual fetching messages", e);
      });
    },
    [messagesManualFetcher, curRoom?.id],
  );

  useEffect(() => {
    if (params?.id && isFinished) {
      setCurRoom(
        chatRooms.find(
          (room) => wrapItemToString(room.id) === wrapItemToString(params.id),
        ),
      );
    } else {
      setCurRoom(undefined);
    }
  }, [chatRooms, params?.id, isFinished]);

  return (
    <CurRoomsContext.Provider
      value={{
        messages,
        error,
        isFinished,
        refetch,
        cacheKey,
        refetchState,
        isAbsoluteFinished,
        manualFetcher,
        curRoom,
        filterEmail,
        setFilterEmail,
        debouncedFilterEmail: debouncedFilter,
        pageInfo,
        setPageInfo,
        handleChangeSearch,
        handleClearSearch,
        chatRooms,
        handleRoomDelete,
        handleRoomChange,
        authUser,
        handleRoomHover,
        otherUser,
        curUser,
      }}
    >
      {children}
    </CurRoomsContext.Provider>
  );
};

export const useCurRooms = () => {
  const context = useContext(CurRoomsContext);
  if (context === null) {
    throw new Error(
      " useCurRooms must be used within a CurRoomsContext.Provider",
    );
  }
  return context;
};
