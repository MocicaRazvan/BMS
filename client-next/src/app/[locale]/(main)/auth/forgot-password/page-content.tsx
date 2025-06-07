"use client";
import { useForm } from "react-hook-form";
import { EmailSchemaTexts, EmailType, getEmailSchema } from "@/types/forms";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useCallback, useMemo, useState } from "react";
import { Form } from "@/components/ui/form";
import { Button } from "@/components/ui/button";
import { Loader2 } from "lucide-react";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { Session } from "next-auth";
import { MX_SPRING_MESSAGE } from "@/types/constants";
import { normalizeEmailWrapper } from "@/lib/email-normalizer-wrapper";
import EmailFormField, {
  EmailFromFieldTexts,
} from "@/components/forms/email-form-field";

export interface ForgotPasswordPageText {
  cardTitle: string;
  submitButton: string;
  loadingButton: string;
  successMessage: string;
  mxError: string;
  user: Session["user"];
}

interface ForgotPasswordPageProps extends ForgotPasswordPageText {
  emailSchemaTexts: EmailSchemaTexts;
  emailFromFieldTexts: EmailFromFieldTexts;
}

export default function ForgotPasswordPage({
  cardTitle,
  emailFromFieldTexts,
  submitButton,
  loadingButton,
  successMessage,
  emailSchemaTexts,
  user,
  mxError,
}: ForgotPasswordPageProps) {
  const schema = useMemo(
    () => getEmailSchema(emailSchemaTexts),
    [emailSchemaTexts],
  );
  const form = useForm<EmailType>({
    resolver: zodResolver(schema),
    defaultValues: {
      email: user?.email || "",
    },
  });
  const [errorMsg, setErrorMsg] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [show, setShow] = useState(false);

  const onSubmit = useCallback(
    async ({ email }: EmailType) => {
      setErrorMsg("");
      setShow(false);
      setIsLoading(true);
      const { error } = await fetchStream({
        path: "/auth/resetPassword",
        method: "POST",
        body: {
          email: normalizeEmailWrapper(email),
        },
      });
      if (error && error.message.includes(MX_SPRING_MESSAGE)) {
        setErrorMsg(mxError);
      } else {
        setShow(true);
      }
      setIsLoading(false);
    },
    [mxError],
  );

  return (
    <main className="w-full min-h-[calc(100vh-21rem)] flex items-center justify-center transition-all">
      <Card className="w-[500px]">
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
                disabled={!!user?.email}
                duration={!!user?.email ? 0 : 0.2}
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
        <CardFooter className="mt-4 flex items-center justify-center">
          {show && (
            <p className="text-lg tracking-tighter font-bold text-center">
              {successMessage}
            </p>
          )}
        </CardFooter>
      </Card>
    </main>
  );
}
