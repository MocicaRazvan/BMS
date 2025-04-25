"use server";
import { getUser } from "@/lib/user";
import { getCsrfNextAuthHeader } from "@/actions/get-csr-next-auth";
import fetchFactory from "@/lib/fetchers/fetchWithRetry";

enum TOXIC_REASON {
  LANGUAGE = "language",
  TOXICITY = "toxicity",
  NONE = "none",
}

interface ToxicResponse {
  failure: boolean;
  reason: TOXIC_REASON;
  message: string;
}

const springUrl = process.env.NEXT_PUBLIC_SPRING!;

export async function getToxicity(text: string): Promise<ToxicResponse> {
  const [user, csrfHeader] = await Promise.all([
    getUser(),
    getCsrfNextAuthHeader(),
  ]);

  const response = await fetchFactory(fetch)(springUrl + "/toxicity/isToxic", {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${user.token}`,
      ...csrfHeader,
    },
    body: JSON.stringify({ text }),
    credentials: "include",
  });
  if (!response.ok) {
    return {
      failure: true,
      reason: TOXIC_REASON.LANGUAGE,
      message: "Failed to detect toxicity",
    };
  }
  return (await response.json()) as ToxicResponse;
}
