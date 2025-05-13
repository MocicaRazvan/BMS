"use client";

import {
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Input } from "@/components/ui/input";
import { Path, UseFormReturn } from "react-hook-form";
import { normalizeEmailWrapper } from "@/lib/email-normalizer-wrapper";
import { AnimatePresence, motion } from "framer-motion";

export interface EmailFromFieldTexts {
  label: string;
  description: string;
}

interface BaseEmail {
  email: string;
}

interface Props<T extends BaseEmail> {
  form: UseFormReturn<T>;
  texts: EmailFromFieldTexts;
  onFocus?: () => void;
  duration?: number;
  disabled?: boolean;
}

export default function EmailFormField<T extends BaseEmail>({
  form,
  onFocus,
  texts: { label, description },
  duration = 0.2,
  disabled = false,
}: Props<T>) {
  const pathEmail = "email" as Path<T>;
  const value = form.watch(pathEmail);
  return (
    <FormField
      control={form.control}
      name={pathEmail}
      render={({ field }) => (
        <FormItem>
          <FormLabel>{label}</FormLabel>
          <FormControl>
            <Input
              placeholder="johndoe@gmail.com"
              disabled={disabled}
              {...field}
              onFocus={() => {
                onFocus?.();
              }}
            />
          </FormControl>
          <AnimatePresence>
            {value && (
              <motion.div
                key="description-email"
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: "auto" }}
                exit={{ opacity: 0, height: 0 }}
                transition={{ duration }}
              >
                <FormDescription>
                  {`${description} ${normalizeEmailWrapper(value)}`}
                </FormDescription>
              </motion.div>
            )}
          </AnimatePresence>
          <FormMessage />
        </FormItem>
      )}
    />
  );
}

interface EmailFormFieldPlaceHolderProps {
  texts: EmailFromFieldTexts;
  value: string;
}
export function EmailFormFieldPlaceHolder({
  value,
  texts: { label, description },
}: EmailFormFieldPlaceHolderProps) {
  return (
    <FormItem>
      <FormLabel>{label}</FormLabel>
      <FormControl>
        <Input disabled={true} value={value} />
      </FormControl>
      <FormDescription>
        {`${description} ${normalizeEmailWrapper(value)}`}
      </FormDescription>
    </FormItem>
  );
}
