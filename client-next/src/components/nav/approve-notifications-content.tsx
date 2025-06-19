import {
  ApproveModelNotificationResponse,
  ApproveNotificationResponse,
} from "@/types/dto";
import { CheckCheck, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { fromDistanceToNowUtc, isDeepEqual } from "@/lib/utils";
import { Locale } from "@/navigation/navigation";
import { Client } from "@stomp/stompjs";
import { useStompClient } from "react-stomp-hooks";
import { parseISO } from "date-fns";
import { useLocale } from "next-intl";
import { memo, ReactNode, useEffect, useRef } from "react";
import { useRouter } from "@/navigation/client-navigation";

export interface ApproveNotificationContentTexts {
  title: ReactNode;
  content: ReactNode;
}

export interface Props<
  T extends ApproveModelNotificationResponse,
  I extends ApproveNotificationResponse<T>,
> {
  items: I[];
  itemName: string;
  itemsText: Record<string, ApproveNotificationContentTexts>;
  deleteCallback: (appId: number, stompClient: Client) => void;
}

function ApproveNotificationContent<
  T extends ApproveModelNotificationResponse,
  I extends ApproveNotificationResponse<T>,
>({ items, itemName, deleteCallback, itemsText }: Props<T, I>) {
  const router = useRouter();
  const locale = useLocale();
  const stompClient = useStompClient();
  const wasPrefetched = useRef(false);

  useEffect(() => {
    if (items.length === 0 || wasPrefetched.current) return;
    const oneLink = items.find((item) => item?.extraLink);
    if (oneLink?.extraLink) {
      router.prefetch(oneLink.extraLink);
      wasPrefetched.current = true;
    }
  }, [items.length, router]);

  return items.map((item, i) => {
    // const content = JSON.parse(item?.content);
    return (
      <div
        className="grid gap-4 cursor-pointer hover:bg-accent p-2 rounded transition-all hover:shadow-lg hover:scale-[1.02] mb-3"
        key={item?.reference?.appId + item?.type + itemName}
        onClick={() => {
          if (stompClient && stompClient?.connected) {
            deleteCallback(item?.reference?.appId, stompClient);
            if (item?.extraLink) {
              router.push(item.extraLink);
            }
          }
        }}
      >
        <div className="flex items-start gap-3">
          <div className="flex-shrink-0 h-full flex items-center justify-center">
            <Button size="icon" className="rounded-full cursor-default	">
              {item?.type === "APPROVED" ? (
                <CheckCheck className="h-5 w-5 text-success font-bold" />
              ) : (
                <X className="h-5 w-5 text-destructive font-bold" />
              )}
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
export default memo(
  ApproveNotificationContent,
  (prevProps, nextProps) =>
    prevProps.itemName === nextProps.itemName &&
    isDeepEqual(prevProps.items, nextProps.items) &&
    isDeepEqual(prevProps.itemsText, nextProps.itemsText) &&
    prevProps.deleteCallback === nextProps.deleteCallback,
);
