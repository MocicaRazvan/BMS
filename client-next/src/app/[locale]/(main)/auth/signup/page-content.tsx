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
import {
  getRegistrationSchema,
  RegisterSchemaTexts,
  RegisterType,
} from "@/types/forms";
import { useMemo, useState } from "react";
import { Link } from "@/navigation/navigation";
import { Loader2 } from "lucide-react";

import { PasswordInput } from "@/components/ui/password-input";
import { registerSubmit } from "@/actions/forms/register-action";
import OauthProviders from "@/app/[locale]/(main)/auth/oauth-providers";
import {
  PasswordStrengthIndicator,
  PasswordStrengthIndicatorTexts,
  usePasswordStrength,
} from "@/components/forms/passowrd-strength-indicator";
import { MX_SPRING_MESSAGE } from "@/types/constants";
import EmailFormField, {
  EmailFromFieldTexts,
} from "@/components/forms/email-form-field";
import { useRouter } from "@/navigation/client-navigation";

interface SignUpPageText {
  emailExistsError: string;
  cardTitle: string;
  firstNameLabel: string;
  lastNameLabel: string;
  passwordLabel: string;
  confirmPasswordLabel: string;
  submitButton: string;
  loadingButton: string;
  linkSignIn: string;
  mxError: string;
}

interface Props extends SignUpPageText {
  registrationSchemaTexts: RegisterSchemaTexts;
  passwordStrengthTexts: PasswordStrengthIndicatorTexts;
  emailFromFieldTexts: EmailFromFieldTexts;
}

export default function SignUp({
  linkSignIn,
  confirmPasswordLabel,
  passwordLabel,
  firstNameLabel,
  lastNameLabel,
  emailFromFieldTexts,
  submitButton,
  loadingButton,
  emailExistsError,
  cardTitle,
  registrationSchemaTexts,
  passwordStrengthTexts,
  mxError,
}: Props) {
  const schema = useMemo(
    () => getRegistrationSchema(registrationSchemaTexts),
    [registrationSchemaTexts],
  );
  const form = useForm<RegisterType>({
    resolver: zodResolver(schema),
    defaultValues: {
      email: "",
      password: "",
      confirmPassword: "",
      firstName: "",
      lastName: "",
    },
  });
  const router = useRouter();
  const [errorMsg, setErrorMsg] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const password = form.watch("password");
  const { strength } = usePasswordStrength(password);

  const onSubmit = async (values: RegisterType) => {
    setIsLoading(true);
    try {
      const resp = await registerSubmit(values);
      if (resp) {
        console.log("resp", resp);

        if (resp.message.includes("already exists")) {
          setErrorMsg(emailExistsError);
        }
        if (resp.message.includes(MX_SPRING_MESSAGE)) {
          setErrorMsg(mxError);
        } else {
          setErrorMsg(resp.message);
        }
      } else {
        router.push("/auth/signin");
      }
    } catch (error) {
      console.error("Error during registration:", error);
      setErrorMsg("An unexpected error occurred. Please try again.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <main className="w-full min-h-[calc(100vh-4rem)] flex items-center justify-center transition-all py-4 px-2">
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
              <div className="w-full flex items-center justify-center gap-4">
                <FormField
                  control={form.control}
                  name="firstName"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>{firstNameLabel}</FormLabel>
                      <FormControl>
                        <Input
                          id="signup-first-name"
                          autoComplete="given-name"
                          placeholder="John"
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
                  name="lastName"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>{lastNameLabel}</FormLabel>
                      <FormControl>
                        <Input
                          id="signup-last-name"
                          autoComplete="family-name"
                          placeholder="Doe"
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
              </div>
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
                      <PasswordInput
                        {...field}
                        onFocus={() => {
                          if (errorMsg) setErrorMsg("");
                        }}
                      />
                    </FormControl>
                    <FormMessage />
                    <div className="pt-5">
                      <PasswordStrengthIndicator
                        texts={passwordStrengthTexts}
                        strength={strength}
                      />
                    </div>
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="confirmPassword"
                disabled={strength.score !== 100}
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
          <OauthProviders />
        </CardContent>
        <CardFooter>
          <Link href="/auth/signin" className="text-sm italic hover:underline">
            {linkSignIn}
          </Link>
        </CardFooter>
      </Card>
    </main>
  );
}
