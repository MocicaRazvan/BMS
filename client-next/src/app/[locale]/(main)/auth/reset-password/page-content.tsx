"use client";

import { notFound, useSearchParams } from "next/navigation";
import { useForm } from "react-hook-form";
import {
  getResetPasswordSchema,
  ResetPasswordSchemaTexts,
  ResetPasswordType,
} from "@/types/forms";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { PasswordInput } from "@/components/ui/password-input";
import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";
import { fetchStream } from "@/hoooks/fetchStream";
import { useRouter } from "@/navigation";
import { dezerialize } from "zodex";
import { Session } from "next-auth";
import { signIn, useSession } from "next-auth/react";
import { BaseError } from "@/types/responses";
import { logError } from "@/app/[locale]/(main)/auth/signin/actions";

export interface ResetPasswordPageText {
  cardTitle: string;
  emailLabel: string;
  passwordLabel: string;
  confirmPasswordLabel: string;
  submitButton: string;
  loadingButton: string;
  errorMessages: string;
}

interface ResetPasswordPageProps extends ResetPasswordPageText {
  resetPasswordSchemaTexts: ResetPasswordSchemaTexts;
  user: Session["user"];
}

export default function ResetPasswordPage({
  passwordLabel,
  confirmPasswordLabel,
  emailLabel,
  submitButton,
  loadingButton,
  resetPasswordSchemaTexts,
  cardTitle,
  errorMessages,
  user,
}: ResetPasswordPageProps) {
  const session = useSession();

  const searchParams = useSearchParams();
  const token = searchParams.get("token");
  const email = searchParams.get("email");

  if (!token || !email) {
    notFound();
  }

  const schema = useMemo(
    () => getResetPasswordSchema(resetPasswordSchemaTexts),
    [resetPasswordSchemaTexts],
  );

  const form = useForm<ResetPasswordType>({
    resolver: zodResolver(schema),
    defaultValues: {
      password: "",
      confirmPassword: "",
    },
  });

  const router = useRouter();
  const [errorMsg, setErrorMsg] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const onSubmit = async ({ password }: ResetPasswordType) => {
    setIsLoading(true);
    const { messages, error, isFinished } = await fetchStream<{
      token: string;
      email: string;
    }>({
      path: "/auth/changePassword",
      method: "POST",
      body: {
        email,
        token,
        newPassword: password,
      },
    });
    console.log(messages);
    if (isFinished && error) {
      setErrorMsg(errorMsg);
      setIsLoading(false);
    } else {
      if (isFinished && messages[0]) {
        if (user) {
          await logError("token", messages[0].token);
          await session.update({
            ...session,
            data: {
              ...session.data,
              user: { ...user, token: messages[0].token },
            },
          });
          router.push(`/users/single/${user.id}`);
        } else {
          try {
            const result = await signIn("credentials", {
              redirect: false,
              email,
              password,
            });
            if (result?.error) {
              console.log("Authentication error:", result.error);
              await logError("Authentication error", result.error);
              setErrorMsg(errorMessages);
            } else {
              console.log("Sign-in successful:", result);
              // todo
              router.push(`/`);
            }
          } catch (error) {
            setIsLoading(false);
          }
        }
      } else if (isFinished && !messages[0]) {
        setErrorMsg(errorMsg);
        setIsLoading(false);
      }

      // router.replace("/auth/signin");
    }

    console.log({ messages, error });
  };

  return (
    <main className="w-full min-h-[calc(100vh-4rem)] flex items-center justify-center transition-all">
      <Card className="w-[500px]">
        <CardHeader>
          <CardTitle className="text-center">{cardTitle}</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
              <FormItem>
                <FormLabel>{emailLabel}</FormLabel>
                <FormControl>
                  <Input disabled={true} value={email} />
                </FormControl>
              </FormItem>

              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{passwordLabel}</FormLabel>
                    <FormControl>
                      <PasswordInput
                        {...field}
                        onFocus={() => {
                          if (errorMsg) setErrorMsg("");
                        }}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="confirmPassword"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{confirmPasswordLabel}</FormLabel>
                    <FormControl>
                      <PasswordInput
                        {...field}
                        onFocus={() => {
                          if (errorMsg) setErrorMsg("");
                        }}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              {errorMsg && (
                <p className="font-medium text-destructive">{errorMsg}</p>
              )}
              {!isLoading ? (
                <Button type="submit">{submitButton}</Button>
              ) : (
                <Button disabled>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  {loadingButton}
                </Button>
              )}
            </form>
          </Form>
        </CardContent>
      </Card>
    </main>
  );
}
