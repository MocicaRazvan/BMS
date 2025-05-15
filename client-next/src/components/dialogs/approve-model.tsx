"use client";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { ApproveDto, ResponseWithUserDtoEntity } from "@/types/dto";
import { toast } from "@/components/ui/use-toast";
import { BaseDialogTexts } from "@/components/dialogs/delete-model";
import { memo, ReactNode } from "react";
import { getAlertDialogApproveTexts } from "@/texts/components/dialog";
import { cn, isDeepEqual } from "@/lib/utils";
import { useStompClient } from "react-stomp-hooks";
import { WithUser } from "@/lib/user";
import LoadingDialogAnchor from "@/components/dialogs/loading-dialog-anchor";
import { useClientLRUStore } from "@/lib/client-lru-store";

export interface AlertDialogApproveProps extends WithUser {
  model: ApproveDto;
  path: string;
  title: string;
  callBack: () => void;
  approved: boolean;
  notificationName: string;
  stompExtraLink: string;
}

export interface AlertDialogApproveTexts extends BaseDialogTexts {
  toast: string | ReactNode;
}

const AlertDialogApprove = memo(
  ({
    model,
    authUser,
    callBack,
    path,
    title,
    approved,
    notificationName,
    stompExtraLink,
  }: AlertDialogApproveProps) => {
    const stompClient = useStompClient();

    const dialogApproveTexts = useClientLRUStore({
      setter: () => getAlertDialogApproveTexts(title, (!approved).toString()),
      args: [`alertDialogApproveTexts-${title}-${approved}`],
    });

    const approve = async () => {
      if (!stompClient || !stompClient?.connected) return;
      try {
        const resp = await fetchStream<ResponseWithUserDtoEntity<ApproveDto>>({
          path: `/${path}/admin/approve/${model.id}`,
          method: "PATCH",
          token: authUser.token,
          queryParams: { approved: approved.toString() },
        });

        if (resp.error) {
          //todo better error handling
          console.log(resp.error);
        } else {
          callBack();
          toast({
            // title: model.title,
            description: dialogApproveTexts?.toast,
            variant: approved ? "success" : "destructive",
          });
        }
      } catch (error) {
        console.log(error);
      }
    };

    if (!dialogApproveTexts)
      return <LoadingDialogAnchor className="w-full h-full" />;
    return (
      <AlertDialog>
        <AlertDialogTrigger asChild>
          <Button
            variant="outline"
            className={cn(
              "w-full",
              approved
                ? "border-success text-success"
                : "border-destructive text-destructive",
            )}
          >
            {dialogApproveTexts.anchor}
          </Button>
        </AlertDialogTrigger>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>{dialogApproveTexts.title}</AlertDialogTitle>
            <AlertDialogDescription>
              {dialogApproveTexts.description}
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>{dialogApproveTexts.cancel}</AlertDialogCancel>
            <AlertDialogAction asChild onClick={approve}>
              <Button variant="destructive">
                {dialogApproveTexts.confirm}
              </Button>
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    );
  },
  (
    { callBack: prevCallBack, ...prevProps },
    { callBack: nextCallBack, ...nextProps },
  ) => isDeepEqual(prevProps, nextProps),
);

AlertDialogApprove.displayName = "AlertDialogApprove";

export { AlertDialogApprove };
