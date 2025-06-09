"use client";

import React, { Suspense, useCallback } from "react";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { CustomEntityModel, PostResponse } from "@/types/dto";
import LoadingSpinner from "@/components/common/loading-spinner";
import {
  checkApprovePrivilege,
  isSuccessCheckReturn,
  wrapItemToString,
} from "@/lib/utils";
import ElementHeader, {
  ElementHeaderTexts,
} from "@/components/common/element-header";
import CustomImageCarousel from "@/components/common/custom-image-crousel";
import AuthorProfile from "@/components/common/author-profile";
import Loader from "@/components/ui/spinner";
import { PostCommentsTexts } from "@/texts/components/posts";
import PostComments from "@/components/posts/post-comments";
import { useGetTitleBodyUser } from "@/hoooks/useGetTitleBodyUser";
import useClientNotFound from "@/hoooks/useClientNotFound";
import PostRecommendationList, {
  PostRecommendationListTexts,
} from "@/components/recomandation/post-recommendation-list";
import { Separator } from "@/components/ui/separator";
import { AnswerFromBodyFormTexts } from "@/components/forms/answer-from-body-form";
import ItemBodyQa from "@/components/common/item-body-qa";
import useTrackItemView from "@/hoooks/use-track-item-view";
import useFetchStream from "@/hoooks/useFetchStream";
import { useLocale } from "next-intl";
import { Locale } from "@/navigation";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import { estimateReadingTime } from "@/lib/reading-time/estimator";

export interface SinglePostPageTexts {
  elementHeaderTexts: ElementHeaderTexts;
  updateButton: string;
  postCommentsTexts: PostCommentsTexts;
  postRecommendationListTexts: PostRecommendationListTexts;
  answerFromBodyFormTexts: AnswerFromBodyFormTexts;
  numberOfReads: string;
}

interface Props extends SinglePostPageTexts {
  showRecommendations?: boolean;
  trackViews?: boolean;
}

export default function SinglePostPageContent({
  elementHeaderTexts,
  updateButton,
  postCommentsTexts,
  showRecommendations = false,
  postRecommendationListTexts,
  answerFromBodyFormTexts,
  trackViews = false,
  numberOfReads,
}: Props) {
  const { authUser } = useAuthUserMinRole();

  const locale = useLocale() as Locale;
  const {
    itemState: postState,
    setItemState: setPostState,
    messages,
    error,
    // authUser,
    item,
    id,
    user,
    router,
    isFinished,
    isLiked,
    isDisliked,
    // session,
  } = useGetTitleBodyUser<PostResponse>({
    authUser,
    basePath: `/posts/withUser`,
  });

  const {
    messages: viewCount,
    isFinished: isFinishedViewCount,
    error: viewCountError,
  } = useFetchStream<number>({
    path: "/posts/viewCount/" + id,
    authToken: true,
  });

  useTrackItemView(
    `/posts/viewCount/${id}`,
    3000,
    Boolean(
      trackViews &&
        postState?.approved &&
        postState?.userId &&
        wrapItemToString(postState.userId) !== wrapItemToString(authUser.id),
    ),
  );

  const { navigateToNotFound } = useClientNotFound();

  const react = useCallback(
    async (type: "like" | "dislike") => {
      if (!id || !authUser.token) return;
      try {
        const resp = await fetchStream<CustomEntityModel<PostResponse>>({
          path: `/posts/${type}/${id}`,
          method: "PATCH",
          token: authUser.token,
        });
        console.log(resp);
        const newPost = resp.messages[0]?.content;
        setPostState((prev) =>
          !prev
            ? prev
            : {
                ...prev,
                userLikes: newPost.userLikes,
                userDislikes: newPost.userDislikes,
              },
        );
      } catch (error) {
        console.log(error);
      }
    },
    [authUser.token, id, setPostState],
  );
  if (error?.status) {
    return navigateToNotFound();
  }

  if (!isFinished || !postState) {
    console.log("loading main");
    return (
      <section className="w-full min-h-[calc(100vh-4rem)] flex items-center justify-center transition-all">
        <LoadingSpinner />
      </section>
    );
  }

  const privilegeReturn = checkApprovePrivilege(
    authUser,
    postState,
    navigateToNotFound,
  );

  if (React.isValidElement(privilegeReturn)) {
    return privilegeReturn;
  }

  if (!isSuccessCheckReturn(privilegeReturn)) {
    return navigateToNotFound();
  }

  const { isOwnerOrAdmin, isAdmin, isOwner } = privilegeReturn;

  return (
    <section className="w-full mx-auto max-w-[1500px] min-h-[calc(100vh-4rem)] flex-col items-center justify-center transition-all px-1 md:px-6 py-10 relative ">
      <ElementHeader
        elementState={postState}
        react={react}
        isLiked={isLiked}
        isDisliked={isDisliked}
        {...elementHeaderTexts}
        extraContent={
          <p className="text-sm text-muted-foreground font-semibold">
            {estimateReadingTime(postState?.body, 200, locale).text}
          </p>
        }
      />
      <div className="w-3/4 mx-auto flex items-center justify-end">
        {isFinishedViewCount && viewCount.length > 0 && !viewCountError && (
          <p className="text-sm text-muted-foreground font-semibold">
            {`${numberOfReads} ${viewCount[0]}`}
          </p>
        )}
      </div>
      {item?.images.length > 0 && (
        <div className="mt-10">
          <CustomImageCarousel images={item?.images} />
        </div>
      )}
      <div className="mt-20 px-14">
        <ItemBodyQa
          html={item?.body}
          formProps={{ body: item?.body, texts: answerFromBodyFormTexts }}
        />
        <AuthorProfile author={user} />
      </div>
      {postState.approved && (
        <Suspense fallback={<Loader className="w-full h-full" />}>
          <PostComments
            postId={item.id}
            {...postCommentsTexts}
            authUser={authUser}
          />
        </Suspense>
      )}
      {showRecommendations && (
        <>
          <Separator className="my-5 md:my-10 md:mt-14" />
          <PostRecommendationList
            id={item.id}
            {...postRecommendationListTexts}
          />
        </>
      )}
      {/*{isOwnerOrAdmin && (*/}
      {/*  <div className="sticky bottom-0 hover:scale-105 flex items-center px-4 w-fit mx-auto justify-center gap-4 mt-5 bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/70 p-2 rounded-md">*/}
      {/*    <AlertDialogDeletePost*/}
      {/*      post={item}*/}
      {/*      title={item.title}*/}
      {/*      token={authUser.token}*/}
      {/*      callBack={() => {*/}
      {/*        isAdmin*/}
      {/*          ? router.push("/admin/posts")*/}
      {/*          : router.push(`/trainer/user/${authUser.id}/posts`);*/}
      {/*      }}*/}
      {/*    />*/}
      {/*    {isOwner && (*/}
      {/*      <Button*/}
      {/*        onClick={() => {*/}
      {/*          router.push(`/trainer/posts/update/${item.id}`);*/}
      {/*        }}*/}
      {/*      >*/}
      {/*        {updateButton}*/}
      {/*      </Button>*/}
      {/*    )}*/}
      {/*  </div>*/}
      {/*)}*/}
    </section>
  );
}
