import { redirect } from "@/navigation/navigation";

export const dynamic = "force-dynamic";

export default async function NotFoundPage() {
  redirect("/not-found");
}
