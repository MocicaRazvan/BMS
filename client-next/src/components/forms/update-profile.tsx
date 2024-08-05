"use client";

import {
  getUpdateProfileSchema,
  UpdateProfileSchemaTexts,
  UpdateProfileType,
} from "@/types/forms";
import { useCallback, useMemo } from "react";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import { WithUser } from "@/lib/user";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import useFilesBase64 from "@/hoooks/useFilesBase64";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import InputFile, { FieldInputTexts } from "@/components/forms/input-file";
import { UpdateProfileTexts } from "@/texts/components/forms";
import ErrorMessage from "@/components/forms/error-message";
import ButtonSubmit from "@/components/forms/button-submit";
import { handleBaseError } from "@/lib/utils";
import { useSession } from "next-auth/react";
import { fetchWithFiles } from "@/hoooks/fetchWithFiles";
import { CustomEntityModel, UserBody, UserDto } from "@/types/dto";
import { BaseError } from "@/types/responses";
import { Session } from "next-auth";

interface Props extends WithUser, UpdateProfileTexts {
  successCallback: (img: string) => void;
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
        successCallback(res.content.image);
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
    ],
  );

  useFilesBase64({
    files: authUser.image ? [authUser.image] : [],
    fieldName: "image",
    setValue: form.setValue,
    getValues: form.getValues,
  });

  return (
    <div>
      <Form {...form}>
        <form
          onSubmit={form.handleSubmit(onSubmit)}
          className="space-y-8 lg:space-y-12 w-full px-10 lg:px-20"
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
          />{" "}
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
          />
          <ErrorMessage message={errorText} show={!!errorMsg} />
          <ButtonSubmit
            isLoading={isLoading}
            disable={false}
            buttonSubmitTexts={buttonSubmitTexts}
          />
        </form>
      </Form>
    </div>
  );
}
