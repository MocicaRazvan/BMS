import { cn } from "@/lib/utils";
import { Loader2 } from "lucide-react";

const Loader = ({ className }: { className?: string }) => {
  return (
    <Loader2
      className={cn(
        "my-28 h-18 w-18 text-primary/60 animate-spin font-bold text-lg",
        className,
      )}
      size={46}
    />
  );
};

export default Loader;
