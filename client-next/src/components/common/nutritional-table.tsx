import {
  Table,
  TableBody,
  TableCaption,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { getCalories } from "@/types/responses";
import { Badge } from "@/components/ui/badge";
import { IngredientTableColumnTexts } from "@/components/table/ingredients-table";
import {
  IngredientNutritionalFactResponse,
  NutritionalFactResponse,
} from "@/types/dto";
import { cn } from "@/lib/utils";
import { Fragment } from "react";

const tableColsKeys: (keyof NutritionalFactResponse &
  keyof IngredientTableColumnTexts)[] = [
  "fat",
  "saturatedFat",
  "carbohydrates",
  "sugar",
  "protein",
  "salt",
] as const;
const tableCols: (keyof IngredientTableColumnTexts)[] = [
  ...tableColsKeys,
  "calories",
  "unit",
] as const;
export interface NutritionalTableTexts {
  ingredientColumnTexts: IngredientTableColumnTexts;
  tableCaption: string;
}

interface Props extends NutritionalTableTexts {
  ing: IngredientNutritionalFactResponse;
  showUnit?: boolean;
  tableClassName?: string;
}
export default function NutritionalTable({
  tableCaption,
  ingredientColumnTexts,
  ing,
  showUnit = true,
  tableClassName,
}: Props) {
  const finalCols = showUnit
    ? tableCols
    : tableCols.filter((col) => col !== "unit");
  return (
    <>
      <Table className={cn(`hidden lg:table w-full`, tableClassName)}>
        <TableCaption className="mt-2">{tableCaption}</TableCaption>
        <TableHeader>
          <TableRow>
            {finalCols.map((col, i) => (
              <TableHead key={col + "lg" + i}>
                {" "}
                {ingredientColumnTexts[col] as string}{" "}
              </TableHead>
            ))}
          </TableRow>
        </TableHeader>
        <TableBody>
          <TableRow>
            {tableColsKeys.map((col, i) => (
              <TableCell key={col + i}>
                {typeof ing.nutritionalFact[col] === "number"
                  ? ing.nutritionalFact[col]?.toFixed(2)
                  : ing.nutritionalFact[col]}
              </TableCell>
            ))}
            <TableCell>{getCalories(ing)}</TableCell>
            {showUnit && (
              <TableCell>
                {" "}
                <Badge
                  variant={
                    ing.nutritionalFact.unit === "GRAM"
                      ? "secondary"
                      : "default"
                  }
                >
                  {ing.nutritionalFact.unit}
                </Badge>
              </TableCell>
            )}
          </TableRow>
        </TableBody>
      </Table>
      <div className="lg:hidden space-y-8 px-6 w-full">
        <p className="mt-4 text-sm text-muted-foreground">{tableCaption}</p>

        {tableColsKeys.map((col, i) => (
          <Fragment key={col + "sm" + i}>
            <div key={col + "sm" + i} className="flex justify-between">
              <p>{ingredientColumnTexts[col] as string}</p>
              <p>
                {typeof ing.nutritionalFact[col] === "number"
                  ? ing.nutritionalFact[col]?.toFixed(2)
                  : ing.nutritionalFact[col]}
              </p>
            </div>
            <hr className="border" />
          </Fragment>
        ))}
        <div className="flex justify-between">
          <p>{ingredientColumnTexts.calories}</p>
          <p>{getCalories(ing)}</p>
        </div>
        <hr className="border" />
        {showUnit && (
          <div className="flex justify-between">
            <p>{ingredientColumnTexts.unit}</p>
            <Badge
              variant={
                ing.nutritionalFact.unit === "GRAM" ? "secondary" : "default"
              }
            >
              {ing.nutritionalFact.unit}
            </Badge>
          </div>
        )}
      </div>
    </>
  );
}
