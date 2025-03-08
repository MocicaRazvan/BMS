import { ConversationTexts } from "@/components/chat/conversation";
import { getChatMessageFormTexts } from "@/texts/components/forms";
import { getTranslations } from "next-intl/server";
import { MainContentTexts } from "@/app/[locale]/(main)/(user)/chat/main-content";
import { ChatMainContentWrapperTexts } from "@/app/[locale]/(main)/(user)/chat/main-content-wrapper";
import { getDataTablePaginationTexts } from "@/texts/components/table";
import { ChatRoomTexts } from "@/components/chat/chat-room";
import { getDeleteChatRoomDialogTexts_FallBack } from "@/texts/components/dialog";

export async function getChatRoomTexts(): Promise<ChatRoomTexts> {
  const [t, deleteChatDialogTexts] = await Promise.all([
    getTranslations("components.chat.ChatRoomTexts"),
    getDeleteChatRoomDialogTexts_FallBack("place_holder"),
  ]);
  return { numberUnread: t("numberUnread"), deleteChatDialogTexts };
}

export async function getConversationTexts(): Promise<ConversationTexts> {
  const [chatMessageFormTexts, t] = await Promise.all([
    getChatMessageFormTexts(),
    getTranslations("components.chat.ConversationTexts"),
  ]);

  return {
    chatMessageFormTexts,
    userInTheSameChat: t("userInTheSameChat"),
    loadMoreLoading: t("loadMoreLoading"),
    loadMore: t("loadMore"),
    errorLoading: t("errorLoading"),
  };
}

export async function getMainContentTexts(): Promise<MainContentTexts> {
  const [conversationTexts, chatRoomTexts, t] = await Promise.all([
    getConversationTexts(),
    getChatRoomTexts(),
    getTranslations("components.chat.MainContentTexts"),
  ]);
  return {
    conversationTexts,
    chatRoomsLabel: t("chatRoomsLabel"),
    noSelectedChatRoom: t("noSelectedChatRoom"),
    chatRoomTexts,
  };
}

export async function getChatMainContentWrapperTexts(): Promise<ChatMainContentWrapperTexts> {
  const [mainContentTexts, dataTablePaginationTexts, t] = await Promise.all([
    getMainContentTexts(),
    getDataTablePaginationTexts(),
    getTranslations("components.chat.ChatMainContentWrapperTexts"),
  ]);
  return {
    mainContentTexts,
    search: t("search"),
    errorLoading: t("errorLoading"),
    dataTablePaginationTexts,
  };
}
