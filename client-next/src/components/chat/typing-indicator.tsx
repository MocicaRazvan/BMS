"use client";
import { motion } from "framer-motion";
import { UserDto } from "@/types/dto";
import { Avatar, AvatarImage } from "@/components/ui/avatar";
import noImg from "../../assests/noImage.jpg";

interface Props {
  user: UserDto;
}

const TypingIndicator = ({ user }: Props) => {
  return (
    <div className="flex items-center justify-start gap-2.5">
      <Avatar className="w-12 h-12">
        <AvatarImage
          src={user.image || noImg}
          alt={user.email?.substring(0, 2)}
        />
      </Avatar>
      <div className="flex items-center bg-primary space-x-1 px-1 py-3 w-10 rounded-lg shadow shadow-shadow_color">
        {[0, 1, 2].map((i) => (
          <motion.span
            key={i}
            className="w-2 h-2 bg-background rounded-full"
            animate={{
              opacity: [0.3, 1, 0.3],
              y: [0, -3, 0],
            }}
            transition={{
              duration: 0.6,
              repeat: Infinity,
              delay: i * 0.2,
            }}
          />
        ))}
      </div>
    </div>
  );
};

export default TypingIndicator;
