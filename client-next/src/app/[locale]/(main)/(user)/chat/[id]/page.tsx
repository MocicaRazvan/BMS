import { Locale } from "@/navigation";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUser } from "@/lib/user";
import SingleChatPageContent from "@/app/[locale]/(main)/(user)/chat/[id]/page-content";
import { getConversationTexts } from "@/texts/components/chat";

interface Props {
  searchParams: any;
  params: {
    locale: Locale;
    id: string;
  };
}

export async function generateMetadata({
  params: { locale, id },
}: Props): Promise<Metadata> {
  return {
    ...(await getIntlMetadata("user.Chat", "/chat", locale)),
  };
}
export default async function ChatPage({
  searchParams,
  params: { locale, id },
}: Props) {
  unstable_setRequestLocale(locale);
  const [authUser, conversationTexts] = await Promise.all([
    getUser(),
    getConversationTexts(),
  ]);
  return (
    <div className="w-full h-full overflow-hidden">
      <SingleChatPageContent
        id={id}
        authUser={authUser}
        conversationTexts={conversationTexts}
      />
    </div>
  );
}
