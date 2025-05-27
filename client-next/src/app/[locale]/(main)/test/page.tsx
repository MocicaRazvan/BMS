import { Locale } from "@/navigation";
import { getServerSession } from "next-auth";
import { authOptions } from "@/app/api/auth/[...nextauth]/auth-options";
import React from "react";

import TestPageContent from "./page-content";
import { getArchiveQueuesTableTexts } from "@/texts/components/table";
import { unstable_setRequestLocale } from "next-intl/server";

interface Props {
  params: {
    locale: Locale;
  };
}

export default async function TestPage({ params }: Props) {
  unstable_setRequestLocale(params.locale);
  const session = await getServerSession(authOptions);
  const texts = await getArchiveQueuesTableTexts();
  return <TestPageContent texts={texts} />;
}
