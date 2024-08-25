import { ComponentProps, FC, ReactNode } from "react";
import { Link } from "@/navigation";
import { cn } from "@/lib/utils";

interface ActiveLinkProps extends ComponentProps<typeof Link> {
  children: ReactNode;
  isActive?: boolean;
}

const ActiveLink: FC<ActiveLinkProps> = ({
  href,
  children,
  isActive,
  ...props
}) => {
  return (
    <Link
      href={href}
      {...props}
      className={cn(
        "inline-block relative cursor-pointer text-center hover:scale-[1.03] transition-all group",
        isActive ? "font-bold" : "font-semibold",
        props.className,
      )}
    >
      {children}
      <span
        className={cn(
          "absolute bottom-0 left-0 w-full h-[2px] bg-primary transform transition-transform duration-300 ease-out ",
          {
            "scale-x-0 origin-right": !isActive,
            "scale-x-100 origin-left": isActive,
          },
        )}
      />
    </Link>
  );
};

export default ActiveLink;
