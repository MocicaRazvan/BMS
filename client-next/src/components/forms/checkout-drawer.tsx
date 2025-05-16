"use client";

import { Button } from "@/components/ui/button";

import {
  Drawer,
  DrawerContent,
  DrawerDescription,
  DrawerHeader,
  DrawerTitle,
  DrawerTrigger,
} from "@/components/ui/drawer";
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
import { fetchStream } from "@/lib/fetchers/fetchStream";
import {
  CheckoutRequestBody,
  PlanResponse,
  SessionResponse,
} from "@/types/dto";
import { zodResolver } from "@hookform/resolvers/zod";
import { motion } from "framer-motion";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useForm } from "react-hook-form";

import {
  CheckoutSchemaTexts,
  CheckoutSchemaType,
  getCheckoutSchema,
} from "@/types/forms";
import { WithUser } from "@/lib/user";
import ErrorMessage from "@/components/forms/error-message";
import ButtonSubmit, {
  ButtonSubmitTexts,
} from "@/components/forms/button-submit";
import useLoadingErrorState from "@/hoooks/useLoadingErrorState";
import { useFormatter, useLocale } from "next-intl";

export interface CheckoutDrawerTexts {
  buttonSubmitTexts: ButtonSubmitTexts;
  error: string;
  anchor: string;
  title: string;
  header: string;
  label: string;
  description: string;
  placeholder: string;
}

interface Props extends WithUser, CheckoutDrawerTexts {
  totalPrice: number;
  plans: PlanResponse[];
  clearCartForUser: () => void;
  checkoutSchemaTexts: CheckoutSchemaTexts;
}

const MotionButton = motion(Button);

export default function CheckoutDrawer({
  authUser,
  totalPrice,
  plans,
  clearCartForUser,
  checkoutSchemaTexts,
  buttonSubmitTexts,
  header,
  anchor,
  error,
  label,
  placeholder,
  title,
  description,
}: Props) {
  const formatIntl = useFormatter();
  const locale = useLocale();

  const { isLoading, setIsLoading, router, errorMsg, setErrorMsg } =
    useLoadingErrorState();
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const schema = useMemo(
    () => getCheckoutSchema(totalPrice, checkoutSchemaTexts),
    [totalPrice, checkoutSchemaTexts],
  );

  const form = useForm<CheckoutSchemaType>({
    resolver: zodResolver(schema),
    defaultValues: {
      userConfirmedPrice: totalPrice,
    },
  });
  const totalPriceWatch = form.watch("userConfirmedPrice");

  useEffect(() => {
    if (!isDrawerOpen) {
      form.reset();
    }
  }, [isDrawerOpen]);

  useEffect(() => {
    if (totalPriceWatch !== totalPrice) {
      form.setValue("userConfirmedPrice", totalPrice);
    }
  }, [totalPriceWatch, totalPrice]);

  const onSubmit = useCallback(
    async ({ userConfirmedPrice }: CheckoutSchemaType) => {
      setIsLoading(true);
      const body: CheckoutRequestBody = {
        total: totalPrice,
        plans: plans,
        locale,
      };

      try {
        const { messages, error } = await fetchStream<SessionResponse>({
          path: "/orders/checkout/hosted",
          token: authUser.token,
          body,
          method: "POST",
        });
        if (messages.length > 0) {
          console.log(messages[0]);
          window.location.href = messages[0].url;
          clearCartForUser();
          form.reset();
        }
      } catch (e) {
        console.log(e);
      } finally {
        setIsLoading(false);
      }
    },
    [authUser.token],
  );

  return (
    <Drawer open={isDrawerOpen} onOpenChange={setIsDrawerOpen}>
      <DrawerTrigger asChild>
        <MotionButton
          initial={{ opacity: 0, scale: 0 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.3 }}
          className="w-full"
          size="lg"
          onClick={() => setIsDrawerOpen(true)}
        >
          {anchor}
        </MotionButton>
      </DrawerTrigger>
      <DrawerContent>
        <div className="mx-auto w-full max-w-sm pb-10 ">
          <DrawerHeader className="p-0 pb-1.5 md:pb-2">
            <DrawerTitle>{title}</DrawerTitle>
            <DrawerDescription>{header}</DrawerDescription>
          </DrawerHeader>
          <Form {...form}>
            <form onSubmit={form.handleSubmit(onSubmit)} noValidate>
              <FormField
                control={form.control}
                name="userConfirmedPrice"
                render={({ field }) => (
                  <FormItem>
                    <FormLabel>
                      {label}
                      <span className=" ml-2 font-bold">{totalPrice}</span>
                    </FormLabel>
                    <FormControl>
                      <Input
                        placeholder={placeholder}
                        {...field}
                        type="number"
                        disabled
                      />
                    </FormControl>
                    <FormDescription>{description}</FormDescription>
                    <FormMessage />
                  </FormItem>
                )}
              />

              <ErrorMessage message={error} show={!!errorMsg} />
              <ButtonSubmit
                isLoading={isLoading}
                disable={false}
                buttonSubmitTexts={buttonSubmitTexts}
              />
            </form>
          </Form>
        </div>
      </DrawerContent>
    </Drawer>
  );
}
