import { BoughtNotificationResponse } from "@/types/dto";
import { Locale } from "@/navigation/navigation";
import { useRouter } from "@/navigation/client-navigation";
import { useStompClient } from "react-stomp-hooks";
import { Button } from "@/components/ui/button";
import { BadgeEuro } from "lucide-react";
import { parseISO } from "date-fns";
import { BoughtPayloadStomp } from "@/context/bought-notification-context";
import { fromDistanceToNowUtc } from "@/lib/utils";
import { useEffect, useRef } from "react";

export interface BoughtNotificationContentTexts {
  title: string;
  content: string;
}

export interface Props {
  items: BoughtNotificationResponse[];
  itemsText: Record<string, BoughtNotificationContentTexts>;
  deleteCallback: (p: BoughtPayloadStomp) => void;
  locale: Locale;
}

export default function BoughtNotificationContent({
  items,
  deleteCallback,
  itemsText,
  locale,
}: Props) {
  const router = useRouter();
  const stompClient = useStompClient();
  const wasPrefetched = useRef(false);

  useEffect(() => {
    if (wasPrefetched.current || items.length === 0) return;
    const oneLink = items.find((item) => item?.extraLink);
    if (oneLink?.reference?.appId) {
      router.prefetch(`/trainer/plans/single/${oneLink.reference.appId}`);
      wasPrefetched.current = true;
    }
  }, [items.length, router]);

  return items.map((item, i) => {
    const content = JSON.parse(item?.content);
    return (
      <div
        className="grid gap-4 cursor-pointer hover:bg-accent p-2 rounded transition-all hover:shadow-lg hover:scale-[1.02] mb-3"
        key={item?.id + i}
        onClick={() => {
          if (stompClient && stompClient?.connected) {
            deleteCallback({ stompClient, payload: item });
            if (item?.reference?.appId) {
              router.push(`/trainer/plans/single/${item.reference.appId}`);
            }
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
                {fromDistanceToNowUtc(
                  parseISO(item?.timestamp || ""),
                  Intl.DateTimeFormat().resolvedOptions().timeZone,
                  locale as Locale,
                )}
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
