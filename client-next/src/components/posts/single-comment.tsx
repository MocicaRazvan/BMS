"use client";
import { memo, useCallback, useMemo, useState } from "react";
import LikesDislikes from "@/components/common/likes-dislikes";
import AlertDialogDeleteComment from "@/components/dialogs/comments/delete-comment";
import { Trash2 } from "lucide-react";
import { Link } from "@/navigation";
import ProseText from "@/components/common/prose-text";
import { CommentResponse, CustomEntityModel } from "@/types/dto";
import { WithUser } from "@/lib/user";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import {
  CommentSchemaTexts,
  CommentSchemaType,
  getCommentSchema,
} from "@/types/forms";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { TitleBodyTexts } from "@/components/forms/title-body";
import ErrorMessage from "@/components/forms/error-message";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import { fetchStream } from "@/hoooks/fetchStream";
import { getToxicity } from "@/actions/toxcity";
import DOMPurify from "dompurify";
import Editor from "@/components/editor/editor";

export interface BaseSingleCommentProps {
  deleteCommentCallback: (commentId: number) => void;
  react: (commentId: number) => (type: "like" | "dislike") => Promise<void>;
  titleBodyTexts: TitleBodyTexts;
  commentSchemaTexts: CommentSchemaTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  errorText: string;
  updateCallback: (
    id: number,
    data: CommentSchemaType,
    updatedAt: string,
  ) => void;
  edited: string;
  editHeader: string;
  deleteCommentDialog: string;
  editCommentLabel: string;
}

interface Props extends WithUser, BaseSingleCommentProps {
  content: CommentResponse;
  userId: number;
  email: string;
  authorText: string;
  englishError: string;
  toxicError: string;
}

