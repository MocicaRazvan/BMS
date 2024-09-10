"use client";

import {
  AdminEmailSchemaTexts,
  AdminEmailSchemaType,
  getAdminEmailSchema,
} from "@/types/forms";
import { WithUser } from "@/lib/user";
import { useCallback, useEffect, useMemo, useState } from "react";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import { Path, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Card, CardContent, CardTitle } from "@/components/ui/card";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import Editor from "@/components/editor/editor";
import ErrorMessage from "@/components/forms/error-message";
import * as DOMPurify from "dompurify";
import { useDebounce } from "@/components/ui/multiple-selector";
import { EmailRequest } from "@/types/dto";
import { fetchStream } from "@/hoooks/fetchStream";
import { toast } from "@/components/ui/use-toast";
import { handleBaseError } from "@/lib/utils";

export interface AdminEmailTexts {
  adminEmailSchemaTexts: AdminEmailSchemaTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  error: string;
  title: string;
  preview: string;
  toastDescription: string;
  items: Record<
    keyof AdminEmailSchemaType,
    {
      label: string;
      placeholder: string;
    }
  >;
}

const render = (value: string) => `
  <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:37.5em;padding-top:1.25rem;padding-bottom:1.25rem">
    <tbody>
      <tr style="width:100%">
        <td>
          <table align="center" width="100%" class="bg-backround" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:37.5em;box-shadow:0 0 #0000, 0 0 #0000, 0 20px 25px -5px rgb(0,0,0,0.1), 0 8px 10px -6px rgb(0,0,0,0.1);border-radius:0.25rem">
            <tbody>
              <tr style="width:100%">
                <td>
                  <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="height:100px;background-color:rgb(148,163,184);border-top-left-radius:0.25rem;border-top-right-radius:0.25rem;padding: 1.25rem;color: #000000;">
                    <tbody>
                      <tr>
                        <td style="padding-left: 20px ; padding-right: 20px">
                          <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="width:100%">
                            <tbody style="width:100%">
                              <tr style="width:100%" class="flex w-full items-center justify-between">
                                <td data-id="__react-email-column" style="text-align:start; color: #000000;"><a href="https://example.com" target="_blank" style="font-size:1.5rem;line-height:2rem;font-weight:700;letter-spacing:-0.05em;margin-right:1rem;text-decoration-line:none;color:inherit">Bro Meets Science</a></td>
                                <td data-id="__react-email-column" style="text-align:end; padding-left: 20px"><img src="https://res.cloudinary.com/lamatutorial/image/upload/v1722269171/logo_i2scwt_2_we0jmo.png" alt="Logo" width="80" height="80"  /></td>
                              </tr>
                            </tbody>
                          </table>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                  <table align="center" width="100%" class="space-y-2" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:37.5em;margin-top:2.5rem;margin-bottom:2.5rem;padding-left:0.5rem;padding-right:0.5rem;color: #000000;">
                    <tbody>
                      <tr style="width:100%">
                        <td style="padding: 1rem;">${value}</td>
                      </tr>
                    </tbody>
                  </table>
                  <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="height:50px;background-color:rgb(148,163,184);border-bottom-right-radius:0.25rem;border-bottom-left-radius:0.25rem;padding: 1.25rem;color: #000000;">
                    <tbody>
                      <tr>
                        <td style="padding-left: 20px ; padding-right: 20px">
                          <p style="font-size:17px;line-height:24px;margin:16px 0;text-align:center;color: #000000;">For any information, please contact us at <a href="mailto:razvanmocica@gmail.com" style="font-weight:700;letter-spacing:-0.05em;text-decoration-line:none;color:inherit">razvanmocica@gmail.com</a></p>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </td>
              </tr>
            </tbody>
          </table>
        </td>
      </tr>
    </tbody>
  </table>`;

interface Props extends WithUser, AdminEmailTexts {}

export default function AdminEmail({
  adminEmailSchemaTexts,
  authUser,
  error,
  items,
  buttonSubmitTexts,
  preview,
  title,
  toastDescription,
}: Props) {
  const schema = useMemo(
    () => getAdminEmailSchema(adminEmailSchemaTexts),
    [adminEmailSchemaTexts],
  );
  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();
  const [editorKey, setEditorKey] = useState(Math.random());

  const [renderContent, setRenderContent] = useState(render(""));

  const form = useForm<AdminEmailSchemaType>({
    resolver: zodResolver(schema),
    defaultValues: {
      email: "",
      content: "",
      subject: "",
    },
  });

  const onSubmit = useCallback(
    async (data: AdminEmailSchemaType) => {
      setIsLoading(true);
      setErrorMsg("");
      const body: EmailRequest = { ...data, recipientEmail: data.email };
      try {
        const res = await fetchStream({
          path: "/users/admin/email",
          method: "POST",
          token: authUser.token,
          body,
        });
        form.reset();
        setEditorKey(Math.random());
        toast({
          description: `${toastDescription} ${data.email} `,
          variant: "success",
        });
        if (res?.error?.status) {
          setErrorMsg(error);
        }
      } catch (e) {
        handleBaseError(e, setErrorMsg, error);
      } finally {
        setIsLoading(false);
      }
    },
    [authUser.token, error, form, setErrorMsg, setIsLoading, toastDescription],
  );

  const content = form.watch("content");
  const debounceContent = useDebounce(content, 500);

  useEffect(() => {
    setRenderContent(DOMPurify.sanitize(render(debounceContent)));
  }, [debounceContent]);

  return (
    <Card className="max-w-7xl w-full sm:px-2 md:px-5 py-6 border-0 mx-auto">
      {/*<CardTitle className="font-bold text-2xl text-center capitalize">*/}
      {/*  {title}*/}
      {/*</CardTitle>*/}
      <CardContent>
        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(onSubmit)}
            className="space-y-8 lg:space-y-12"
            noValidate
          >
            <FormField
              control={form.control}
              name={"email"}
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="capitalize">
                    {items.email.label}
                  </FormLabel>
                  <FormControl>
                    <Input placeholder={items.email.placeholder} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name={"subject"}
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="capitalize">
                    {items.subject.label}
                  </FormLabel>
                  <FormControl>
                    <Input placeholder={items.subject.placeholder} {...field} />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="content"
              render={({ field }) => (
                <FormItem>
                  <FormLabel className="capitalize">
                    {items.content.label}
                  </FormLabel>
                  <FormControl>
                    {/* <Textarea placeholder={bodyPlaceholder} {...field} />
                     */}
                    <Editor
                      descritpion={field.value as string}
                      onChange={field.onChange}
                      placeholder={items.content.placeholder}
                      key={editorKey}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="space-y-2">
              <p className="font-medium peer-disabled:cursor-not-allowed peer-disabled:opacity-70 text-lg capitalize">
                {preview}
              </p>
              <div className="bg-slate-50 w-fit max-w-3xl mx-auto px-6 py-4 rounded ">
                <div
                  className="prose max-w-none [&_ol]:list-decimal [&_ul]:list-disc text-wrap"
                  dangerouslySetInnerHTML={{ __html: renderContent }}
                />
              </div>
            </div>
            <ErrorMessage message={error} show={!!errorMsg} />
            <ButtonSubmit
              isLoading={isLoading}
              disable={false}
              buttonSubmitTexts={buttonSubmitTexts}
            />
          </form>
        </Form>
      </CardContent>
    </Card>
  );
}
