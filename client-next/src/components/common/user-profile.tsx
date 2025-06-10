import { Card, CardContent } from "../ui/card";
import { Session } from "next-auth";
import { Avatar, AvatarImage } from "../ui/avatar";
import { Badge } from "../ui/badge";
import { Link } from "@/navigation/navigation";

interface Props {
  user: Session["user"];
}

export default function UserProfile({ user }: Props) {
  return (
    <Card className=" shadow-md rounded-lg overflow-hidden transition-all ease-in-out duration-500 hover:shadow-xl h-[150px]">
      <Link href={`/users/${user?.id}`}>
        <CardContent className="flex flex-row items-center gap-4 p-6">
          <Avatar>
            <AvatarImage
              alt="User profile image"
              className="rounded-full object-cover aspect-square"
              src={user?.image}
            />
          </Avatar>
          <div className="flex flex-col items-start justify-center gap-2">
            <h2 className="text-lg font-semibold">{user?.email}</h2>
            <Badge
              variant={
                user?.role === "ROLE_ADMIN"
                  ? "destructive"
                  : user?.role === "ROLE_TRAINER"
                    ? "default"
                    : "secondary"
              }
              className="text-sm"
            >
              {user?.role}
            </Badge>
          </div>
        </CardContent>
      </Link>
    </Card>
  );
}
