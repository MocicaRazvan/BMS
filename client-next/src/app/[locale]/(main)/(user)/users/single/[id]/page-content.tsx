"use client";

import { WithUser } from "@/lib/user";
import useFetchStream from "@/hoooks/useFetchStream";
import { CustomEntityModel, UserDto } from "@/types/dto";
import { BaseError } from "@/types/responses";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Avatar, AvatarImage } from "@/components/ui/avatar";
import noImg from "@/../public/noImage.jpg";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { AlertDialogMakeTrainer } from "@/components/dialogs/user/make-trainer-alert";
import {
  Accordion,
  AccordionContent,
  AccordionItem,
  AccordionTrigger,
} from "@/components/ui/accordion";
import UpdateProfile from "@/components/forms/update-profile";
import { useCallback, useState } from "react";
import { cn } from "@/lib/utils";
import { Loader2 } from "lucide-react";
import { fetchStream } from "@/hoooks/fetchStream";
import { useRouter } from "@/navigation";
import { UpdateProfileTexts } from "@/texts/components/forms";
import { useStompClient } from "react-stomp-hooks";
import useClientNotFound from "@/hoooks/useClientNotFound";

export interface UserPageTexts {
  updateProfileTexts: UpdateProfileTexts;
  ownerTitle: string;
  visitorTitle: string;
  firstNameLabel: string;
  lastNameLabel: string;
  editProfile: string;
  verifyEmail: string;
  changePassword: string;
  startChat: string;
  errorText: string;
  emailSent: string;
}

interface Props extends WithUser, UserPageTexts {
  id: string;
}

