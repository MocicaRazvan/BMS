"use client";
import { Button } from "@/components/ui/button";
import { signOut } from "next-auth/react";
import { useRouter } from "@/navigation";

interface SignOutText {
  questionText: string;
  buttonSignOut: string;
  buttonSignIn: string;
}

export default function SignOut({
  buttonSignOut,
  questionText,
  buttonSignIn,
}: SignOutText) {
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
            signOut({ redirect: false, callbackUrl: "/auth/signin" }).then(() =>
              router.push("/auth/signin"),
            );
          }}
        >
          {buttonSignOut}
        </Button>
        {/*<Link href="/auth/signin" className="italic hover:underline">*/}
        {/*  {buttonSignIn}*/}
        {/*</Link>*/}
      </div>
    </main>
  );
}
