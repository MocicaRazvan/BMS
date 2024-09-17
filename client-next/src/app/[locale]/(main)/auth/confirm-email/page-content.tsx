"use client";
import Loader from "@/components/ui/spinner";
import { useSearchParams } from "next/navigation";
import useFetchStream from "@/hoooks/useFetchStream";
import { useRouter } from "@/navigation";
import useClientNotFound from "@/hoooks/useClientNotFound";

export interface ConfirmEmailPageText {
  isLoadingHeader: string;
  isFinishedErrorHeader: string;
}

export default function ConfirmEmailPage({
  isLoadingHeader,
  isFinishedErrorHeader,
}: ConfirmEmailPageText) {
  const searchParams = useSearchParams();
  const token = searchParams.get("token") || "";
  const email = searchParams.get("email") || "";
  const userId = searchParams.get("userId");
  const { navigateToNotFound } = useClientNotFound();
  const router = useRouter();

  const { messages, isFinished, error } = useFetchStream({
    path: "/auth/confirmEmail",
    method: "POST",
    queryParams: { email, token },
    useAbortController: false,
  });

  console.log(messages, isFinished, error);

  if (!token || !email || !userId) {
    return navigateToNotFound();
  }

  if (isFinished && !error) {
    router.replace(`/users/single/${userId}`);
  }

  return (
    <main className="w-full min-h-[calc(100vh-21rem)] flex items-center justify-center transition-all">
      {!isFinished && (
        <div className="w-full h-full flex flex-col  items-center justify-center ">
          <Loader className="mb-5" />
          <h1 className="text-lg bold tracking-ticghter ">{isLoadingHeader}</h1>
        </div>
      )}
      {isFinished && error && (
        <div className="w-full h-full flex flex-col  items-center justify-center ">
          <h1 className="text-2xl bold tracking-ticghter text-destructive">
            {isFinishedErrorHeader}
          </h1>
        </div>
      )}
    </main>
  );
}
