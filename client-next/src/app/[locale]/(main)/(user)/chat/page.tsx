import { Locale } from "@/navigation";
import { Metadata } from "next";
import { getIntlMetadata } from "@/texts/metadata";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUser } from "@/lib/user";
import ChatPageMainPageContent from "@/app/[locale]/(main)/(user)/chat/page-content";
import { getChatPageTexts } from "@/texts/pages";

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
export default async function ChatPage({
  searchParams,
  params: { locale },
}: Props) {
  unstable_setRequestLocale(locale);
  const [authUser, chatPageTexts] = await Promise.all([
    getUser(),
    getChatPageTexts(),
  ]);
  return (
    <div className="w-full h-full">
      <ChatPageMainPageContent authUser={authUser} {...chatPageTexts} />
    </div>
  );
}
