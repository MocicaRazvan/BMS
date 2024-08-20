"use client";

import { ChatRoomResponse, PageableResponse, PageInfo } from "@/types/dto";
import { ChangeEvent, Suspense, useCallback, useEffect, useState } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import useFetchStream from "@/hoooks/useFetchStream";
import { WithUser } from "@/lib/user";
import ChatMainContent, {
  MainContentTexts,
} from "@/app/[locale]/(main)/(user)/chat/main-content";
import { useSearchParams } from "next/navigation";
import SearchInput from "@/components/forms/input-serach";
import { useDebounce } from "@/components/ui/multiple-selector";
import {
  DataTablePagination,
  DataTablePaginationTexts,
} from "@/components/table/data-table-pagination";
import { cn } from "@/lib/utils";

export interface ChatMainContentWrapperTexts {
  dataTablePaginationTexts: DataTablePaginationTexts;
  mainContentTexts: MainContentTexts;
  search: string;
  errorLoading: string;
}

interface ChatMainContentWrapperProps
  extends WithUser,
    ChatMainContentWrapperTexts {}

export default function ChatMainContentWrapper({
  authUser,
  mainContentTexts,
  dataTablePaginationTexts,
  search,
  errorLoading,
}: ChatMainContentWrapperProps) {
  const currentSearchParams = useSearchParams();

  // const {
  //   messages: connectedUsers,
  //   error: uError,
  //   isFinished: uIsFinished,
  // } = useFetchStream<ConversationUserResponse[]>({
  //   path: "/ws-http/getConnectedUsers",
  //   acceptHeader: "application/json",
  //   useAbortController: false,
  //   authToken: true,
  // });
  //
  // const {
  //   messages: chatRooms,
  //   error: rError,
  //   isFinished: rIsFinished,
  //   refetch: refetchChatRooms,
  // } = useFetchStream<ChatRoomResponse[]>({
  //   path: `/ws-http/chatRooms/${authUser.email}`,
  //   acceptHeader: "application/json",
  //   useAbortController: false,
  //   authToken: true,
  // });

  const [filterEmail, setFilterEmail] = useState("");
  const debouncedFilter = useDebounce(filterEmail, 500);
  const [pageInfo, setPageInfo] = useState<PageInfo>({
    currentPage: 0,
    totalPages: 0,
    totalElements: 0,
    pageSize: 15,
  });
  const {
    messages,
    error,
    isFinished: pIsFinished,
    refetch,
  } = useFetchStream<PageableResponse<ChatRoomResponse[]>>({
    path: `/ws-http/chatRooms/filter/${authUser.email}`,
    method: "PATCH",
    acceptHeader: "application/json",
    useAbortController: false,
    authToken: true,
    body: {
      page: pageInfo.currentPage,
      size: pageInfo.pageSize,
    },
    queryParams: {
      filterReceiver: debouncedFilter,
    },
  });

  console.log("PATCH messages", messages);

  useEffect(() => {
    if (messages && messages.length > 0 && messages[0].pageInfo) {
      setPageInfo((prev) => ({
        ...prev,
        totalPages: messages[0].pageInfo.totalPages,
        totalElements: messages[0].pageInfo.totalElements,
      }));
    }
  }, [JSON.stringify(messages)]);

  // if (uError || rError) {
  //   console.error("Error fetching messages:", uError || rError);
  //   return <div>Error loading messages.</div>;
  // }
  //
  // if (!uIsFinished || !rIsFinished) return <LoadingSpinner />;

  const handleChangeSearch = useCallback((e: ChangeEvent<HTMLInputElement>) => {
    setPageInfo((prev) => ({ ...prev, currentPage: 0 }));
    setFilterEmail(e.target.value);
  }, []);
  const handleClearSearch = useCallback(() => {
    setPageInfo((prev) => ({ ...prev, currentPage: 0 }));
    setFilterEmail("");
  }, []);

  const deleteChatRoomCallback = useCallback(() => {
    refetch();
  }, [refetch]);

  if (pIsFinished && error) {
    console.error("Error fetching messages:", error);
    return (
      <div className="w-full h-full p-20 flex items-center justify-center">
        <p className="text-xl font-bold">{errorLoading}</p>
      </div>
    );
  }

  // return null;

  return (
    <Suspense fallback={<LoadingSpinner />}>
      <div className="mb-20 md:px-6 flex flex-col w-full mx-auto max-w-7xl items-center justify-center mt-10 ">
        <div
          className={cn(
            "w-full flex flex-col items-center md:items-start justify-start ",
            // pageInfo.totalElements === 0 && "hidden",
          )}
        >
          <SearchInput
            value={filterEmail}
            searchInputTexts={{ placeholder: search }}
            onClear={handleClearSearch}
            onChange={handleChangeSearch}
          />

          <DataTablePagination
            pageInfo={pageInfo}
            setPageInfo={setPageInfo}
            sizeOptions={[1, 15, 30, 50]}
            col
            {...dataTablePaginationTexts}
          />
        </div>
        <hr className="my-2" />
        <div className="w-full">
          {!pIsFinished || messages.length === 0 ? (
            <LoadingSpinner />
          ) : (
            <ChatMainContent
              // initialConnectedUsers={connectedUsers[0]}
              initialConnectedUsers={[]}
              initialChatRooms={messages[0].content}
              authUser={authUser}
              deleteCallBack={deleteChatRoomCallback}
              {...mainContentTexts}
            />
          )}
        </div>
      </div>
    </Suspense>
  );
}
