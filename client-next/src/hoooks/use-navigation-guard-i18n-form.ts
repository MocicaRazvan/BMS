"use client";
import { useNavigationGuard } from "next-navigation-guard";
import { useEffect, useState } from "react";
import getUseNavigationGuardI18nTextsAction from "@/actions/texts/navigation-i18n-text-action";
import { UseFormReturn } from "react-hook-form";
interface BaseProps {
  enabled: boolean;
}
export interface UseNavigationGuardI18nTexts {
  confirm: string;
}
export function useNavigationGuardI18n({ enabled }: BaseProps) {
  const [texts, setTexts] = useState<UseNavigationGuardI18nTexts>({
    confirm: "",
  });
  useEffect(() => {
    getUseNavigationGuardI18nTextsAction().then(setTexts);
  }, []);
  useNavigationGuard({
    enabled,
    confirm: () => window.confirm(texts.confirm),
  });
}
interface FormProps {
  form: UseFormReturn<any>;
}
export function useNavigationGuardI18nForm({ form }: FormProps) {
  useNavigationGuardI18n({
    enabled:
      !form.formState.isSubmitting &&
      Object.keys(form.formState.dirtyFields).length > 0,
  });
}
