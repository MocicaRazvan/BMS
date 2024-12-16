import { getTranslations, unstable_setRequestLocale } from "next-intl/server";

import NotFoundLottie from "@/app/[locale]/(main)/not-found/not-found-lottie";
import { Suspense } from "react";
import { Locale } from "@/navigation";

interface Props {
  params: { locale: Locale };
}
export default async function NotFoundPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const t = await getTranslations("ErrorPage");
  return (
    <div className="w-full min-h-[calc(100vh-21rem)] flex flex-col items-center justify-center transition-all gap-10 px-4 pt-10">
      <h1 className="text-3xl md:text-5xl font-bold tracking-tighter">
        {t("statusCode", { statusCode: 404 })} {t("title")}
      </h1>
      <p className="text-lg">{t("description")}</p>
      <Suspense fallback={<div className="md:w-1/3 md:h-1/3 mx-auto" />}>
        <NotFoundLottie />
      </Suspense>
    </div>
  );
}
