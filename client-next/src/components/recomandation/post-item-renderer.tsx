import { PostReposeWithSimilarity } from "@/types/dto";
import { Link } from "@/navigation/navigation";

export interface PostItemRendererTexts {
  likes: string;
  dislikes: string;
}
interface Props {
  item: PostReposeWithSimilarity;
  texts: PostItemRendererTexts;
}

export default function PostItemRenderer({ item, texts }: Props) {
  return (
    <div className="w-full h-full space-y-5">
      <div className="flex items-center justify-between w-full">
        <Link
          href={`/posts/single/${item.id}`}
          className="font-bold text-lg hover:underline max-w-44 xl:max-w-64 2xl:max-w-72 text-nowrap overflow-x-hidden text-ellipsis"
        >
          {item.title}
        </Link>
        <span className="font-bold"></span>
      </div>
      <div className="flex items-center justify-between w-full">
        <div className="flex  items-start justify-center gap-2">
          <span className="font-bold">{texts.likes}</span>
          <span>{item.userLikes.length}</span>
        </div>
        <div className="flex  items-start justify-center gap-2">
          <span className="font-bold">{texts.dislikes}</span>
          <span>{item.userDislikes.length}</span>
        </div>
      </div>
    </div>
  );
}
