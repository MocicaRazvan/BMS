import { useEffect, useState } from "react";
import { PostResponse, ResponseWithUserDtoEntity } from "@/types/dto";
import { useParams } from "next/navigation";
import useFetchStream from "@/hoooks/useFetchStream";
import { BaseError } from "@/types/responses";
import { useRouter } from "@/navigation";
import { WithUser } from "@/lib/user";

export function useGetPost({ authUser }: WithUser) {
  const [postState, setPostState] = useState<PostResponse | null>(null);
  const { id } = useParams();
  const router = useRouter();

  const { messages, error, isFinished } = useFetchStream<
    ResponseWithUserDtoEntity<PostResponse>,
    BaseError
  >({ path: `/posts/withUser/${id}`, method: "GET", authToken: true });

  console.log(messages);

  const post = messages[0]?.model?.content;
  const user = messages[0]?.user;
  useEffect(() => {
    if (messages.length > 0) {
      setPostState(messages[0]?.model?.content);
    }
  }, [JSON.stringify(messages)]);

  const isLiked = postState?.userLikes.includes(parseInt(authUser.id));
  const isDisliked = postState?.userDislikes.includes(parseInt(authUser.id));

  return {
    postState,
    setPostState,
    messages,
    error,
    post,
    user,
    router,
    id,
    isFinished,
    isLiked,
    isDisliked,
  };
}
