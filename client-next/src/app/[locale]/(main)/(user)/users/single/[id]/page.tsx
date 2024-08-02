import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";
import { getUser } from "@/lib/user";
import UserPageContent from "@/app/[locale]/(main)/(user)/users/single/[id]/page-content";
import { getUserPageTexts } from "@/texts/pages";

interface Props {
  params: { locale: Locale; id: string };
}

export default async function UserPage({ params: { locale, id } }: Props) {
  unstable_setRequestLocale(locale);

  const [authUser, userPageTexts] = await Promise.all([
    getUser(),
    getUserPageTexts(),
  ]);

  return <UserPageContent authUser={authUser} id={id} {...userPageTexts} />;
}
