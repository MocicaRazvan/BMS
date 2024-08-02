"use client";

import {
  ColumnDef,
  flexRender,
  getCoreRowModel,
  useReactTable,
  VisibilityState,
  ColumnFiltersState,
  Row,
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
import Loader from "@/components/ui/spinner";
import SearchInput, { SearchInputProps } from "@/components/forms/input-serach";
import RadioSort, {
  RadioSortProps,
  RadioSortTexts,
} from "@/components/common/radio-sort";
import { mkConfig, generateCsv, download } from "export-to-csv";
import jsPDF from "jspdf";
import autoTable from "jspdf-autotable";
import { Download } from "lucide-react";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import { motion } from "framer-motion";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

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
}: DataTableProps<TData, TValue>) {
  const csvConfig = mkConfig({
    fieldSeparator: ",",
    filename: `${fileName}-${new Date().toISOString()}`,
    decimalSeparator: ".",
    useKeysAsHeaders: true,
  });
  const exportCsv = useCallback(
    (rows: Row<TData>[]) => {
      download(csvConfig)(
        generateCsv(csvConfig)(
          rows.map(({ original }) =>
            Object.entries(original).reduce(
              (acc, [key, value]) => ({
                ...acc,
                [key]: Array.isArray(value) ? value.join(", ") : value,
              }),
              {} as Record<string, any>,
            ),
          ),
        ),
      );
    },
    [csvConfig],
  );

  const exportPdf = useCallback(
    (rows: Row<TData>[]) => {
      const doc = new jsPDF();
      const tableColumnHeaders = columns.reduce((acc, cur) => {
        if (cur.id) {
          acc.push(cur.id as string);
        } else if ("accessorKey" in cur) {
          acc.push(cur.accessorKey as string);
        }
        return acc;
      }, [] as string[]);
      const tableRows = rows.map(({ original }) =>
        tableColumnHeaders.map((h) => {
          const value = original[h as keyof TData];
          return Array.isArray(value) ? value.join(", ") : value;
        }),
      );
      autoTable(doc, {
        head: [tableColumnHeaders],
        body: tableRows,
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
    [columns, fileName],
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
                      duration: 0.12,
                      delay: i * 0.14,
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
      {/* <div className="flex items-center justify-end space-x-2 py-4">
        <Button
          variant="outline"
          size="sm"
          onClick={() => table.previousPage()}
          disabled={!table.getCanPreviousPage()}
        >
          Previous
        </Button>
        <Button
          variant="outline"
          size="sm"
          onClick={() => table.nextPage()}
          disabled={!table.getCanNextPage()}
        >
          Next
        </Button>
      </div> */}

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
