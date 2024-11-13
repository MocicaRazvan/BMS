"use client";
import { CustomEntityModel, PostResponse } from "@/types/dto";
import useFetchStream from "@/hoooks/useFetchStream";
import { BaseError } from "@/types/responses";
import LoadingSpinner from "@/components/common/loading-spinner";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { checkOwner } from "@/lib/utils";
import { WithUser } from "@/lib/user";
import PostForm, { PostFormProps } from "@/components/forms/post-form";
import { Option } from "@/components/ui/multiple-selector";

interface Props extends WithUser, PostFormProps {
  postId: string;
}
export default function DuplicatePostPageContent({
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

  const { navigateToNotFound } = useClientNotFound();

  if (!isFinished)
    return (
      <section className="w-full h-full flex items-center justify-center py-12 px-4">
        <LoadingSpinner />
      </section>
    );

  if (error || !messages[0]?.content) {
    return navigateToNotFound();
  }

  const post = messages[0].content;
  checkOwner(authUser, post, navigateToNotFound);

  const tags: Option[] = post.tags.map((tag) => ({ label: tag, value: tag }));

  return (
    <PostForm
      {...props}
      body={post.body}
      title={undefined}
      tags={tags}
      images={post.images}
      authUser={authUser}
    />
  );
}
