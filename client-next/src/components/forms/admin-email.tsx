"use client";

import {
  AdminEmailSchemaTexts,
  AdminEmailSchemaType,
  getAdminEmailSchema,
} from "@/types/forms";
import { useCallback, useEffect, useMemo, useState } from "react";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { Card, CardContent } from "@/components/ui/card";
import {
  Form,
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import Editor, { EditorTexts } from "@/components/editor/editor";
import ErrorMessage from "@/components/forms/error-message";
import DOMPurify from "dompurify";
import { useDebounce } from "@/components/ui/multiple-selector";
import { EmailRequest } from "@/types/dto";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { toast } from "@/components/ui/use-toast";
import { handleBaseError } from "@/lib/utils";
import { useNavigationGuardI18nForm } from "@/hoooks/use-navigation-guard-i18n-form";
import { MX_SPRING_MESSAGE } from "@/types/constants";
import { normalizeEmailWrapper } from "@/lib/email-normalizer-wrapper";
import { AnimatePresence, motion } from "framer-motion";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

export interface AdminEmailTexts {
  adminEmailSchemaTexts: AdminEmailSchemaTexts;
  buttonSubmitTexts: ButtonSubmitTexts;
  error: string;
  mxError: string;
  title: string;
  preview: string;
  toastDescription: string;
  emailDescription: string;
  items: Record<
    keyof AdminEmailSchemaType,
    {
      label: string;
      placeholder: string;
    }
  >;
  editorTexts: EditorTexts;
}

const render = (value: string) => `
  <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:45em;padding-top:1.5rem;padding-bottom:1.5rem">
    <tbody>
      <tr style="width:100%">
        <td>
          <table align="center" width="100%" class="bg-backround" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:45em;box-shadow:0 0 #0000, 0 0 #0000, 0 20px 25px -5px rgb(0,0,0,0.1), 0 8px 10px -6px rgb(0,0,0,0.1);border-radius:0.25rem">
            <tbody>
              <tr style="width:100%">
                <td>
                  <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="height:120px;background-color:rgb(148,163,184);border-top-left-radius:0.25rem;border-top-right-radius:0.25rem;padding: 1.5rem;color: #000000;">
                    <tbody>
                      <tr>
                        <td style="padding-left: 20px ; padding-right: 20px">
                          <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="width:100%">
                            <tbody style="width:100%">
                              <tr style="width:100%" class="flex w-full items-center justify-between">
                                <td data-id="__react-email-column" style="text-align:start; color: #000000;"><a href="https://example.com" target="_blank" style="font-size:2rem;line-height:2.5rem;font-weight:700;letter-spacing:-0.05em;margin-right:1rem;text-decoration-line:none;color:inherit">Bro Meets Science</a></td>
                                <td data-id="__react-email-column" style="text-align:end; padding-left: 20px"><img src="https://res.cloudinary.com/lamatutorial/image/upload/v1722269171/logo_i2scwt_2_we0jmo.png" alt="Logo" width="100" height="100"  /></td>
                              </tr>
                            </tbody>
                          </table>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                  <table align="center" width="100%" class="space-y-2" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="max-width:45em;margin-top:3rem;margin-bottom:3rem;padding-left:1rem;padding-right:1rem;color: #000000;">
                    <tbody>
                      <tr style="width:100%">
                        <td style="padding: 1.5rem; font-size:18px; line-height:28px;">${value}</td>
                      </tr>
                    </tbody>
                  </table>
                  <table align="center" width="100%" border="0" cellPadding="0" cellSpacing="0" role="presentation" style="height:60px;background-color:rgb(148,163,184);border-bottom-right-radius:0.25rem;border-bottom-left-radius:0.25rem;padding: 1.5rem;color: #000000;">
                    <tbody>
                      <tr>
                        <td style="padding-left: 20px ; padding-right: 20px">
                          <p style="font-size:18px;line-height:26px;margin:18px 0;text-align:center;color: #000000;">For any information, please contact us at <a href="mailto:razvanmocica@gmail.com" style="font-weight:700;letter-spacing:-0.05em;text-decoration-line:none;color:inherit">razvanmocica@gmail.com</a></p>
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

interface Props extends AdminEmailTexts {}

export default function AdminEmail({
  adminEmailSchemaTexts,
  error,
  items,
  buttonSubmitTexts,
  preview,
  title,
  toastDescription,
  editorTexts,
  mxError,
  emailDescription,
}: Props) {
  const { authUser } = useAuthUserMinRole();

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

  useNavigationGuardI18nForm({ form });

  const onSubmit = useCallback(
    async (data: AdminEmailSchemaType) => {
      setIsLoading(true);
      setErrorMsg("");
      const body: EmailRequest = {
        ...data,
        recipientEmail: normalizeEmailWrapper(data.email),
      };
      try {
        const res = await fetchStream({
          path: "/users/admin/email",
          method: "POST",
          token: authUser.token,
          body,
        });
        if (res?.error?.status) {
          const err = res?.error;

          if (
            err.status === 400 &&
            typeof err.message === "string" &&
            err.message.includes(MX_SPRING_MESSAGE)
          ) {
            setErrorMsg(mxError);
            return;
          }
          setErrorMsg(error);
          return;
        }
        form.reset();
        setEditorKey(Math.random());
        toast({
          description: `${toastDescription} ${data.email} `,
          variant: "success",
        });
      } catch (e) {
        handleBaseError(e, setErrorMsg, error);
      } finally {
        setIsLoading(false);
      }
    },
    [
      authUser.token,
      error,
      form,
      mxError,
      setErrorMsg,
      setIsLoading,
      toastDescription,
    ],
  );

  const content = form.watch("content");
  const emailValue = form.watch("email");
  const debounceContent = useDebounce(content, 500);

  useEffect(() => {
    setRenderContent(DOMPurify.sanitize(render(debounceContent)));
  }, [debounceContent]);

  return (
    <Card className="max-w-7xl w-full sm:px-2 md:px-5 py-6 border-0 mx-auto">
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
                  <AnimatePresence>
                    {emailValue && (
                      <motion.div
                        key="description-email"
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: "auto" }}
                        exit={{ opacity: 0, height: 0 }}
                        transition={{ duration: 0.2 }}
                      >
                        <FormDescription>
                          {`${emailDescription} ${normalizeEmailWrapper(emailValue)}`}
                        </FormDescription>
                      </motion.div>
                    )}
                  </AnimatePresence>
                  <FormMessage />
                </FormItem>
              )}
            />
            <FormField
              control={form.control}
              name="subject"
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
                    <Editor
                      descritpion={field.value as string}
                      onChange={field.onChange}
                      placeholder={items.content.placeholder}
                      key={editorKey}
                      texts={editorTexts}
                      useEmojis={false}
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />
            <div className="space-y-6">
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
            <ErrorMessage message={errorMsg} show={!!errorMsg} />
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
