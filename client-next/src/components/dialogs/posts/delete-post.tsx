import { AlertDialogDelete } from "@/components/dialogs/delete-model";
import { PostResponse } from "@/types/dto";

interface Props {
  post: PostResponse;
  token: string | undefined;
  callBack: () => void;
  title: string;
}

export default function AlertDialogDeletePost({
  post,
  token,
  callBack,
  title,
}: Props) {
  return (
    <AlertDialogDelete
      callBack={callBack}
      model={post}
      token={token}
      path="posts"
      title={title}
    />
  );
}
