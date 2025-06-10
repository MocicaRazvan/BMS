"use client";
import { zodResolver } from "@hookform/resolvers/zod";
import { useForm } from "react-hook-form";

import { Button } from "@/components/ui/button";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { getSignInSchema, SignInSchemaTexts, SignInType } from "@/types/forms";
import { signIn } from "next-auth/react";
import { useMemo, useState } from "react";
import { Loader2 } from "lucide-react";
import { Link, Locale } from "@/navigation/navigation";
import { useRouter } from "@/navigation/client-navigation";
import OauthProviders from "@/app/[locale]/(main)/auth/oauth-providers";
import { normalizeEmailWrapper } from "@/lib/email-normalizer-wrapper";
import EmailFormField, {
  EmailFromFieldTexts,
} from "@/components/forms/email-form-field";
import { useSearchParams } from "next/navigation";

export interface SignInPageText {
  cardTitle: string;
  passwordLabel: string;
  submitButton: string;
  loadingButton: string;
  errorMessages: string;
  linkSignUp: string;
  linkForgotPassword: string;
}

interface SignInPageProps extends SignInPageText {
  signInSchemaTexts: SignInSchemaTexts;
  locale: Locale;
  emailFromFieldTexts: EmailFromFieldTexts;
}

export default function SingIn({
  cardTitle,
  emailFromFieldTexts,
  passwordLabel,
  submitButton,
  loadingButton,
  errorMessages,
  linkSignUp,
  linkForgotPassword,
  signInSchemaTexts,
  locale,
}: SignInPageProps) {
  const parms = useSearchParams();
  const schema = useMemo(
    () => getSignInSchema(signInSchemaTexts),
    [signInSchemaTexts],
  );
  const form = useForm<SignInType>({
    resolver: zodResolver(schema),
    defaultValues: {
      email: "",
      password: "",
    },
  });
  const router = useRouter();
  const [errorMsg, setErrorMsg] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const onSubmit = async (values: SignInType) => {
    setIsLoading(true);
    if (errorMsg) setErrorMsg("");
    try {
      const result = await signIn("credentials", {
        redirect: false,
        email: normalizeEmailWrapper(values.email),
        password: values.password,
      });
      setIsLoading(false);
      if (result?.error) {
        console.log("Authentication error:", result.error);
        setErrorMsg(errorMessages);
      } else {
        // router.push("/", {});
        if (window) {
          const callbackUrl = parms.get("callbackUrl");
          if (callbackUrl) {
            window.location.href = callbackUrl;
          } else {
            window.location.href = "/" + locale + "/";
          }
        }
      }
    } catch (error) {
      console.error("Unexpected error during sign-in:", error);
      setErrorMsg("An unexpected error occurred. Please try again later.");
      setIsLoading(false);
    }
  };

  return (
    <main className="w-full min-h-[calc(100vh-4rem)] flex items-center justify-center transition-all pt-5 px-2">
      <Card className="w-full max-w-[550px]">
        <CardHeader>
          <CardTitle className="text-center">{cardTitle}</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form
              onSubmit={form.handleSubmit(onSubmit)}
              className="space-y-8"
              noValidate
            >
              <EmailFormField
                texts={emailFromFieldTexts}
                form={form}
                onFocus={() => {
                  if (errorMsg) setErrorMsg("");
                }}
              />
              <FormField
                control={form.control}
                name="password"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{passwordLabel}</FormLabel>
                    <FormControl>
                      <Input
                        type="password"
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
          <OauthProviders />
        </CardContent>

        <CardFooter className="flex flex-col justify-start items-start gap-3">
          <div>
            <Link
              href="/auth/signup"
              className="text-sm italic hover:underline"
            >
              {linkSignUp}
            </Link>
          </div>
          <div>
            <Link
              href="/auth/forgot-password"
              className="text-sm italic hover:underline "
            >
              {linkForgotPassword}
            </Link>
          </div>
        </CardFooter>
      </Card>
    </main>
  );
}
