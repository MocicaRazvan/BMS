"use client";

import { Button } from "@/components/ui/button";
import Loader from "@/components/ui/spinner";
import { fetchStream } from "@/hoooks/fetchStream";
import useFetchStream from "@/hoooks/useFetchStream";
import {
  CommentResponse,
  CustomEntityModel,
  PageableResponse,
  ResponseWithUserDtoEntity,
} from "@/types/dto";
import { useCallback, useEffect, useState } from "react";
import { X } from "lucide-react";

import { WithUser } from "@/lib/user";
import CommentAccordion from "@/components/posts/comment-accordion";
import { PostCommentsTexts } from "@/texts/components/posts";
import CommentsList from "@/components/posts/comments-list";
import { SortDirection } from "@/types/fetch-utils";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { TitleBodyType } from "@/types/forms";
import { format } from "date-fns";

interface Props extends WithUser, PostCommentsTexts {
  postId: number;
}

export default function PostComments({
  postId,
  authUser,
  commentsFormTexts,
  loadMore,
  author,
  oldest,
  newest,
  sortLabel,
  edited,
  editHeader,
  commentAccordionTexts,
  deleteCommentDialog,
  editCommentLabel,
}: Props) {
  const pageSize = 10;
  const [comments, setComments] = useState<
    ResponseWithUserDtoEntity<CommentResponse>[]
  >([]);
  const [totalComments, setTotalComments] = useState<number>(0);
  const [page, setPage] = useState<number>(0);
  const [createdAtSort, setCreatedAtSort] =
    useState<Exclude<SortDirection, "none">>("desc");

  const { messages, error, isFinished, refetch } = useFetchStream<
    PageableResponse<ResponseWithUserDtoEntity<CommentResponse>>
  >({
    path: `/comments/withUser/post/${postId}`,
    method: "PATCH",
    authToken: true,
    body: {
      page,
      size: pageSize,
      sortingCriteria: { createdAt: createdAtSort },
    },
  });

  const refetchComments = useCallback(() => {
    setComments([]);
    setPage(0);
    setTotalComments(0);
    setCreatedAtSort("desc");
    refetch();
  }, [refetch]);

  useEffect(() => {
    if (messages.length > 0) {
      if (page == 0) {
        setTotalComments(messages[0].pageInfo.totalElements);
        setComments(messages.map((m) => m.content));
      } else {
        setComments((prev) => [...prev, ...messages.map((m) => m.content)]);
      }
    }
  }, [JSON.stringify(messages)]);

  const isMore = pageSize * (page + 1) < totalComments;

  const react = useCallback(
    (commentId: number) => async (type: "like" | "dislike") => {
      try {
        const resp = await fetchStream<CustomEntityModel<CommentResponse>>({
          path: `/comments/${type}/${commentId}`,
          method: "PATCH",
          token: authUser.token,
        });

        if (resp.error?.status) {
          console.log(resp.error);
          return;
        }
        const newComment = resp.messages[0]?.content;
        console.log(newComment);

        setComments((prev) =>
          prev.map((c) =>
            c.model.content.id === newComment.id
              ? {
                  ...c,
                  model: {
                    ...c.model,
                    content: {
                      ...c.model.content,
                      userLikes: newComment.userLikes,
                      userDislikes: newComment.userDislikes,
                    },
                  },
                }
              : c,
          ),
        );
      } catch (error) {
        console.log(error);
      }
    },

    [authUser.token],
  );

  const deleteCommentCallback = useCallback(
    (commentId: number) => {
      // setComments((prev) => prev.filter((c) => c.model.content.id !== commentId));
      // setTotalComments((prev) => --prev);
      refetchComments();
    },
    [refetchComments],
  );

  const updateCommentCallback = useCallback(
    (id: number, data: TitleBodyType, updatedAt: string) => {
      setComments((prev) =>
        prev.map((c) =>
          c.model.content.id !== id
            ? c
            : {
                ...c,
                model: {
                  content: {
                    ...c.model.content,
                    ...data,
                    updatedAt,
                  },
                },
              },
        ),
      );
    },
    [],
  );

  return (
    <div className=" mb-40 flex items-center justify-center flex-col gap-4 transition-all max-w-3xl w-full  mx-auto mt-20">
      <div className="mb-10 w-full">
        <CommentAccordion
          postId={postId}
          token={authUser.token}
          refetch={refetchComments}
          {...commentAccordionTexts}
        />
      </div>
      {/*{comments.map(({ model: { content }, user: { email, id: userId } }) => (*/}
      {/*  <div key={content.id} className="w-full">*/}
      {/*    <SingleComment*/}
      {/*      content={content}*/}
      {/*      deleteCommentCallback={deleteCommentCallback}*/}
      {/*      userId={userId}*/}
      {/*      react={react}*/}
      {/*      email={email}*/}
      {/*      authorText={author}*/}
      {/*      authUser={authUser}*/}
      {/*    />*/}
      {/*  </div>*/}
      {/*))}*/}
      {isFinished && comments.length > 0 && (
        <div className="flex items-center justify-start w-full">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline">{`${sortLabel} (${createdAtSort === "desc" ? newest : oldest})`}</Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent className="w-56">
              <DropdownMenuRadioGroup
                value={createdAtSort}
                onValueChange={(val) => {
                  setPage(0);
                  setCreatedAtSort(val as Exclude<SortDirection, "none">);
                }}
              >
                <DropdownMenuRadioItem value={"asc"}>
                  {oldest}
                </DropdownMenuRadioItem>
                <DropdownMenuRadioItem value={"desc"}>
                  {newest}
                </DropdownMenuRadioItem>
              </DropdownMenuRadioGroup>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      )}
      {isFinished && (
        <CommentsList
          comments={comments}
          deleteCommentCallback={deleteCommentCallback}
          react={react}
          author={author}
          authUser={authUser}
          {...commentsFormTexts}
          updateCallback={updateCommentCallback}
          errorText={"update error text"}
          edited={edited}
          editHeader={editHeader}
          englishError={commentAccordionTexts.englishError}
          toxicError={commentAccordionTexts.toxicError}
          deleteCommentDialog={deleteCommentDialog}
          editCommentLabel={editCommentLabel}
        />
      )}
      {!isFinished && <Loader />}
      {isMore && (
        <Button onClick={() => setPage((prev) => ++prev)}>{loadMore}</Button>
      )}
    </div>
  );
}
