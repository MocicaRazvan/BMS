import { useEffect, useMemo, useState } from "react";
import { ResponseWithUserDtoEntity, TitleBodyUserDto } from "@/types/dto";
import { useParams } from "next/navigation";
import useFetchStream from "@/hoooks/useFetchStream";
import { BaseError } from "@/types/responses";
import { useRouter } from "@/navigation/client-navigation";
import { WithUser } from "@/lib/user";

interface Args extends WithUser {
  basePath: string;
  itemId?: number | string;
  useAbortController?: boolean;
  trigger?: boolean;
}

export function useGetTitleBodyUser<T extends TitleBodyUserDto>({
  authUser,
  basePath,
  itemId,
  useAbortController = false,
  trigger = true,
}: Args) {
  const [itemState, setItemState] = useState<T | null>(null);
  const { id } = useParams();
  const router = useRouter();

  const { messages, error, isFinished, isAbsoluteFinished } = useFetchStream<
    ResponseWithUserDtoEntity<T>,
    BaseError
  >({
    path: `${basePath}/${itemId || id}`,
    method: "GET",
    authToken: true,
    useAbortController,
    trigger,
  });

  const item = messages[0]?.model?.content;
  const user = messages[0]?.user;
  useEffect(() => {
    if (messages.length > 0) {
      setItemState(messages[0]?.model?.content);
    }
  }, [messages]);

  const isLiked = useMemo(
    () => itemState?.userLikes.includes(parseInt(authUser.id)),
    [itemState, authUser.id],
  );
  const isDisliked = useMemo(
    () => itemState?.userDislikes.includes(parseInt(authUser.id)),
    [itemState, authUser.id],
  );

  return {
    itemState,
    setItemState,
    messages,
    error,
    item,
    user,
    router,
    id,
    isFinished,
    isLiked,
    isDisliked,
    isAbsoluteFinished,
  };
}
