import useFetchStream from "@/hoooks/useFetchStream";
import { CustomEntityModel, UserDto } from "@/types/dto";
import { BaseError } from "@/types/responses";

export default function useGetUser(id: string) {
  const { messages, error, refetch, isFinished } = useFetchStream<
    CustomEntityModel<UserDto>,
    BaseError
  >({
    path: `/users/${id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
  });

  const user = messages[0]?.content;
  return { messages, error, refetch, isFinished, user };
}
