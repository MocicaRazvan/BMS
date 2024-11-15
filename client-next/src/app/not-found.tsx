import { redirect } from "@/navigation";

export default async function NotFoundPage() {
  redirect("/not-found");
}
