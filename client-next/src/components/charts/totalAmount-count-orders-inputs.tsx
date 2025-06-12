import {
  type DropDownTexts,
  type TotalAmountCountOrdersTexts,
} from "@/components/charts/totalAmount-count-ordres";
import { Button } from "@/components/ui/button";
import { useEffect } from "react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export const CountTotalAmountRadioOptions = [
  "both",
  "count",
  "totalAmount",
] as const;

export type CountTotalAmountRadioOptionsType =
  (typeof CountTotalAmountRadioOptions)[number];

export interface TrendLineButtonProps extends TotalAmountCountOrdersTexts {
  showTrendLine: boolean;
  onShowTrendLineChange: (showTrendLine: boolean) => void;
  hide?: boolean;
}

export interface DropDownMenuCountTotalAmountSelectProps extends DropDownTexts {
  radioOption: CountTotalAmountRadioOptionsType;
  onRadioOptionChange: (option: CountTotalAmountRadioOptionsType) => void;
  showBoth?: boolean;
}

export function DropDownMenuCountTotalAmountSelect({
  countLabel,
  totalAmountLabel,
  bothLabel,
  radioOption,
  onRadioOptionChange,
  showBoth = true,
}: DropDownMenuCountTotalAmountSelectProps) {
  const label =
    radioOption === "both"
      ? bothLabel
      : radioOption === "count"
        ? countLabel
        : totalAmountLabel;
  useEffect(() => {
    if (!showBoth && radioOption === "both") {
      onRadioOptionChange("count");
    }
  }, [onRadioOptionChange, radioOption, showBoth]);
  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button variant="outline">{label}</Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent className="w-56">
        <DropdownMenuRadioGroup
          value={radioOption}
          onValueChange={(e) =>
            onRadioOptionChange(e as CountTotalAmountRadioOptionsType)
          }
        >
          {showBoth && (
            <DropdownMenuRadioItem value={CountTotalAmountRadioOptions[0]}>
              {bothLabel}
            </DropdownMenuRadioItem>
          )}
          <DropdownMenuRadioItem value={CountTotalAmountRadioOptions[1]}>
            {countLabel}
          </DropdownMenuRadioItem>
          <DropdownMenuRadioItem value={CountTotalAmountRadioOptions[2]}>
            {totalAmountLabel}
          </DropdownMenuRadioItem>
        </DropdownMenuRadioGroup>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}

export const TrendLineButton = ({
  showTrendLine,
  onShowTrendLineChange,
  showTrendLineLabel,
  hideTrendLineLabel,
  hide,
}: TrendLineButtonProps) => {
  if (hide) return null;
  return (
    <Button
      variant="outline"
      className="min-w-[180px]"
      onClick={() => onShowTrendLineChange(!showTrendLine)}
    >
      {showTrendLine ? hideTrendLineLabel : showTrendLineLabel}
    </Button>
  );
};
