"use client";

import {
  ColumnDef,
  ColumnFiltersState,
  flexRender,
  getCoreRowModel,
  Row,
  useReactTable,
  VisibilityState,
} from "@tanstack/react-table";

import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

import { Button } from "@/components/ui/button";
import {
  Dispatch,
  ReactNode,
  SetStateAction,
  useCallback,
  useState,
} from "react";
import { PageInfo } from "@/types/dto";

import {
  DropdownMenu,
  DropdownMenuCheckboxItem,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  DataTablePagination,
  DataTablePaginationTexts,
} from "./data-table-pagination";
import SearchInput, { SearchInputProps } from "@/components/forms/input-serach";
import RadioSort, {
  RadioSortProps,
  RadioSortTexts,
} from "@/components/common/radio-sort";
import { download, generateCsv, mkConfig } from "export-to-csv";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import { Download } from "lucide-react";
import { motion } from "framer-motion";
import { Skeleton } from "@/components/ui/skeleton";
import { cn, fromStringOfDotToObjectValue } from "@/lib/utils";
import { format, parseISO } from "date-fns";
import { useFormatter } from "next-intl";

export interface TableFilter {
  key: string;
  value: string;
  placeholder: string;
}

export interface DataTableTexts {
  dataTablePaginationTexts: DataTablePaginationTexts;
  radioSortTexts: RadioSortTexts;
  columnsLabel: string;
  noResults: string;
  exportLabel: string;
}

interface DataTableProps<TData, TValue> extends DataTableTexts {
  columns: ColumnDef<TData, TValue>[];
  data: TData[];
  isFinished: boolean;
  pageInfo: PageInfo;
  fileName: string;
  setPageInfo: Dispatch<SetStateAction<PageInfo>>;
  searchInputProps: SearchInputProps;
  radioSortProps: Omit<RadioSortProps, "noSort">;
  extraCriteria?: ReactNode;
  sizeOptions?: number[];
  hidePDFColumnIds?: string[];
  specialPDFColumns?: {
    key: string;
    handler: (value: object) => string;
  }[];
}

const MotionTableRow = motion(TableRow);

