import { getTranslations, unstable_setRequestLocale } from "next-intl/server";
import { Locale } from "@/navigation";

interface Props {
  params: { locale: Locale };
}
export default async function NotFoundPage({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  const t = await getTranslations("ErrorPage");
  return (
    <div className="w-full min-h-[calc(100vh-21rem)] flex flex-col items-center justify-center transition-all gap-10">
      <h1 className="text-5xl font-bold">
        {t("statusCode", { statusCode: 404 })} {t("title")}
      </h1>
      <p className="text-lg">{t("description")}</p>
    </div>
  );
}
