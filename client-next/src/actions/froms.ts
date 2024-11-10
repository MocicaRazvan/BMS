"use server";

import { RegisterType } from "@/types/forms";
import { BaseError } from "@/types/responses";
import { redirect } from "@/navigation";
import { emitInfo } from "@/logger";

export async function registerSubmit(data: RegisterType): Promise<BaseError> {
  const resp = await fetch(process.env.NEXT_PUBLIC_SPRING! + "/auth/register", {
    method: "POST",
    body: JSON.stringify(data),
    headers: { "Content-Type": "application/json" },
  });

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