export function DataTable<TData extends Record<string, any>, TValue>({
  columns,
  data,
  pageInfo,
  setPageInfo,
  dataTablePaginationTexts,
  noResults,
  isFinished,
  columnsLabel,
  searchInputProps,
  radioSortProps,
  extraCriteria,
  radioSortTexts,
  exportLabel,
  sizeOptions,
  fileName,
  hidePDFColumnIds = [],
  specialPDFColumns = [],
}: DataTableProps<TData, TValue>) {
  const formatIntl = useFormatter();

  const csvConfig = mkConfig({
    fieldSeparator: ",",
    filename: `${fileName}-${new Date().toISOString()}`,
    decimalSeparator: ".",
    showColumnHeaders: true,
  });

  const getExport = useCallback(
    (rows: Row<TData>[]) => {
      const tableColumnHeaders = columns
        .filter((c) =>
          table
            .getAllColumns()
            .filter((column) => column.getIsVisible())
            .map((column) => column.id)
            .includes(c?.id || ""),
        )
        .reduce(
          (acc, cur) => {
            console.log("cur", cur);
            const obj = {
              id: "",
              accessorKey: "",
            };

            if (cur.id) {
              if (cur.id === "actions" || hidePDFColumnIds.includes(cur.id)) {
                return acc;
              }
              obj.id = cur.id;
            }
            if ("accessorKey" in cur) {
              obj.accessorKey = String(cur.accessorKey);
            }
            acc.push(obj);
            return acc;
          },
          [] as {
            id: string;
            accessorKey: string;
          }[],
        );

      const lastLengthColumns = ["userLikes", "userDislikes"];
      const dateColumns = ["createdAt", "updatedAt"];
      const numberColumns = ["price", "total"];
      const tableRows = rows.map(({ original }) =>
        tableColumnHeaders.map((h) => {
          const {
            isLastLengthColumn,
            isDateColumn,
            isNumberColumn,
            isSpecial,
          } = h.accessorKey.split(".").reduce(
            (acc, key) => {
              if (
                lastLengthColumns.includes(key) ||
                key.toLowerCase().includes("ids")
              ) {
                acc.isLastLengthColumn = true;
              }
              if (dateColumns.includes(key)) {
                acc.isDateColumn = true;
              }
              if (numberColumns.includes(key)) {
                acc.isNumberColumn = true;
              }

              if (specialPDFColumns.find((s) => s.key === h.accessorKey)) {
                acc.isSpecial = true;
              }

              return acc;
            },
            {
              isLastLengthColumn: false,
              isDateColumn: false,
              isNumberColumn: false,
              isSpecial: false,
            },
          );
          let value = fromStringOfDotToObjectValue(
            h.accessorKey,
            original,
            isLastLengthColumn,
          );
          if (Array.isArray(value)) {
            value = value.join(", ");
          } else if (isDateColumn) {
            value = format(parseISO(value), "dd/MM/yyyy");
          } else if (isNumberColumn) {
            value = formatIntl.number(value, {
              style: "currency",
              currency: "EUR",
              maximumFractionDigits: 2,
            });
          } else if (typeof value === "object" && isSpecial) {
            value = specialPDFColumns
              .find((s) => s.key === h.accessorKey)
              ?.handler(value);
          }
          return value as string | number;
        }),
      );
      return () => ({
        tableColumnHeaders,
        tableRows,
      });
    },
    [columns, formatIntl, hidePDFColumnIds, specialPDFColumns],
  );

  const exportCsv = useCallback(
    (rows: Row<TData>[]) => {
      const { tableColumnHeaders, tableRows } = getExport(rows)();
      const formattedRows = tableRows.map((row) =>
        tableColumnHeaders.reduce(
          (acc, cur, index) => {
            acc[cur.id] = row[index];
            // optimization
            return acc;
          },
          {} as Record<string, string | number>,
        ),
      );
      const finalConfig = {
        ...csvConfig,
        columnHeaders: tableColumnHeaders.map((h) => h.id),
      };
      download(finalConfig)(generateCsv(finalConfig)(formattedRows));
    },
    [csvConfig, getExport],
  );

  const exportPdf = useCallback(
    (rows: Row<TData>[]) => {
      const doc = new jsPDF({
        orientation: "landscape",
      });

      const { tableColumnHeaders, tableRows } = getExport(rows)();
      autoTable(doc, {
        head: [tableColumnHeaders.map((h) => h.id)],
        body: tableRows,
        useCss: true,
        theme: "striped",
        headStyles: { fillColor: [22, 160, 133] },
        bodyStyles: { fillColor: [244, 244, 244] },
        alternateRowStyles: { fillColor: [255, 255, 255] },
        startY: 20,
        margin: { top: 20, bottom: 20 },
        styles: {
          fontSize: 10,
          cellPadding: 4,
          overflow: "linebreak",
          minCellWidth: 20,
        },
      });
      doc.save(`${fileName}-${new Date().toISOString()}.pdf`);
    },
    [columns, fileName, getExport],
  );

  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({});
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);

  const table = useReactTable({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
    manualPagination: true,
    autoResetPageIndex: true,
    manualFiltering: true,
    enableRowSelection: false,
    enableMultiRowSelection: false,
    onColumnVisibilityChange: setColumnVisibility,
    onColumnFiltersChange: setColumnFilters,
    state: {
      columnVisibility,
      columnFilters,
    },
  });

  return (
    <div className="mb-2">
      <div className="flex flex-col lg:flex-row items-start py-4 flex-wrap gap-10  ">
        <div className="flex-1 flex items-start flex-col lg:flex-row justify-start gap-4">
          <div className="order-0">
            <SearchInput {...searchInputProps} />
          </div>
          <div className="order-10">
            <RadioSort {...radioSortProps} {...radioSortTexts} />
          </div>
          {extraCriteria && extraCriteria}
        </div>
        <div className=" w-full lg:w-fit flex items-center justify-between gap-4 ml-auto">
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                variant="outline"
                className="flex items-center justify-center"
                size="icon"
              >
                {/*{exportLabel}*/}
                <Download />
              </Button>
            </DropdownMenuTrigger>

            <DropdownMenuContent align="end">
              <DropdownMenuItem
                className="cursor-pointer py-2 "
                onClick={() => exportCsv(table.getFilteredRowModel().rows)}
              >
                {"CSV"}
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                className="cursor-pointer py-2 "
                onClick={() => exportPdf(table.getFilteredRowModel().rows)}
              >
                {"PDF"}
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="outline" className="ml-auto">
                {columnsLabel}
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="p-1">
              {table
                .getAllColumns()
                .filter((column) => column.getCanHide())
                .map((column) => {
                  return (
                    <DropdownMenuCheckboxItem
                      key={column.id}
                      className="capitalize cursor-pointer"
                      checked={column.getIsVisible()}
                      onCheckedChange={(value) =>
                        column.toggleVisibility(value)
                      }
                    >
                      {column.id}
                    </DropdownMenuCheckboxItem>
                  );
                })}
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>
      <div className="rounded-md border">
        {!isFinished ? (
          <div className="w-full h-full min-h-[60vh] space-y-4 p-2">
            {Array.from({ length: 10 }).map((_, i) => (
              <div key={i} className={cn(" h-[6vh] ")}>
                <Skeleton className="w-full  h-[6vh]" />
              </div>
            ))}
            {/*<Loader />*/}
          </div>
        ) : (
          <Table wrapperClassName="lg:overflow-visible  ">
            <TableHeader className="bg-accent/50">
              {table.getHeaderGroups().map((headerGroup) => (
                <TableRow key={headerGroup.id}>
                  {headerGroup.headers.map((header) => {
                    return (
                      <TableHead key={header.id}>
                        {header.isPlaceholder
                          ? null
                          : flexRender(
                              header.column.columnDef.header,
                              header.getContext(),
                            )}
                      </TableHead>
                    );
                  })}
                </TableRow>
              ))}
            </TableHeader>
            <TableBody className="relative !overflow-y-visible">
              {isFinished && table.getRowModel().rows?.length ? (
                table.getRowModel().rows.map((row, i) => (
                  <MotionTableRow
                    key={row.id}
                    data-state={row.getIsSelected() && "selected"}
                    className="lg:hover:relative z-20  hover:bg-muted "
                    initial={{ opacity: 0, y: 20, scale: 0.8 }}
                    animate={{ opacity: 1, y: 0, scale: 1 }}
                    transition={{
                      duration: 0.1,
                      // delay: i * 0.11,
                      ease: "linear",
                    }}
                    whileHover={{
                      scale: 1.02,
                      transition: { duration: 0.1, delay: 0 },
                    }}
                  >
                    {row.getVisibleCells().map((cell) => (
                      <TableCell key={cell.id}>
                        {flexRender(
                          cell.column.columnDef.cell,
                          cell.getContext(),
                        )}
                      </TableCell>
                    ))}
                  </MotionTableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell
                    colSpan={columns.length}
                    className="h-24 text-center"
                  >
                    {noResults}
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        )}
      </div>

      <div className="mt-2 lg:mt-4">
        <DataTablePagination
          // table={table}
          // totalPages={totalPages}
          // totalElements={totalElements}
          // setPagination={setPagination}
          // pagination={pagination}
          pageInfo={pageInfo}
          setPageInfo={setPageInfo}
          {...dataTablePaginationTexts}
          sizeOptions={sizeOptions}
        />
      </div>
    </div>
  );
}
