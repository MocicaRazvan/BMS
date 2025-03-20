import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import ChatRooms from "@/components/chat/chat-rooms";
import { getUser } from "@/lib/user";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";
import NoRoom from "@/components/chat/no-room";
import { CurRoomsProvider } from "@/context/cur-rooms-context";
import { getChatRoomsTexts } from "@/texts/components/chat";

export default async function ChatLayout({
  children,
  params: { locale },
}: Readonly<{ children: React.ReactNode; params: { locale: Locale } }>) {
  unstable_setRequestLocale(locale);
  const [authUser, chatRoomsTexts] = await Promise.all([
    getUser(),
    getChatRoomsTexts(),
  ]);
  return (
    <CurRoomsProvider authUser={authUser}>
      <div className="flex h-[150vh] md:h-[92vh] px-5 mt-1.5">
        <div
          className="hidden md:flex md:flex-col md:items-center md:justify-center w-96 border-t border-b border-l rounded-l  bg-muted/20
        backdrop-blur supports-[backdrop-filter]:bg-muted/20
        overflow-y-auto h-full "
        >
          <aside className="p-2 space-y-1 flex-1">
            <Suspense fallback={<LoadingSpinner />}>
              <ChatRooms {...chatRoomsTexts} />
            </Suspense>
          </aside>
        </div>

        <div className="flex-1 flex flex-col rounded-r border  bg-muted/20 backdrop-blur supports-[backdrop-filter]:bg-muted/20 ">
          {children}
        </div>
        <NoRoom authUser={authUser} />
      </div>
    </CurRoomsProvider>
  );
}
