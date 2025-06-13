import { AlertDialogApprove } from "@/components/dialogs/approve-model";

import { PostResponse } from "@/types/dto";
import { WithUser } from "@/lib/user";
import { approvePostNotificationName } from "@/context/post-approve-notification-context";
import { forwardRef } from "react";

interface Props extends WithUser {
  post: PostResponse;
  callBack: () => void;
}

const AlertDialogApprovePost = forwardRef<HTMLDivElement, Props>(
  ({ post, authUser, callBack }, ref) => {
    return (
      <div ref={ref}>
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
      </div>
    );
  },
);
AlertDialogApprovePost.displayName = "AlertDialogApprovePost";
export default AlertDialogApprovePost;
