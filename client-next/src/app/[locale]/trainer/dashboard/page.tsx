import { Locale } from "@/navigation";
import { unstable_setRequestLocale } from "next-intl/server";

interface Props {
  params: { locale: Locale };
}

export default async function TrainerDashboard({ params: { locale } }: Props) {
  unstable_setRequestLocale(locale);
  return <div>Trainer Dashboard</div>;
}
