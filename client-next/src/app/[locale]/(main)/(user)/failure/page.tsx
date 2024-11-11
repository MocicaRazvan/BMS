import { redirect } from "@/navigation";
import { RedirectType } from "next/navigation";

export default async function FailureRedirect() {
  return redirect("/", RedirectType.replace);
}
