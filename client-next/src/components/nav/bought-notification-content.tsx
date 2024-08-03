import { BoughtNotificationResponse } from "@/types/dto";
import { Client } from "@stomp/stompjs";
import { useRouter } from "@/navigation";
import { useLocale } from "next-intl";
import { useStompClient } from "react-stomp-hooks";
import { Button } from "@/components/ui/button";
import { BadgeEuro, CheckCheck, X } from "lucide-react";
import { formatDistanceToNow, parseISO } from "date-fns";
import { enUS, ro } from "date-fns/locale";
import { BoughtPayloadStomp } from "@/context/bought-notification-context";

export interface BoughtNotificationContentTexts {
  title: string;
  content: string;
}

export interface Props {
  items: BoughtNotificationResponse[];
  itemsText: Record<string, BoughtNotificationContentTexts>;
  deleteCallback: (p: BoughtPayloadStomp) => void;
}

export default function BoughtNotificationContent({
  items,
  deleteCallback,
  itemsText,
}: Props) {
  const router = useRouter();
  const locale = useLocale();
  const stompClient = useStompClient();
  return items.map((item, i) => {
    const content = JSON.parse(item?.content);
    return (
      <div
        className="grid gap-4 cursor-pointer hover:bg-accent p-2 rounded transition-all hover:shadow-lg hover:scale-[1.02] mb-3"
        key={item?.id + i}
        onClick={() => {
          // if (stompClient && stompClient?.connected) {
          //   deleteCallback(item.id, stompClient);
          //   if (item?.extraLink) {
          //     router.push("/sales");
          //   }
          // }
          if (stompClient && stompClient?.connected) {
            deleteCallback({ stompClient, payload: item });
          }
        }}
      >
        <div className="flex items-start gap-3">
          <div className="flex-shrink-0 h-full flex items-center justify-center">
            <Button size="icon" className="rounded-full cursor-default	">
              <BadgeEuro className="h-5 w-5 text-success font-bold" />
            </Button>
          </div>
          <div className="flex-1 space-y-1">
            <div className="flex items-center justify-between">
              <h4 className="text-sm font-semibold">
                {itemsText?.[item.id]?.title || ""}
              </h4>
              <p className="text-xs text-muted-foreground">
                {formatDistanceToNow(parseISO(item?.timestamp || ""), {
                  addSuffix: true,
                  locale: locale === "ro" ? ro : enUS,
                })}
              </p>
            </div>
            <p className="text-sm text-muted-foreground flex items-center justify-start gap-1">
              {itemsText?.[item.id]?.content || ""}
            </p>
          </div>
        </div>
      </div>
    );
  });
}
