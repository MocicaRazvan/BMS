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
import { fetchStream } from "@/hoooks/fetchStream";
import {
  ApprovedNotificationType,
  ApproveDto,
  ResponseWithUserDtoEntity,
} from "@/types/dto";
import { toast } from "@/components/ui/use-toast";
import { BaseDialogTexts } from "@/components/dialogs/delete-model";
import { ReactNode, useEffect, useState } from "react";
import { getAlertDialogApproveTexts } from "@/texts/components/dialog";
import { cn } from "@/lib/utils";
import { useStompClient } from "react-stomp-hooks";
import { WithUser } from "@/lib/user";

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

export function AlertDialogApprove({
  model,
  authUser,
  callBack,
  path,
  title,
  approved,
  notificationName,
  stompExtraLink,
}: AlertDialogApproveProps) {
  const stompClient = useStompClient();
  const [dialogApproveTexts, setDialogApproveTexts] =
    useState<AlertDialogApproveTexts | null>(null);
  useEffect(() => {
    getAlertDialogApproveTexts(title, (!approved).toString()).then(
      setDialogApproveTexts,
    );
  }, [approved, title]);
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
        const type: ApprovedNotificationType = approved
          ? "APPROVED"
          : "DISAPPROVED";
        stompClient.publish({
          destination: `/app/${notificationName}/sendNotificationCreateReference/${model.id}`,
          body: JSON.stringify({
            senderEmail: authUser.email,
            receiverEmail: resp.messages[0].user.email,
            type,
            content: JSON.stringify({
              title: model.title,
            }),
            extraLink: stompExtraLink,
          }),
        });
        callBack();
        toast({
          title: model.title,
          description: dialogApproveTexts?.toast,
          variant: approved ? "success" : "destructive",
        });
      }
    } catch (error) {
      console.log(error);
    }
  };

  if (!dialogApproveTexts) return null;
  return (
    <AlertDialog>
      <AlertDialogTrigger asChild>
        <Button
          variant="outline"
          className={cn(
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
            <Button variant="destructive">{dialogApproveTexts.confirm}</Button>
          </AlertDialogAction>
        </AlertDialogFooter>
      </AlertDialogContent>
    </AlertDialog>
  );
}
