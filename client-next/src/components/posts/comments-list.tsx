import SingleComment, {
  BaseSingleCommentProps,
} from "@/components/posts/single-comment";
import { WithUser } from "@/lib/user";
import { CommentResponse, ResponseWithUserDtoEntity } from "@/types/dto";

interface Props extends WithUser, BaseSingleCommentProps {
  comments: ResponseWithUserDtoEntity<CommentResponse>[];
  author: string;
  englishError: string;
  toxicError: string;
}

export default function CommentsList({
  comments,
  deleteCommentCallback,
  author,
  react,
  authUser,
  ...props
}: Props) {
  return comments.map(({ model: { content }, user: { email, id: userId } }) => (
    <div key={content.id} className="w-full">
      <SingleComment
        content={content}
        deleteCommentCallback={deleteCommentCallback}
        userId={userId}
        react={react}
        email={email}
        authorText={author}
        authUser={authUser}
        {...props}
      />
    </div>
  ));
}
