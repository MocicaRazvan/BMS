import { getTranslations } from "next-intl/server";
import { getDeleteChatRoomDialogTexts_FallBack } from "@/texts/components/dialog";
import {
  ChatRoomContentTexts,
  ChatRoomItemTexts,
  ChatRoomsTexts,
} from "@/components/chat/chat-rooms";
import { getDataTablePaginationTexts } from "@/texts/components/table";
import {
  ConversationContentTexts,
  ConversationTexts,
} from "@/components/chat/conversation";
import { getChatMessageFormTexts } from "@/texts/components/forms";

export async function getChatRoomItemTexts(): Promise<ChatRoomItemTexts> {
  const [t, deleteChatDialogTexts] = await Promise.all([
    getTranslations("components.chat.ChatRoomItemTexts"),
    getDeleteChatRoomDialogTexts_FallBack("place_holder"),
  ]);

  return {
    unreadMessagesText: t("unreadMessagesText"),
    deleteChatDialogTexts,
    typingText: t("typingText"),
  };
}

export async function getChatRoomContentTexts(): Promise<ChatRoomContentTexts> {
  const [t, chatRoomItemTexts] = await Promise.all([
    getTranslations("components.chat.ChatRoomContentTexts"),
    getChatRoomItemTexts(),
  ]);
  return {
    noRoomsTexts: t("noRoomsTexts"),
    chatRoomItemTexts,
  };
}

export async function getChatRoomsTexts(): Promise<ChatRoomsTexts> {
  const [dataTablePaginationTexts, chatRoomContentTexts, t] = await Promise.all(
    [
      getDataTablePaginationTexts(),
      getChatRoomContentTexts(),
      getTranslations("components.chat.ChatRoomsTexts"),
    ],
  );
  return {
    dataTablePaginationTexts,
    chatRoomContentTexts,
    searchPlaceholder: t("searchPlaceholder"),
    errorText: t("errorText"),
    headerText: t("headerText"),
  };
}

export async function getConversationContentTexts(): Promise<ConversationContentTexts> {
  const [chatMessageFormTexts, t] = await Promise.all([
    getChatMessageFormTexts(),
    getTranslations("components.chat.ConversationContentTexts"),
  ]);
  return {
    chatMessageFormTexts,
    sameChatText: t("sameChatText"),
    typingText: t("typingText"),
  };
}

export async function getConversationTexts(): Promise<ConversationTexts> {
  const [conversationContentPropsTexts, t] = await Promise.all([
    getConversationContentTexts(),
    getTranslations("components.chat.ConversationTexts"),
  ]);
  return { conversationContentPropsTexts, errorText: t("errorText") };
}
