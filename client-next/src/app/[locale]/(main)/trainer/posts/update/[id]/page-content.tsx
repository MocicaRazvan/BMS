"use client";

import { WithUser } from "@/lib/user";
import PostForm, { PostFormProps } from "@/components/forms/post-form";
import useFetchStream from "@/hoooks/useFetchStream";
import { CustomEntityModel, PostResponse } from "@/types/dto";
import { BaseError } from "@/types/responses";
import LoadingSpinner from "@/components/common/loading-spinner";
import { notFound } from "next/navigation";
import { checkOwner } from "@/lib/utils";
import { Option } from "@/components/ui/multiple-selector";
import { Suspense } from "react";

interface Props extends WithUser, PostFormProps {
  postId: string;
}

export default function UpdatePostPageContent({
  authUser,
  postId,
  ...props
}: Props) {
  const { messages, error, isFinished } = useFetchStream<
    CustomEntityModel<PostResponse>,
    BaseError
  >({
    path: `/posts/${postId}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
  });

  if (!isFinished) return <LoadingSpinner />;

  console.log("HERE", isFinished, messages, error);

  if (error || !messages[0]?.content) {
    console.log("HERE");
    return notFound();
  }

  const post = messages[0].content;
  checkOwner(authUser, post);

  const tags: Option[] = post.tags.map((tag) => ({ label: tag, value: tag }));

  return (
    <main className="flex items-center justify-center px-6 py-10">
      <Suspense fallback={<LoadingSpinner />}>
        <PostForm
          {...props}
          body={post.body}
          title={post.title}
          tags={tags}
          images={post.images}
          authUser={authUser}
        />
      </Suspense>
    </main>
  );
}
