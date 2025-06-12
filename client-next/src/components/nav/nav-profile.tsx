"use client";

import { WithUser } from "@/lib/user";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarImage } from "@/components/ui/avatar";
import { Link } from "@/navigation/navigation";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import noImg from "../../assests/noImage.jpg";
import DaysCalendarCTA, {
  DaysCalendarCTATexts,
} from "@/components/days-calendar/days-calendar-cta";

interface Props extends WithUser {
  dayCalendarCTATexts: DaysCalendarCTATexts;
}

export default function NavProfile({ authUser, dayCalendarCTATexts }: Props) {
  return (
    <DropdownMenu modal={false}>
      <DropdownMenuTrigger asChild>
        {authUser.image ? (
          <Avatar className="cursor-pointer">
            <AvatarImage src={authUser?.image} alt={authUser?.email} />
          </Avatar>
        ) : (
          <p className="cursor-pointer text-sm hover:underline">
            {authUser.email}
          </p>
        )}
      </DropdownMenuTrigger>
      <DropdownMenuContent className="cursor-pointer mt-3 z-10">
        <DropdownMenuItem className="hover:!bg-background">
          <div className="flex flex-col items-center justify-center hover:bg-background">
            <Link href={`/users/single/${authUser?.id}`} className="group">
              <div className="flex items-center justify-center w-full gap-3 cursor-pointer">
                <Avatar className="w-14 h-14 rounded-full">
                  <AvatarImage
                    src={authUser?.image || noImg}
                    alt={authUser?.email}
                  />
                </Avatar>
                <p className="text-lg font-bold max-w-[350px] truncate hover:underline group-hover:underline">
                  {authUser.email}
                </p>
                {authUser.role !== "ROLE_USER" && (
                  <Badge
                    variant={
                      authUser?.role === "ROLE_ADMIN"
                        ? "destructive"
                        : authUser?.role === "ROLE_TRAINER"
                          ? "default"
                          : "secondary"
                    }
                    className="text-sm "
                  >
                    {authUser?.role.split("_")[1] || authUser?.role}
                  </Badge>
                )}
              </div>
            </Link>
            <div className="mb-7 flex items-center justify-center">
              <DaysCalendarCTA
                {...dayCalendarCTATexts}
                className="w-fit text-[16px]"
                size={20}
              />
            </div>
            <div>
              <Button
                size="lg"
                onMouseOver={(e) => e.stopPropagation()}
                asChild
                variant="destructive"
              >
                <Link href="/auth/signout">{"SignOut"}</Link>
              </Button>
            </div>
          </div>
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}
