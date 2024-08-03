"use server";

import { RegisterType } from "@/types/forms";
import { BaseError } from "@/types/responses";
import { redirect } from "@/navigation";

export async function registerSubmit(data: RegisterType): Promise<BaseError> {
  const resp = await fetch(process.env.NEXT_PUBLIC_SPRING! + "/auth/register", {
    method: "POST",
    body: JSON.stringify(data),
    headers: { "Content-Type": "application/json" },
  });
  if (resp.ok) {
    redirect("/");
  }
  return resp.json();
}
