import { ChatMessageNotificationResponse } from "@/types/dto";
import { MessageCircleIcon } from "lucide-react";
import { parseISO, formatDistanceToNow } from "date-fns";
import { Locale } from "@/navigation";
import { ro, enUS } from "date-fns/locale";
import { useLocale } from "next-intl";
import { Button } from "@/components/ui/button";
import { useCallback, useEffect, useState } from "react";
import { useStompClient } from "react-stomp-hooks";
import { getChatMessageNotificationContentTexts } from "@/texts/components/nav";
import { fromDistanceToNowUtc } from "@/lib/utils";

export interface ChatMessageNotificationContentTexts {
  content: string;
}

interface Props {
  notifications: { [p: string]: ChatMessageNotificationResponse[] };
  chatMessageNotificationTexts: Record<
    string,
    ChatMessageNotificationContentTexts
  >;
}

export default function ChatNotificationsContent({
  notifications,
  chatMessageNotificationTexts,
}: Props) {
  const locale = useLocale();
  const stompClient = useStompClient();

  const handleNavigation = useCallback(
    (senderNotif: ChatMessageNotificationResponse) => {
      if (stompClient && stompClient.connected) {
        stompClient.publish({
          destination: "/app/changeRoom",
          body: JSON.stringify({
            chatId: senderNotif.reference.id,
            userEmail: senderNotif.receiver.email,
          }),
        });
      }
    },
    [stompClient?.connected],
  );

  return (
    Object.entries(notifications)
      // .concat(Object.entries(notifications))
      .map(([sender, notif]) => (
        <ChatNotificationItem
          key={sender + notif.length}
          notif={notif}
          sender={sender}
          handleNavigation={handleNavigation}
          locale={locale as Locale}
          texts={chatMessageNotificationTexts[sender]}
        />
      ))
  );
}

interface ItemProps {
  notif: ChatMessageNotificationResponse[];
  sender: string;
  handleNavigation: (senderNotif: ChatMessageNotificationResponse) => void;
  locale: Locale;
  texts: ChatMessageNotificationContentTexts;
}
function ChatNotificationItem({
  notif,
  sender,
  handleNavigation,
  locale,
  texts,
}: ItemProps) {
  // const [texts, setTexts] = useState<ChatMessageNotificationContentTexts>({
  //   content: "",
  // });
  //
  // useEffect(() => {
  //   getChatMessageNotificationContentTexts(notif.length, sender).then(setTexts);
  // }, [notif.length, sender]);

  return (
    <div
      className="grid gap-4 cursor-pointer hover:bg-accent p-2 rounded transition-all hover:shadow-lg hover:scale-[1.02] mb-3 min-h-20"
      onClick={() => handleNavigation(notif[0])}
    >
      <div className="flex items-start gap-3">
        <div className="flex-shrink-0 h-full flex items-center justify-center">
          <Button size="icon" className="rounded-full">
            <MessageCircleIcon className="h-5 w-5" />
          </Button>
        </div>
        <div className="flex-1 space-y-1">
          <div className="flex items-start justify-between">
            <h4 className="text-sm font-semibold">{sender}</h4>
          </div>
          <p className="text-sm text-muted-foreground">{texts?.content}</p>
          <p className="text-xs text-muted-foreground text-wrap text-end w-full min-h-6">
            {fromDistanceToNowUtc(
              parseISO(notif.at(-1)?.timestamp || ""),
              Intl.DateTimeFormat().resolvedOptions().timeZone,
              locale,
            )}
          </p>
        </div>
      </div>
    </div>
  );
}
