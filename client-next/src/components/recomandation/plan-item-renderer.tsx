import { PlanReposeWithSimilarity } from "@/types/dto";
import PlanType from "@/components/plans/plan-type";
import { useFormatter } from "next-intl";
import { Link } from "@/navigation";

export interface PlanItemRendererTexts {
  objective: string;
}
interface Props {
  item: PlanReposeWithSimilarity;
  texts: PlanItemRendererTexts;
}

export default function PlanItemRenderer({ item, texts }: Props) {
  const formatIntl = useFormatter();
  return (
    <div className="w-full h-full space-y-5">
      <div className="flex items-center justify-between w-full">
        <Link
          href={`/plans/single/${item.id}`}
          className="font-bold text-lg hover:underline max-w-44 xl:max-w-64 2xl:max-w-72 text-nowrap overflow-x-hidden text-ellipsis"
        >
          {item.title}
        </Link>
      </div>
      <div className="flex items-center justify-between w-full">
        <div className="flex flex-col items-start justify-center">
          <span className="font-bold">{texts.objective}</span>
          <span>{item.objective.replace("_", " ")}</span>
        </div>
        <div className="flex flex-col items-start justify-center gap-1">
          <PlanType type={item.type} />
          <span className="font-bold max-w-44 overflow-x-hidden text-ellipsis">
            {formatIntl.number(item.price, {
              style: "currency",
              currency: "EUR",
              maximumFractionDigits: 2,
            })}
          </span>
        </div>
      </div>
    </div>
  );
}
