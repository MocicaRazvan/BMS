import { ReactNode } from "react";
import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";

// import { redirect } from "next/navigation";
// import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";

export default function AuthLayout({
  children,
  params: { locale },
}: {
  children: ReactNode;
  params: { locale: Locale };
}) {
  // const session = await getServerSession(authOptions);
  //
  // console.log(session);

  // if (session?.user) {
  //   redirect(`/users/${session.user.id}`);
  // }

  unstable_setRequestLocale(locale);
  return <div>{children}</div>;
}
