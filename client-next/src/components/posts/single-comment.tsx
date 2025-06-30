"use client";
import { memo, useState } from "react";
import LikesDislikes from "@/components/common/likes-dislikes";
import AlertDialogDeleteComment from "@/components/dialogs/comments/delete-comment";
import { Trash2 } from "lucide-react";
import { Link } from "@/navigation/navigation";
import ProseText from "@/components/common/prose-text";
import { CommentResponse } from "@/types/dto";
import { WithUser } from "@/lib/user";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import { CommentSchemaTexts, CommentSchemaType } from "@/types/forms";

import { TitleBodyTexts } from "@/components/forms/title-body";
import { ButtonSubmitTexts } from "@/components/forms/button-submit";
import { EditorTexts } from "@/components/editor/editor";
import { Skeleton } from "@/components/ui/skeleton";
import dynamicWithPreload from "@/lib/dynamic-with-preload";
import usePreloadDynamicComponents from "@/hoooks/use-prelod-dynamic-components";
import { isDeepEqual } from "@/lib/utils";

const DynamicUpdateCommentForm = dynamicWithPreload(
  () => import("@/components/forms/update-comment-form"),
  {
    loading: () => <Skeleton className="min-h-[calc(400px+6rem)] w-full " />,
  },
);

export interface BaseSingleCommentProps {
  deleteCommentCallback: (commentId: number) => void;
  react: (commentId: number) => (type: "like" | "dislike") => Promise<void>;
  titleBodyTexts: TitleBodyTexts;
  commentSchemaTexts: CommentSchemaTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  editorTexts: EditorTexts;
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
    editorTexts,
  }) => {
    usePreloadDynamicComponents(
      DynamicUpdateCommentForm,
      parseInt(authUser.id ?? "") === userId,
    );

    const [editMode, setEditMode] = useState("");

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
              {/*{content.createdAt !== content.updatedAt && (*/}
              {/*  <p className="text-sm text-muted-foreground">{edited}</p>*/}
              {/*)}*/}
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
            href={`/users/single/${userId}`}
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
                <AccordionContent className="w-full flex items-center justify-center mx-auto h-[510px] md:h-[480px]">
                  <DynamicUpdateCommentForm
                    titleBodyTexts={titleBodyTexts}
                    commentSchemaTexts={commentSchemaTexts}
                    buttonSubmitTexts={buttonSubmitTexts}
                    editorTexts={editorTexts}
                    content={content}
                    edited={edited}
                    editHeader={editHeader}
                    deleteCommentDialog={deleteCommentDialog}
                    editCommentLabel={editCommentLabel}
                    updateCallback={updateCallback}
                    englishError={englishError}
                    toxicError={toxicError}
                    errorText={errorText}
                    setEditMode={setEditMode}
                    authUser={authUser}
                  />
                </AccordionContent>
              </AccordionItem>
            </Accordion>
          )}
        </div>
      </div>
    );
  },
  (
    {
      updateCallback: updatePrev,
      deleteCommentCallback: deletePrev,
      react: reactPrev,
      ...restPrev
    },
    {
      updateCallback: updateNext,
      deleteCommentCallback: deleteNext,
      react: reactNext,
      ...restNext
    },
  ) =>
    isDeepEqual(restPrev, restNext) &&
    updatePrev === updateNext &&
    deletePrev === deleteNext &&
    reactPrev === reactNext,
);

SingleComment.displayName = "SingleComment";
export default SingleComment;
