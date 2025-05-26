import { LocaleProps, redirect } from "@/navigation";
import { RedirectType } from "next/navigation";
import { unstable_setRequestLocale } from "next-intl/server";

export default async function FailureRedirect({
  params: { locale },
}: LocaleProps) {
  unstable_setRequestLocale(locale);

  return redirect("/", RedirectType.replace);
}
