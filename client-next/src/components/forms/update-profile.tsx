"use client";

import { getUpdateProfileSchema, UpdateProfileType } from "@/types/forms";
import { useCallback, useEffect, useMemo } from "react";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import { WithUser } from "@/lib/user";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import useFilesObjectURL from "@/hoooks/useFilesObjectURL";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import InputFile from "@/components/forms/input-file";
import { UpdateProfileTexts } from "@/texts/components/forms";
import ErrorMessage from "@/components/forms/error-message";
import ButtonSubmit from "@/components/forms/button-submit";
import { handleBaseError } from "@/lib/utils";
import { useSession } from "next-auth/react";
import { fetchWithFiles } from "@/hoooks/fetchWithFiles";
import { CustomEntityModel, UserBody, UserDto } from "@/types/dto";
import { BaseError } from "@/types/responses";
import { useNavigationGuardI18nForm } from "@/hoooks/use-navigation-guard-i18n-form";
import { toast } from "@/components/ui/use-toast";

interface Props extends WithUser, UpdateProfileTexts {
  successCallback: (userDto: UserDto) => void;
  toastSuccess: string;
}

export default function UpdateProfile({
  updateProfileSchemaTexts,
  fieldTexts,
  authUser,
  buttonSubmitTexts,
  error: errorText,
  lastName,
  firstName,
  successCallback,
  toastSuccess,
}: Props) {
  const schema = useMemo(
    () => getUpdateProfileSchema(updateProfileSchemaTexts),
    [updateProfileSchemaTexts],
  );

  const session = useSession();

  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();

  const form = useForm<UpdateProfileType>({
    resolver: zodResolver(schema),
    defaultValues: {
      firstName: authUser.firstName,
      lastName: authUser.lastName,
      image: [],
    },
  });

  useNavigationGuardI18nForm({ form });

  const onSubmit = useCallback(
    async (data: UpdateProfileType) => {
      if (!session.data?.user?.token) return;
      setIsLoading(true);
      const files = data?.image?.map((image) => image.file) || [];
      try {
        const res = await fetchWithFiles<
          UserBody,
          BaseError,
          CustomEntityModel<UserDto>
        >({
          path: `/users/${authUser.id}`,
          method: "PUT",
          token: authUser.token,
          data: {
            files,
            body: {
              firstName: data.firstName,
              lastName: data.lastName,
            },
          },
          clientId: "NONE",
        });
        await session.update({
          ...session,
          data: {
            ...session.data,
            user: {
              ...session.data?.user,
              firstName: res.content.firstName,
              lastName: res.content.lastName,
              image: res.content.image,
            },
          },
        });
        successCallback(res.content);
        toast({
          variant: "success",
          title: toastSuccess,
        });
      } catch (e) {
        handleBaseError(e, setErrorMsg, errorText);
      } finally {
        setIsLoading(false);
      }
    },
    [
      authUser.id,
      authUser.token,
      errorText,
      session,
      setErrorMsg,
      setIsLoading,
      successCallback,
      toastSuccess,
    ],
  );

  const { fileCleanup, chunkProgressValue } = useFilesObjectURL({
    files: authUser.image ? [authUser.image] : [],
    fieldName: "image",
    setValue: form.setValue,
    getValues: form.getValues,
  });

  const isSubmitDisabled = useMemo(
    () => chunkProgressValue < 100 && !!authUser.image,
    [chunkProgressValue, authUser.image],
  );

  useEffect(() => {
    return () => {
      fileCleanup();
    };
  }, [fileCleanup]);

  return (
    <div>
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(onSubmit)}
          className="space-y-8 lg:space-y-12 w-full px-10 lg:px-20"
          noValidate
        >
          <FormField
            control={form.control}
            name="firstName"
            render={({ field }) => (
              <FormItem>
                <FormLabel>{firstName}</FormLabel>
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
                <FormLabel>{lastName}</FormLabel>
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
          <InputFile<UpdateProfileType>
            control={form.control}
            fieldName={"image"}
            fieldTexts={fieldTexts}
            multiple={false}
            initialLength={authUser.image ? 1 : 0}
            cropShape="round"
            loadingProgress={chunkProgressValue}
          />
          <ErrorMessage message={errorText} show={!!errorMsg} />
          <ButtonSubmit
            isLoading={isLoading}
            disable={isSubmitDisabled}
            buttonSubmitTexts={buttonSubmitTexts}
          />
        </form>
      </Form>
    </div>
  );
}
