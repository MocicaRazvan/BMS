"use client";
import { Button } from "@/components/ui/button";
import { signIn } from "next-auth/react";
import Image from "next/image";
import githubWhiteIcon from "@/../public/auth/github-icon-white.svg";
import githubIcon from "@/../public/auth/github-icon.svg";
import googleIcon from "@/../public/auth/google-icon.svg";
import { useTheme } from "next-themes";
import useGetCustomImageBlur from "@/hoooks/images/use-get-custom-image-blur";

export default function OauthProviders() {
  const { theme } = useTheme();
  const { blurData } = useGetCustomImageBlur();
  return (
    <div className="my-10 flex flex-col gap-5 items-center justify-center w-full">
      <div className="flex items-center my-5 w-full gap-4">
        <hr className="flex-grow" />
        <h2 className="text-lg font-bold tracking-tighter">
          {"Try A Provider"}
        </h2>
        <hr className="flex-grow" />
      </div>
      <Button
        size="lg"
        className="px-6 py-8 flex items-center justify-evenly gap-2 w-72"
        variant="outline"
        onClick={() => signIn("github")}
      >
        <Image
          src={theme === "dark" ? githubWhiteIcon : githubIcon}
          alt={"Google logo"}
          placeholder="blur"
          blurDataURL={blurData}
        />
        <p className="text-lg font-bold">{"Use Github"}</p>
      </Button>
      <Button
        variant="outline"
        size="lg"
        className="px-6 py-8 flex items-center justify-evenly gap-2 w-72"
        onClick={() => signIn("custom-google-provider")}
      >
        <Image
          src={googleIcon}
          alt={"Github logo"}
          width={50}
          height={50}
          placeholder="blur"
          blurDataURL={blurData}
        />
        <p className="text-lg font-bold">{"Use Google"}</p>
      </Button>
    </div>
  );
}
