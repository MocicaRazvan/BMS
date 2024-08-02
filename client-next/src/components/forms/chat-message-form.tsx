"use client";
import { Path, useForm } from "react-hook-form";
import {
  conversationMessageSchema,
  ConversationMessageType,
  getTitleBodySchema,
  TitleBodySchemaTexts,
  TitleBodyType,
} from "@/types/forms";
import { zodResolver } from "@hookform/resolvers/zod";
import { useCallback, useMemo, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import Editor from "@/components/editor/editor";
import { useStompClient } from "react-stomp-hooks";
import {
  ChatMessageNotificationBody,
  ConversationUserResponse,
} from "@/types/dto";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import { TitleBodyForm, TitleBodyTexts } from "@/components/forms/title-body";
import ErrorMessage from "@/components/forms/error-message";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import { CornerDownLeft } from "lucide-react";
import * as DOMPurify from "dompurify";

export interface ChatMessageFormTexts {
  titleBodySchemaTexts: TitleBodySchemaTexts;
  titleBodyTexts: TitleBodyTexts;
  errorText: string;
  buttonSubmitTexts: ButtonSubmitTexts;
}

interface ChatMessageFormProps extends ChatMessageFormTexts {
  chatRoomId: number;
  sender: ConversationUserResponse;
  receiver: ConversationUserResponse;
}

export default function ChatMessageForm({
  sender,
  receiver,
  chatRoomId,
  titleBodySchemaTexts,
  titleBodyTexts,
  errorText,
  buttonSubmitTexts,
}: ChatMessageFormProps) {
  const stompClient = useStompClient();
  const [editorKey, setEditorKey] = useState(Math.random());
  // const form = useForm<ConversationMessageType>({
  //   resolver: zodResolver(conversationMessageSchema),
  //   defaultValues: {
  //     content: "",
  //   },
  // });

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

  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();

  //
  // const onSubmit = useCallback(
  //   async ({ content }: ConversationMessageType) => {
  //     form.setValue("content", "");
  //     setEditorKey(Math.random());
  //     if (stompClient && content && stompClient.connected) {
  //       stompClient.publish({
  //         destination: "/app/sendMessage",
  //         body: JSON.stringify({
  //           content,
  //           chatRoomId,
  //           senderEmail: sender.email,
  //           receiverEmail: receiver.email,
  //         }),
  //       });
  //       console.log("sending notification");
  //       console.log("receiverchatId", receiver.connectedChatRoom?.id);
  //       console.log("connectedChatRoom", chatRoomId);
  //       if (
  //         !receiver.connectedChatRoom?.id ||
  //         receiver.connectedChatRoom.id !== chatRoomId
  //       ) {
  //         const body: ChatMessageNotificationBody = {
  //           senderEmail: sender.email,
  //           receiverEmail: receiver.email,
  //           type: "NEW_MESSAGE",
  //           referenceId: chatRoomId,
  //           content: "New message from " + sender.email,
  //           extraLink: "",
  //         };
  //         stompClient.publish({
  //           destination: "/app/chatMessageNotification/sendNotification",
  //           body: JSON.stringify(body),
  //         });
  //       }
  //     }
  //   },
  //   [chatRoomId, form, receiver, sender, stompClient],
  // );
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
    <div className="py-2 border-t flex items-center px-5">
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(onSubmit)}
          className={" w-full h-full"}
        >
          {/*<TitleBodyForm<TitleBodyType>*/}
          {/*  control={form.control}*/}
          {/*  titleBodyTexts={titleBodyTexts}*/}
          {/*  hideTitle={true}*/}
          {/*  editorKey={editorKey}*/}
          {/*/>*/}
          <FormField
            control={form.control}
            name={"body" as Path<TitleBodyType>}
            render={({ field }) => (
              <FormItem>
                {/*<FormLabel className="capitalize">{body}</FormLabel>*/}
                <FormControl>
                  {/* <Textarea placeholder={bodyPlaceholder} {...field} />
                   */}
                  <Editor
                    descritpion={field.value as string}
                    onChange={field.onChange}
                    placeholder={titleBodyTexts.bodyPlaceholder}
                    key={editorKey}
                  />
                </FormControl>
                <FormMessage />
              </FormItem>
            )}
          />
          <ErrorMessage message={errorText} show={!!errorMsg} />
          <ButtonSubmit
            isLoading={isLoading}
            disable={!form.formState.isDirty || body === ""}
            buttonSubmitTexts={{
              ...buttonSubmitTexts,
              submitText: (
                <>
                  <p>{buttonSubmitTexts.submitText} </p>
                  <CornerDownLeft className={"ms-2 size-4"} />{" "}
                </>
              ),
            }}
          />
        </form>
      </Form>
    </div>
  );
}
