import { vectorStoreInstance, VectorStoreSingleton } from "@/lib/langchain";
import GarBtn from "@/app/[locale]/gar/gar-btn";

export default async function Page() {
  const embedings = await VectorStoreSingleton.gar(4096);
  console.log(embedings);
  return (
    <div>
      <GarBtn items={embedings} />{" "}
    </div>
  );
}
