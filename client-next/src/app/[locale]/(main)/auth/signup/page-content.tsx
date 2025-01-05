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
import { Link, useRouter } from "@/navigation";
import { Loader2 } from "lucide-react";

import { PasswordInput } from "@/components/ui/password-input";
import { registerSubmit } from "@/actions/froms";
import OauthProviders from "@/app/[locale]/(main)/auth/oauth-providers";
import {
  calculatePasswordStrength,
  PasswordStrengthIndicator,
  PasswordStrengthIndicatorTexts,
} from "@/components/forms/passowrd-strength-indicator";

interface SignUpPageText {
  emailExistsError: string;
  cardTitle: string;
  firstNameLabel: string;
  lastNameLabel: string;
  emailLabel: string;
  passwordLabel: string;
  confirmPasswordLabel: string;
  submitButton: string;
  loadingButton: string;
  linkSignIn: string;
}

interface Props extends SignUpPageText {
  registrationSchemaTexts: RegisterSchemaTexts;
  passwordStrengthTexts: PasswordStrengthIndicatorTexts;
}

export default function SignUp({
  linkSignIn,
  confirmPasswordLabel,
  passwordLabel,
  firstNameLabel,
  lastNameLabel,
  emailLabel,
  submitButton,
  loadingButton,
  emailExistsError,
  cardTitle,
  registrationSchemaTexts,
  passwordStrengthTexts,
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
  const strength = useMemo(
    () => calculatePasswordStrength(password),
    [password],
  );

  const onSubmit = async (values: RegisterType) => {
    setIsLoading(true);
    const resp = await registerSubmit(values);
    if (resp) {
      console.log("resp", resp);
      if (resp.message.includes("already exists")) {
        setErrorMsg(emailExistsError);
      } else {
        setErrorMsg(resp.message);
      }
      setIsLoading(false);
    } else {
      router.push("/auth/signin");
    }
    setIsLoading(false);
  };

  return (
    <main className="w-full min-h-[calc(100vh-4rem)] flex items-center justify-center transition-all py-4">
      <Card className="w-[500px]">
        <CardHeader>
          <CardTitle className="text-center">{cardTitle}</CardTitle>
        </CardHeader>
        <CardContent>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
              <div className="w-full flex items-center justify-center gap-4">
                <FormField
                  control={form.control}
                  name="firstName"
                  render={({ field }) => (
                    <FormItem>
                      <FormLabel>{firstNameLabel}</FormLabel>
                      <FormControl>
                        <Input
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
              <FormField
                control={form.control}
                name="email"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>{emailLabel}</FormLabel>
                    <FormControl>
                      <Input
                        placeholder="johndoe@gmail.com"
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
                        password={password}
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
