import { UserDto } from "@/types/dto";
import { HTMLAttributes } from "react";
import { Link } from "@/navigation";
import { Avatar, AvatarImage } from "@/components/ui/avatar";

interface Props extends HTMLAttributes<HTMLDivElement> {
  author: UserDto;
}

export default function AuthorProfile({ author, ...props }: Props) {
  return (
    <div {...props}>
      <Link href={`/users/single/${author.id}`}>
        <div className="border flex items-center justify-center mt-16 gap-2 cursor-pointer px-4 py-2 hover:shadow-md  hover:shadow-shadow_color rounded-lg transition-all md:w-1/3 mx-auto group group-hover:scale-105 duration-300">
          <Avatar className="group-hover:translate-y-[-10px] duration-200">
            <AvatarImage
              src={author.image}
              alt={author.email}
              className="w-16 h-16"
            />
          </Avatar>
          <p className="group-hover:translate-y-[-10px] duration-200 group-hover:underline">
            {author.email}
          </p>
        </div>
      </Link>
    </div>
  );
}
