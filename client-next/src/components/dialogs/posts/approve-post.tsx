import { AlertDialogApprove } from "@/components/dialogs/approve-model";

import { PostResponse } from "@/types/dto";
import { WithUser } from "@/lib/user";
import { approvePostNotificationName } from "@/context/post-approve-notification-context";

interface Props extends WithUser {
  post: PostResponse;
  callBack: () => void;
}

export default function AlertDialogApprovePost({
  post,
  authUser,
  callBack,
}: Props) {
  return (
    <AlertDialogApprove
      callBack={callBack}
      model={post}
      authUser={authUser}
      path="posts"
      title={post.title}
      approved={!post.approved}
      notificationName={approvePostNotificationName}
      stompExtraLink={`/trainer/posts/single/${post.id}`}
    />
  );
}
