"use client";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { Download } from "lucide-react";
import { cn } from "@/lib/utils";
import Loader from "@/components/ui/spinner";
import useExportTable, {
  UseExportTableArgs,
} from "@/hoooks/table/use-export-table";
import { Row } from "@tanstack/react-table";
import PulsatingButton from "@/components/magicui/pulsating-button";

interface Props<T extends Record<string, any>, V = any>
  extends UseExportTableArgs<T, V> {
  selectedLength: number;
  downloadSelected: string;
  persistedRows: Row<T>[];
}
export default function ExportTableDropDown<
  T extends Record<string, any>,
  V = any,
>({
  table,
  downloadSelected,
  selectedLength,
  persistedRows,
  ...rest
}: Props<T, V>) {
  const {
    exportPdf,
    exportCsv,
    isPdfExporting,
    isOneExporting,
    isCsvExporting,
  } = useExportTable<T, V>({
    table,
    ...rest,
  });
  return (
    <div className="flex items-center justify-center gap-4">
      {selectedLength > 0 && (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <div>
              <PulsatingButton className="flex items-center justify-center py-2 px-1.5">
                <div className="flex items-center justify-center gap-2">
                  {downloadSelected}
                  <Download className="h-5 w-5" />
                </div>
              </PulsatingButton>
            </div>
          </DropdownMenuTrigger>

          <DropdownMenuContent align="end">
            <DropdownMenuItem
              className={cn(
                "cursor-pointer py-2 flex items-center justify-center h-10",
                isOneExporting && "cursor-wait bg-muted",
              )}
              onSelect={(e) => {
                e.preventDefault();
                exportCsv(persistedRows);
              }}
              disabled={isOneExporting}
            >
              {!isCsvExporting ? "CSV" : <Loader className="my-0 w-5 h-5" />}
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              className={cn(
                "cursor-pointer py-2 flex items-center justify-center h-10",
                isOneExporting && "cursor-wait bg-muted",
              )}
              onSelect={(e) => {
                e.preventDefault();
                exportPdf(persistedRows);
              }}
              disabled={isOneExporting}
            >
              {!isPdfExporting ? "PDF" : <Loader className="my-0 w-5 h-5" />}
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      )}
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button
            variant="outline"
            className="flex items-center justify-center"
            size="icon"
          >
            <Download />
          </Button>
        </DropdownMenuTrigger>

        <DropdownMenuContent align="end">
          <DropdownMenuItem
            className={cn(
              "cursor-pointer py-2 flex items-center justify-center h-10",
              isOneExporting && "cursor-wait bg-muted",
            )}
            onSelect={(e) => {
              e.preventDefault();
              exportCsv(table.getFilteredRowModel().rows);
            }}
            disabled={isOneExporting}
          >
            {!isCsvExporting ? "CSV" : <Loader className="my-0 w-5 h-5" />}
          </DropdownMenuItem>
          <DropdownMenuSeparator />
          <DropdownMenuItem
            className={cn(
              "cursor-pointer py-2 flex items-center justify-center h-10",
              isOneExporting && "cursor-wait bg-muted",
            )}
            onSelect={(e) => {
              e.preventDefault();
              exportPdf(table.getFilteredRowModel().rows);
            }}
            disabled={isOneExporting}
          >
            {!isPdfExporting ? "PDF" : <Loader className="my-0 w-5 h-5" />}
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  );
}
