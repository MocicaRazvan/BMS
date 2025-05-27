"use server";

import { getMetadataValues } from "@/texts/metadata";
import { Locale } from "@/navigation";
import { Session } from "next-auth";

export async function getMetadataAction(
  authUser: Session["user"],
  locale: Locale,
) {
  return getMetadataValues(authUser, locale as Locale);
}
