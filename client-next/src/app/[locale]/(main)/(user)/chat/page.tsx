import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import ChatMainContentWrapper, {
  ChatMainContentWrapperTexts,
} from "@/app/[locale]/(main)/(user)/chat/main-content-wrapper";
import { getUser } from "@/lib/user";
import { getChatMainContentWrapperTexts } from "@/texts/components/chat";

interface Props {
  searchParams: any;
}

export default async function ChatPage({ searchParams }: Props) {
  console.log("chatIdS p", searchParams);
  // const session = await getServerSession(authOptions);
  // if (!session?.user) {
  //   return null;
  // }

  const [authUser, chatMainContentWrapperTexts] = await Promise.all([
    getUser(),
    getChatMainContentWrapperTexts(),
  ]);

  return (
    <div>
      <Suspense fallback={<LoadingSpinner />}>
        <ChatMainContentWrapper
          authUser={authUser}
          {...chatMainContentWrapperTexts}
        />
      </Suspense>
    </div>
  );
}
