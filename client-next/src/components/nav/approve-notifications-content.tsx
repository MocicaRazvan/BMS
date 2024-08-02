import {
  ApproveModelNotificationResponse,
  ApproveNotificationResponse,
} from "@/types/dto";
import { CheckCheck, MessageCircleIcon, ThumbsUpIcon, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";
import { useRouter } from "@/navigation";
import { Client } from "@stomp/stompjs";
import { useStompClient } from "react-stomp-hooks";
import { formatDistanceToNow, parseISO } from "date-fns";
import { enUS, ro } from "date-fns/locale";
import { useLocale } from "next-intl";
import { ReactNode } from "react";

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

export default function ApproveNotificationContent<
  T extends ApproveModelNotificationResponse,
  I extends ApproveNotificationResponse<T>,
>({ items, itemName, deleteCallback, itemsText }: Props<T, I>) {
  const router = useRouter();
  const locale = useLocale();
  const stompClient = useStompClient();
  return items.map((item, i) => {
    const content = JSON.parse(item.content);
    return (
      <div
        className="grid gap-4 cursor-pointer hover:bg-accent p-2 rounded transition-all hover:shadow-lg hover:scale-[1.02] mb-3"
        key={item?.reference?.appId + i}
        onClick={() => {
          if (stompClient && stompClient?.connected) {
            deleteCallback(item.reference.appId, stompClient);
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
                {itemsText[item.id]?.title || ""}
              </h4>
              <p className="text-xs text-muted-foreground">
                {formatDistanceToNow(parseISO(item?.timestamp || ""), {
                  addSuffix: true,
                  locale: locale === "ro" ? ro : enUS,
                })}
              </p>
            </div>
            <p className="text-sm text-muted-foreground flex items-center justify-start gap-1">
              {itemsText[item.id]?.content || ""}
            </p>
          </div>
        </div>
      </div>
    );
  });
}
