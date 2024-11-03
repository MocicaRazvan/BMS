import Loader from "@/components/ui/spinner";
import { cn } from "@/lib/utils";

interface LoadingSpinnerProps {
  sectionClassName?: string;
  loaderClassName?: string;
}

export default function LoadingSpinner({
  sectionClassName,
  loaderClassName,
}: LoadingSpinnerProps) {
  return (
    <section
      className={cn(
        "w-full min-h-[calc(100vh-12rem)] flex items-center justify-center transition-all overflow-hidden z-[1]",
        sectionClassName,
      )}
    >
      <Loader className={cn("w-full", loaderClassName)} />
    </section>
  );
}
