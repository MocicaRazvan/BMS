import { useEffect, useState } from "react";
import { useRouter } from "@/navigation/client-navigation";

export default function useLoadingErrorState() {
  const [isLoading, setIsLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState("");
  const router = useRouter();
  useEffect(() => {
    if (isLoading) {
      setErrorMsg("");
    }
  }, [isLoading]);
  return { isLoading, setIsLoading, errorMsg, setErrorMsg, router };
}
