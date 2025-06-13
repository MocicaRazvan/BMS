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
import { Button, ButtonProps } from "@/components/ui/button";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { UserDto } from "@/types/dto";
import { WithUser } from "@/lib/user";
import { toast } from "@/components/ui/use-toast";
import { forwardRef, ReactNode, useCallback, useEffect, useState } from "react";
import { getAlertDialogMakeTrainerTexts } from "@/texts/components/dialog";

interface Props extends WithUser {
  user: UserDto;
  successCallback: () => void;
  buttonProps?: ButtonProps;
  anchorText?: string;
}

export interface AlertDialogMakeTrainerTexts {
  anchor: string;
  title: string;
  description: string;
  toast: ReactNode;
  cancel: string;
  confirm: string;
}

const AlertDialogMakeTrainer = forwardRef<HTMLDivElement, Props>(
  ({ user, authUser, successCallback, buttonProps, anchorText }, ref) => {
    const [texts, setTexts] = useState<AlertDialogMakeTrainerTexts | null>(
      null,
    );

    useEffect(() => {
      getAlertDialogMakeTrainerTexts(user.email).then(setTexts);
    }, [user.email]);

    const makeTrainer = useCallback(async () => {
      if (!texts) return;
      try {
        const resp = await fetchStream({
          path: `/users/admin/${user.id}`,
          method: "PATCH",
          token: authUser.token,
        });

        if (resp.error) {
          console.log(resp.error);
          toast({
            title: user.email,
            description: "Error",
            variant: "destructive",
          });
        } else {
          successCallback();
          toast({
            title: user.email,
            description: texts.toast,
            variant: "success",
          });
        }
      } catch (error) {
        console.log(error);
      }
    }, [authUser.token, successCallback, texts, user.email, user.id]);

    return (
      <div ref={ref}>
        <AlertDialog>
          <AlertDialogTrigger asChild>
            <Button
              variant="destructive"
              size="lg"
              className="font-bold text-lg w-full"
              {...buttonProps}
            >
              {texts?.anchor || anchorText}
            </Button>
          </AlertDialogTrigger>
          <AlertDialogContent>
            {texts && (
              <>
                <AlertDialogHeader>
                  <AlertDialogTitle>{texts.title}</AlertDialogTitle>
                  <AlertDialogDescription>
                    {texts.description}
                  </AlertDialogDescription>
                </AlertDialogHeader>
                <AlertDialogFooter>
                  <AlertDialogCancel>{texts.cancel}</AlertDialogCancel>
                  <AlertDialogAction asChild onClick={makeTrainer}>
                    <Button variant="destructive">{texts.confirm}</Button>
                  </AlertDialogAction>
                </AlertDialogFooter>
              </>
            )}
          </AlertDialogContent>
        </AlertDialog>
      </div>
    );
  },
);
AlertDialogMakeTrainer.displayName = "AlertDialogMakeTrainer";
export { AlertDialogMakeTrainer };
