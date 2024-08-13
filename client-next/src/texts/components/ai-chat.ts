import { AiChatBoxTexts } from "@/components/ai-chat/ai-chat-box";
import { getTranslations } from "next-intl/server";

export async function getAiChatBoxTexts(): Promise<AiChatBoxTexts> {
  const t = await getTranslations("components.ai-chat.AiChatBoxTexts");
  return {
    emptyContent: t("emptyContent"),
    emptyHeader: t("emptyHeader"),
    errorContent: t("errorContent"),
    inputPlaceholder: t("inputPlaceholder"),
    loadingContent: t("loadingContent"),
  };
}
