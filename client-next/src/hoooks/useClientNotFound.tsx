import { useRouter } from "@/navigation/client-navigation";
import { ReactNode, useCallback } from "react";
import LoadingSpinner from "@/components/common/loading-spinner";

export default function useClientNotFound() {
  const router = useRouter();

  const navigateToNotFound: () => ReactNode = useCallback(() => {
    router.replace("/not-found");
    return (
      <section className="w-full min-h-[calc(100vh-12rem)] flex items-center justify-center transition-all">
        <LoadingSpinner />
      </section>
    );
  }, [router]);

  return { navigateToNotFound };
}
