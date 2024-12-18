"use client";
import { Button } from "@/components/ui/button";
import { signOut } from "next-auth/react";
import { Locale, useRouter } from "@/navigation";

interface SignOutText {
  questionText: string;
  buttonSignOut: string;
  buttonSignIn: string;
}
interface Props extends SignOutText {
  locale: Locale;
}
export default function SignOut({
  buttonSignOut,
  questionText,
  buttonSignIn,
  locale,
}: Props) {
  const router = useRouter();

  return (
    <main className="w-full min-h-[calc(100vh-4rem)] flex items-center justify-center transition-all">
      <div className="border p-10 rounded-xl flex justify-center items-center flex-col gap-10">
        <h1 className="text-2xl font-bold text-center text-destructive">
          {questionText}
        </h1>

        <Button
          variant="destructive"
          className=" px-24 py-5"
          onClick={() => {
            if (window && window?.localStorage) {
              window.localStorage.clear();
            }
            signOut({ redirect: true, callbackUrl: "/auth/signin" }).then(
              () => {
                router.push("/auth/signin");
              },
            );
          }}
        >
          {buttonSignOut}
        </Button>
      </div>
    </main>
  );
}
