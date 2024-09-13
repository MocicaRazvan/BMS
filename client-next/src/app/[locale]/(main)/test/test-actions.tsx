"use server";
import { revalidatePath } from "next/cache";

export async function invalidatePath() {
  revalidatePath("/[locale]/(main)/posts/single/[id]");
}
