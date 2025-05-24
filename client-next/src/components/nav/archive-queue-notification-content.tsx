"use client";

import { memo } from "react";
import { useArchiveNotifications } from "@/context/archive-notifications-context";
import { isDeepEqual } from "@/lib/utils";
import { containerActionColors } from "@/lib/constants";
import { format } from "date-fns";
import { Button } from "@/components/ui/button";
import { Trash2 } from "lucide-react";
import { WithUser } from "@/lib/user";
import { Link } from "@/navigation";

export const ArchiveQueueNotificationContent = memo(
  ({ authUser }: WithUser) => {
    const { notifications, deleteNotification } =
      useArchiveNotifications(authUser);

    return notifications.map((message) => {
      const messageDate = new Date(message.timestamp);
      return (
        <div
          key={message.id}
          className="grid gap-4 hover:bg-accent p-2 rounded transition-all hover:shadow-lg hover:scale-[1.02] mb-3"
        >
          <div className="flex items-center justify-between gap-10 w-full">
            <div className="flex items-center justify-between w-full">
              <div className="space-y-1.5">
                <Link
                  href="/admin/archiveQueues"
                  className="font-semibold text-[15px] hover:underline"
                >
                  {message.queueName}
                </Link>
                <p
                  className={`text-${containerActionColors[message.action]} font-medium`}
                >
                  {message.action.replace("_", " ")}
                </p>
              </div>

              <div className="space-y-1.5 text-muted-foreground">
                <p className="text-left font-medium">
                  {format(messageDate + "Z", "dd/MM/yyyy")}
                </p>
                <p className="text-left font-medium">
                  {format(messageDate + "Z", "HH:mm:ss")}
                </p>
              </div>
            </div>

            <div>
              <Button
                variant="outline"
                size={"icon"}
                className="border-destructive text-destructive w-10 h-10"
                onClick={() => {
                  deleteNotification(message.id);
                }}
              >
                <Trash2 size={18} />
              </Button>
            </div>
          </div>
        </div>
      );
    });
  },
  isDeepEqual,
);

ArchiveQueueNotificationContent.displayName = "ArchiveQueueNotificationContent";
export default ArchiveQueueNotificationContent;
