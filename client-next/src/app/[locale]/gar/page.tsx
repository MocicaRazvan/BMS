import GarBtn from "@/app/[locale]/gar/gar-btn";
import { getUser } from "@/lib/user";
import { getDayFromTexts } from "@/texts/components/forms";
import DayForm from "@/components/forms/day-form";
import LoadingSpinner from "@/components/common/loading-spinner";
import { Suspense } from "react";

export default async function Page() {
  const [authUser, dayFormTexts] = await Promise.all([
    getUser(),
    getDayFromTexts("create"),
  ]);

  return (
    <main className="flex items-center justify-center px-6 py-10">
      <Suspense fallback={<LoadingSpinner />}>
        <DayForm
          {...dayFormTexts}
          authUser={authUser}
          path={"/days/create/meals"}
        />
      </Suspense>
    </main>
  );
}
