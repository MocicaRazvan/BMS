import { useEffect, useState } from "react";
import {
  PostResponse,
  ResponseWithUserDtoEntity,
  TitleBodyDto,
  TitleBodyImagesDto,
  TitleBodyUserDto,
} from "@/types/dto";
import { useParams } from "next/navigation";
import { useSession } from "next-auth/react";
import useFetchStream from "@/hoooks/useFetchStream";
import { BaseError } from "@/types/responses";
import { useRouter } from "@/navigation";
import { WithUser } from "@/lib/user";

interface Args extends WithUser {
  basePath: string;
  itemId?: number | string;
}

export function useGetTitleBodyUser<T extends TitleBodyUserDto>({
  authUser,
  basePath,
  itemId,
}: Args) {
  const [itemState, setItemState] = useState<T | null>(null);
  const { id } = useParams();
  // const session = useSession();
  const router = useRouter();

  // const authUser = session.data?.user;
  const { messages, error, isFinished } = useFetchStream<
    ResponseWithUserDtoEntity<T>,
    BaseError
  >({
    path: `${basePath}/${itemId || id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
  });

  console.log(messages);

  const item = messages[0]?.model?.content;
  const user = messages[0]?.user;
  useEffect(() => {
    if (messages.length > 0) {
      setItemState(messages[0]?.model?.content);
    }
  }, [JSON.stringify(messages)]);

  const isLiked = itemState?.userLikes.includes(parseInt(authUser.id));
  const isDisliked = itemState?.userDislikes.includes(parseInt(authUser.id));

  return {
    itemState,
    setItemState,
    messages,
    error,
    // authUser,
    item,
    user,
    router,
    id,
    isFinished,
    isLiked,
    isDisliked,
    // session,
  };
}