export const SingleComment = memo<Props>(
  ({
    content,
    authUser,
    deleteCommentCallback,
    react,
    userId,
    email,
    authorText,
    titleBodyTexts,
    buttonSubmitTexts,
    commentSchemaTexts,
    errorText,
    updateCallback,
    editHeader,
    edited,
    toxicError,
    englishError,
    deleteCommentDialog,
    editCommentLabel,
  }) => {
    const schema = useMemo(
      () => getCommentSchema(commentSchemaTexts),
      [commentSchemaTexts],
    );
    const form = useForm<CommentSchemaType>({
      resolver: zodResolver(schema),
      defaultValues: {
        body: content.body,
        title: content.title,
      },
    });
    const [editMode, setEditMode] = useState("");
    const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
      useLoadingErrorState();

    const onSubmit = useCallback(
      async (data: CommentSchemaType) => {
        setIsLoading(true);
        const trimmedBody = data.body.trim();
        const toxicRes = await getToxicity(
          DOMPurify.sanitize(trimmedBody, {
            ALLOWED_TAGS: [],
            ALLOWED_ATTR: [],
          }),
        );
        if (toxicRes.failure) {
          if (toxicRes.reason.toLowerCase() === "toxicity") {
            form.setError("body", {
              message: toxicError,
            });
          } else {
            form.setError("body", {
              message: englishError,
            });
          }
          setIsLoading(false);

          return;
        }
        try {
          const { messages, error, isFinished } = await fetchStream<
            CustomEntityModel<CommentResponse>
          >({
            path: `/comments/update/${content.id}`,
            method: "PUT",
            body: {
              ...data,
              body: trimmedBody,
            },
            token: authUser.token,
          });
          if (error) {
            if (error.message) {
              setErrorMsg(error.message);
            }
            setErrorMsg(errorText);
          } else {
            if (messages[0]) {
              updateCallback(
                content.id,
                {
                  title: messages[0].content.title,
                  body: messages[0].content.body,
                },
                messages[0].content.updatedAt,
              );
              console.log("messages", messages);
              setEditMode("");
            }
          }
        } catch (error) {
          console.log(error);
        } finally {
          setIsLoading(false);
        }
      },
      [
        authUser.token,
        content.id,
        errorText,
        setErrorMsg,
        setIsLoading,
        updateCallback,
      ],
    );

    return (
      <div key={content.id} className="w-full border rounded-lg px-4 py-6">
        <div className="flex-col items-center">
          <div className="flex w-full items-center justify-between px-2">
            <div className="flex items-center justify-start gap-4">
              {/*<h3 className="font-bold text-lg">{content.title}</h3>*/}
              <LikesDislikes
                react={react(content.id)}
                likes={content.userLikes || []}
                dislikes={content.userDislikes || []}
                isLiked={content.userLikes.includes(
                  parseInt(authUser.id ?? ""),
                )}
                isDisliked={content.userDislikes.includes(
                  parseInt(authUser.id ?? ""),
                )}
                size={"sm"}
              />
            </div>
            <div className="flex items-center justify-center gap-3">
              {content.createdAt !== content.updatedAt && (
                <p className="text-sm text-muted-foreground">{edited}</p>
              )}
              {(parseInt(authUser.id ?? "") === userId ||
                authUser.role === "ROLE_ADMIN") && (
                <AlertDialogDeleteComment
                  anchor={
                    <div className="cursor-pointer rounded-xl border border-transparent hover:border-muted p-2 hover:shadow hover:shadow-shadow_color">
                      <Trash2 />
                    </div>
                  }
                  callBack={() => deleteCommentCallback(content.id)}
                  token={authUser.token ?? ""}
                  comment={content}
                  title={deleteCommentDialog}
                  // title={content.title}
                />
              )}
            </div>
          </div>
          <Link
            href={`/users/${userId}`}
            className="text-sm italic cursor-pointer hover:underline"
          >
            {`${authorText} ${email}`}
          </Link>

          <div className="my-5 px-5">
            <ProseText html={content.body} />
          </div>
          {parseInt(authUser.id ?? "") === userId && (
            <Accordion
              type="single"
              collapsible
              className="w-full"
              value={editMode}
              onValueChange={setEditMode}
            >
              <AccordionItem value="item-edit">
                <AccordionTrigger>{editHeader}</AccordionTrigger>
                <AccordionContent className=" w-full flex items-center justify-center mx-auto ">
                  <Form {...form}>
                    <form
                      onSubmit={form.handleSubmit(onSubmit)}
                      className="space-y-8 w-full px-10 pt-1 lg:space-y-12"
                    >
                      {/*<TitleBodyForm<TitleBodyType>*/}
                      {/*  control={form.control}*/}
                      {/*  titleBodyTexts={{*/}
                      {/*    ...titleBodyTexts,*/}
                      {/*    body: editCommentLabel,*/}
                      {/*  }}*/}
                      {/*  hideTitle={true}*/}
                      {/*/>*/}

                      <FormField
                        control={form.control}
                        name={"body"}
                        render={({ field }) => (
                          <FormItem className="space-y-0">
                            <FormLabel>{editCommentLabel}</FormLabel>
                            <FormControl>
                              {/* <Textarea placeholder={bodyPlaceholder} {...field} />
                               */}
                              <Editor
                                descritpion={field.value as string}
                                onChange={field.onChange}
                                placeholder={titleBodyTexts.bodyPlaceholder}
                              />
                            </FormControl>
                            <FormMessage />
                          </FormItem>
                        )}
                      />
                      <ErrorMessage message={errorText} show={!!errorMsg} />
                      <ButtonSubmit
                        isLoading={isLoading}
                        disable={false}
                        buttonSubmitTexts={buttonSubmitTexts}
                      />
                    </form>
                  </Form>
                </AccordionContent>
              </AccordionItem>
            </Accordion>
          )}
        </div>
      </div>
    );
  },
  (prevProps, nextProps) => {
    // return    isDeepEqual(prevProps, nextProps);
    return JSON.stringify(prevProps) === JSON.stringify(nextProps);
  },
);

SingleComment.displayName = "SingleComment";
export default SingleComment;