export default function UserPageContent({
  authUser,
  id,
  updateProfileTexts,
  editProfile,
  changePassword,
  firstNameLabel,
  lastNameLabel,
  startChat,
  ownerTitle,
  visitorTitle,
  verifyEmail,
  emailSent,
  errorText,
}: Props) {
  const router = useRouter();
  const stompClient = useStompClient();
  const [userState, setUserState] = useState<typeof authUser>(authUser);
  const { navigateToNotFound } = useClientNotFound();
  const { messages, error, refetch, isFinished } = useFetchStream<
    CustomEntityModel<UserDto>,
    BaseError
  >({
    path: `/users/${id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
  });

  const [respMsg, setRespMsg] = useState<{
    message: string;
    isError: boolean;
    isVisible: boolean;
    isLoading: boolean;
  }>({
    message: "",
    isError: false,
    isVisible: false,
    isLoading: false,
  });

  const handleVerify = useCallback(
    async (email: string) => {
      setRespMsg((prev) => ({ ...prev, isLoading: true }));
      const { messages, error } = await fetchStream({
        path: "/auth/verifyEmail",
        method: "POST",
        body: {
          email,
        },
      });
      if (error) {
        setRespMsg({
          message: errorText,
          isError: true,
          isVisible: true,
          isLoading: false,
        });
      } else {
        setRespMsg({
          message: emailSent,
          isError: false,
          isVisible: true,
          isLoading: false,
        });
      }
      setTimeout(
        () =>
          setRespMsg({
            message: "",
            isError: false,
            isVisible: false,
            isLoading: false,
          }),
        5500,
      );
      setRespMsg((prev) => ({ ...prev, isLoading: false }));
    },
    [emailSent, errorText],
  );

  const handleStartChat = useCallback(
    (recUser: UserDto) => {
      if (stompClient && stompClient?.connected && recUser && authUser) {
        stompClient?.publish({
          destination: "/app/addChatRoom",
          body: JSON.stringify({
            users: [
              { email: recUser.email, connectedStatus: "OFFLINE" },
              {
                email: authUser.email,
                connectedStatus: "ONLINE",
              },
            ],
          }),
        });
        router.push(`/chat?email=${recUser.email}`);
      }
    },
    [stompClient?.connected, authUser, router],
  );

  if (!isFinished) return <LoadingSpinner />;
  if (isFinished && messages.length === 0 && error?.status)
    return navigateToNotFound();
  const user = messages[0].content;

  const isOwner = authUser.email === user.email;
  const isUser = user.role === "ROLE_USER";
  const isAuthAdmin = authUser.role === "ROLE_ADMIN";

  return (
    <section className="w-full min-h-[calc(100vh-4rem)] pb-14 flex items-center justify-center transition-all mt-4">
      <div className=" w-full mx-2 md:mx-0 md:w-2/3  border rounded-xl px-6 py-8 space-y-8 lg:space-y-12">
        <h1 className="text-2xl lg:text-4xl tracking-tighter font-bold text-center mb-12">
          {isOwner ? ownerTitle : visitorTitle}
        </h1>
        <div className="flex items-center justify-center gap-4">
          <Avatar className="w-16 h-16">
            <AvatarImage src={user?.image || noImg} alt={user?.email} />
          </Avatar>

          <h3 className="text-lg lg:text-xl font-bold text-center">
            {user?.email}
          </h3>
        </div>
        <div className="flex justify-between w-full mx-auto items-center mt-10 flex-wrap">
          <div className="flex flex-col md:flex-row items-center justify-around gap-2 w-full ">
            <p className="text-lg">
              {firstNameLabel}
              <span className="font-bold ml-4">
                {user.firstName || "Not Completed"}
              </span>
            </p>
            <p className="text-lg ">
              {lastNameLabel}
              <span className="font-bold ml-4">
                {user.lastName || "Not Completed"}
              </span>
            </p>
          </div>
        </div>
        {user.role !== "ROLE_USER" && (
          <div className="mx-auto flex items-center justify-center">
            <Badge
              variant={
                user.role === "ROLE_ADMIN"
                  ? "destructive"
                  : user.role === "ROLE_TRAINER"
                    ? "default"
                    : "secondary"
              }
              className="text-lg"
            >
              {user.role.split("_")[1] || user.role}
            </Badge>
          </div>
        )}
        {isAuthAdmin && isUser && (
          <div className="flex w-1/3 mx-auto items-center justify-center">
            <AlertDialogMakeTrainer
              user={user}
              authUser={authUser}
              successCallback={() => {
                refetch();
                router.refresh();
              }}
            />
          </div>
        )}
        {!isOwner && (
          <div className={"mt-8 flex items-center justify-center"}>
            <Button onClick={() => handleStartChat(user)}>{startChat}</Button>
          </div>
        )}
        {isOwner && (
          <Accordion type="single" collapsible className="w-full">
            <AccordionItem value="item-1">
              <AccordionTrigger>{editProfile}</AccordionTrigger>
              <AccordionContent>
                <UpdateProfile
                  authUser={userState}
                  {...updateProfileTexts}
                  successCallback={({
                    lastName,
                    firstName,
                    emailVerified,
                    role,
                    image,
                  }) => {
                    refetch();
                    setUserState((prev) => ({
                      ...prev,
                      image,
                      lastName,
                      firstName,
                      emailVerified,
                      role,
                    }));
                    router.refresh();
                  }}
                />
              </AccordionContent>
            </AccordionItem>
          </Accordion>
        )}
        {isOwner && user.provider === "LOCAL" && !user.emailVerified && (
          <div className="mt-10 flex flex-col items-center justify-center gap-6 h-[50px]">
            {!respMsg.isVisible ? (
              <Button
                size="lg"
                disabled={respMsg.isLoading}
                onClick={() => handleVerify(user.email)}
              >
                {respMsg.isLoading ? (
                  <Loader2 className=" h-4 w-4 animate-spin" />
                ) : (
                  verifyEmail
                )}
              </Button>
            ) : (
              <p
                className={cn(
                  "text-lg bold tracking-tighter",
                  respMsg.isError && "text-destructive",
                )}
              >
                {respMsg.message}
              </p>
            )}
          </div>
        )}
        {isOwner && user.provider === "LOCAL" && (
          <div className="w-full flex items-center justify-center">
            <Button
              size="lg"
              onClick={() => router.push("/auth/forgot-password")}
            >
              {changePassword}
            </Button>
          </div>
        )}
      </div>
    </section>
  );
}
