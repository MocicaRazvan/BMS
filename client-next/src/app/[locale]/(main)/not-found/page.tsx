// "use client";
//
// import { useMessages } from "next-intl";
// import { getTranslations } from "next-intl/server";
// import { useEffect, useState } from "react";
// import { getErrorPageTexts } from "@/texts/pages";
//
// export default function NotFoundPage() {
//   const [texts, setTexts] = useState<{
//     statusCode: string;
//     title: string;
//     description: string;
//   }>({ statusCode: "", title: "", description: "" });
//
//   useEffect(() => {
//     getErrorPageTexts().then(setTexts);
//   }, []);
//
//   if (!texts.title) return null;
//   return (
//     <div className="w-full min-h-[calc(100vh-21rem)] flex flex-col items-center justify-center transition-all gap-10">
//       <h1 className="text-5xl font-bold">
//         {texts.statusCode} {texts.title}
//       </h1>
//       <p className="text-lg">{texts.description}</p>
//     </div>
//   );
// }
import {getTranslations} from "next-intl/server";

export default async function NotFoundPage() {
  const t = await getTranslations("ErrorPage");
  console.log(t);
  return (
    <div className="w-full min-h-[calc(100vh-21rem)] flex flex-col items-center justify-center transition-all gap-10">
      <h1 className="text-5xl font-bold">
        {t("statusCode", { statusCode: 404 })} {t("title")}
      </h1>
      <p className="text-lg">{t("description")}</p>
    </div>
  );
}
