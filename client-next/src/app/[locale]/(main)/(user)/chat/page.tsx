import { Suspense } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";
import ChatMainContentWrapper from "@/app/[locale]/(main)/(user)/chat/main-content-wrapper";
import { getUser } from "@/lib/user";
import { getChatMainContentWrapperTexts } from "@/texts/components/chat";
import { Locale } from "@/navigation";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";

interface Props {
  searchParams: any;
  params: {
    locale: Locale;
  };
}

export async function generateMetadata({
  params: { locale },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.Chat", "/chat", locale)),
  };
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
