"use client";
import { Path, useForm } from "react-hook-form";
import {
  getTitleBodySchema,
  TitleBodySchemaTexts,
  TitleBodyType,
} from "@/types/forms";
import { zodResolver } from "@hookform/resolvers/zod";
import { memo, useCallback, useMemo, useState } from "react";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormMessage,
} from "@/components/ui/form";
import Editor, { EditorTexts } from "@/components/editor/editor";
import { useStompClient } from "react-stomp-hooks";
import {
  ChatMessageNotificationBody,
  ConversationUserResponse,
} from "@/types/dto";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import { TitleBodyTexts } from "@/components/forms/title-body";
import ErrorMessage from "@/components/forms/error-message";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import { CornerDownLeft } from "lucide-react";
import DOMPurify from "dompurify";
import { cn } from "@/lib/utils";

export interface ChatMessageFormTexts {
  titleBodySchemaTexts: TitleBodySchemaTexts;
  titleBodyTexts: TitleBodyTexts;
  errorText: string;
  buttonSubmitTexts: ButtonSubmitTexts;
  editorTexts: EditorTexts;
  buttonText: string;
}

interface ChatMessageFormProps extends ChatMessageFormTexts {
  chatRoomId: number;
  sender: ConversationUserResponse;
  receiver: ConversationUserResponse;
  onValueChange?: (value: string) => void;
  wrapperClassName?: string;
}

const ChatMessageForm = memo(
  ({
    sender,
    receiver,
    chatRoomId,
    titleBodySchemaTexts,
    titleBodyTexts,
    errorText,
    buttonSubmitTexts,
    editorTexts,
    onValueChange,
    buttonText,
    wrapperClassName = "",
  }: ChatMessageFormProps) => {
    const stompClient = useStompClient();
    const [editorKey, setEditorKey] = useState(Math.random());

    const schema = useMemo(
      () => getTitleBodySchema(titleBodySchemaTexts),
      [titleBodySchemaTexts],
    );

    const form = useForm<TitleBodyType>({
      resolver: zodResolver(schema),
      defaultValues: {
        body: "",
        title: "placeholder",
      },
    });

    const { isLoading, setIsLoading, errorMsg, setErrorMsg } =
      useLoadingErrorState();

    const onSubmit = useCallback(
      async ({ body }: TitleBodyType) => {
        setIsLoading(true);
        form.setValue("body", "");
        setEditorKey(Math.random());
        try {
          if (stompClient && body && stompClient.connected) {
            stompClient.publish({
              destination: "/app/sendMessage",
              body: JSON.stringify({
                content: body,
                chatRoomId,
                senderEmail: sender.email,
                receiverEmail: receiver.email,
              }),
            });
            if (
              !receiver.connectedChatRoom?.id ||
              receiver.connectedChatRoom.id !== chatRoomId
            ) {
              const body: ChatMessageNotificationBody = {
                senderEmail: sender.email,
                receiverEmail: receiver.email,
                type: "NEW_MESSAGE",
                referenceId: chatRoomId,
                content: "New message from " + sender.email,
                extraLink: "",
              };
              stompClient.publish({
                destination: "/app/chatMessageNotification/sendNotification",
                body: JSON.stringify(body),
              });
            }
          }
        } catch (e) {
          setErrorMsg(errorText);
        } finally {
          setIsLoading(false);
        }
      },
      [
        chatRoomId,
        form,
        receiver?.connectedChatRoom?.id,
        receiver.email,
        sender.email,
        setIsLoading,
        stompClient?.connected,
      ],
    );

    const body = DOMPurify.sanitize(form.watch("body"), {
      ALLOWED_TAGS: [],
      ALLOWED_ATTR: [],
    }).trim();

    return (
      <div
        className={cn(
          " border-t flex items-center pb-5 md:pb-0 pt-3 md:px-1",
          wrapperClassName,
        )}
      >
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className={
              " w-full h-full flex flex-col md:flex-row items-center justify-between gap-5"
            }
          >
            <FormField
              control={form.control}
              name={"body" as Path<TitleBodyType>}
              render={({ field }) => (
                <FormItem className="flex-1 w-full max-h-[300px] ">
                  <FormControl>
                    <Editor
                      editorContentWrapperClassname="overflow-y-auto max-h-[150px]"
                      descritpion={field.value as string}
                      onChange={(e) => {
                        onValueChange?.(e);
                        field.onChange(e);
                      }}
                      placeholder={titleBodyTexts.bodyPlaceholder}
                      key={editorKey}
                      texts={editorTexts}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <ErrorMessage message={errorText} show={!!errorMsg} />
            <div className="flex items-center justify-center h-full">
              <ButtonSubmit
                isLoading={isLoading}
                disable={!form.formState.isDirty || body === ""}
                size="sm"
                buttonSubmitTexts={{
                  ...buttonSubmitTexts,
                  submitText: (
                    <>
                      <p>{buttonText} </p>
                      <CornerDownLeft className={"ms-2 size-4"} />
                    </>
                  ),
                }}
              />
            </div>
          </form>
        </Form>
      </div>
    );
  },
);

ChatMessageForm.displayName = "ChatMessageForm";
export default ChatMessageForm;
