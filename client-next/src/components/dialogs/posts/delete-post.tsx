import { AlertDialogDelete } from "@/components/dialogs/delete-model";
import { PostResponse } from "@/types/dto";
import { forwardRef } from "react";

interface Props {
  post: PostResponse;
  token: string | undefined;
  callBack: () => void;
  title: string;
}

const AlertDialogDeletePost = forwardRef<HTMLDivElement, Props>(
  ({ post, token, callBack, title }, ref) => {
    return (
      <div ref={ref}>
        <AlertDialogDelete
          callBack={callBack}
          model={post}
          token={token}
          path="posts"
          title={title}
        />
      </div>
    );
  },
);
AlertDialogDeletePost.displayName = "AlertDialogDeletePost";
export default AlertDialogDeletePost;
