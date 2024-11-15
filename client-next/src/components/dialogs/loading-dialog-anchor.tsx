import { Skeleton } from "@/components/ui/skeleton";

export default function LoadingDialogAnchor() {
  return (
    <div className="h-12 w-full p-2 flex items-center justify-start">
      <Skeleton className="w-2/3 h-full" />
    </div>
  );
}
