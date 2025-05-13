"use server";

import { RegisterType } from "@/types/forms";
import { BaseError } from "@/types/responses";
import { redirect } from "@/navigation";
import { emitInfo } from "@/logger";
import { getCsrfNextAuthHeader } from "@/actions/get-csr-next-auth";
import fetchFactory from "@/lib/fetchers/fetchWithRetry";
import { normalizeEmailWrapper } from "@/lib/email-normalizer-wrapper";

export async function registerSubmit(data: RegisterType): Promise<BaseError> {
  const csrfHeader = await getCsrfNextAuthHeader();
  const resp = await fetchFactory(fetch)(
    process.env.NEXT_PUBLIC_SPRING! + "/auth/register",
    {
      method: "POST",
      body: JSON.stringify({
        ...data,
        email: normalizeEmailWrapper(data.email),
      }),
      headers: { "Content-Type": "application/json", ...csrfHeader },
      credentials: "include",
    },
  );

  emitInfo({
    message: "Register Submit",
    status: resp.status,
    data: JSON.stringify(data),
  });

  if (resp.ok) {
    redirect("/");
  }
  return resp.json();
}
