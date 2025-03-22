"use client";

import {
  ColumnDef,
  ColumnFiltersState,
  flexRender,
  getCoreRowModel,
  Row,
  RowSelectionState,
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
  useEffect,
  useMemo,
  useRef,
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
import { Download } from "lucide-react";
import { AnimatePresence, motion } from "framer-motion";
import { Skeleton } from "@/components/ui/skeleton";
import useExportTable, {
  UseExportTableArgs,
} from "@/hoooks/table/use-export-table";
import { Checkbox } from "@/components/ui/checkbox";
import SelectedRows, {
  SelectedRowsTexts,
} from "@/components/table/selected-rows";
import PulsatingButton from "@/components/magicui/pulsating-button";

export interface TableFilter {
  key: string;
  value: string;
  placeholder: string;
}

export interface DataTableTexts {
  dataTablePaginationTexts: DataTablePaginationTexts;
  radioSortTexts: RadioSortTexts;
  selectedRowsTexts: SelectedRowsTexts;
  columnsLabel: string;
  noResults: string;
  exportLabel: string;
  downloadSelected: string;
}

interface DataTableProps<TData extends Record<string, any>, TValue>
  extends DataTableTexts,
    Omit<UseExportTableArgs<TData, TValue>, "table"> {
  columns: ColumnDef<TData, TValue>[];
  data: TData[];
  isFinished: boolean;
  pageInfo: PageInfo;
  setPageInfo: Dispatch<SetStateAction<PageInfo>>;
  searchInputProps: SearchInputProps;
  radioSortProps: Omit<RadioSortProps, "noSort">;
  extraCriteria?: ReactNode;
  rangeDateFilter?: ReactNode;
  sizeOptions?: number[];
  getRowId: (row: TData) => string;
  useRadioSort?: boolean;
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
  rangeDateFilter,
  lastLengthColumns = ["userLikes", "userDislikes"],
  dateColumns = ["createdAt", "updatedAt"],
  currencyColumns = ["price", "total"],
  getRowId,
  selectedRowsTexts,
  downloadSelected,
  useRadioSort = true,
}: DataTableProps<TData, TValue>) {
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({});
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
  const [rowSelection, setRowSelection] = useState<RowSelectionState>({});
  const persistedRowsRef = useRef<Row<TData>[]>([]);
  const selectedLength = useMemo(
    () => Object.keys(rowSelection).length,
    [rowSelection],
  );

  const finalColumns: ColumnDef<TData, TValue>[] = useMemo(
    () => [
      {
        id: "select",
        header: ({ table }) => (
          <Checkbox
            checked={
              table.getIsAllPageRowsSelected() ||
              (table.getIsSomePageRowsSelected() && "indeterminate")
            }
            onCheckedChange={(value) =>
              table.toggleAllPageRowsSelected(!!value)
            }
            aria-label="Select all"
          />
        ),
        cell: ({ row }) => (
          <Checkbox
            checked={row.getIsSelected()}
            onCheckedChange={(value) => row.toggleSelected(!!value)}
            aria-label="Select row"
          />
        ),
        enableSorting: false,
        enableHiding: false,
      },
      ...columns,
    ],
    [JSON.stringify(columns)],
  );

  const table = useReactTable({
    data,
    columns: finalColumns,
    getCoreRowModel: getCoreRowModel(),
    manualPagination: true,
    autoResetPageIndex: true,
    manualFiltering: true,
    enableRowSelection: true,
    enableMultiRowSelection: true,
    manualSorting: true,
    onColumnVisibilityChange: setColumnVisibility,
    onColumnFiltersChange: setColumnFilters,
    onRowSelectionChange: setRowSelection,
    getRowId,
    state: {
      columnVisibility,
      columnFilters,
      pagination: {
        pageSize: pageInfo.pageSize,
        pageIndex: pageInfo.currentPage,
      },
      rowSelection,
    },
  });

  useEffect(() => {
    const ids = Object.keys(rowSelection);
    const prev = persistedRowsRef.current;

    const prevMap = new Map(prev.map((row) => [getRowId(row.original), row]));
    const tableRows = table.getCoreRowModel().rows;
    const tableRowMap = new Map(
      tableRows.map((row) => [getRowId(row.original), row]),
    );

    persistedRowsRef.current = ids
      .map((id) => prevMap.get(id) || tableRowMap.get(id))
      .filter((row): row is Row<TData> => Boolean(row));
  }, [getRowId, rowSelection, table]);

  const { exportPdf, exportCsv } = useExportTable<TData, TValue>({
    lastLengthColumns,
    dateColumns,
    currencyColumns,
    fileName,
    hidePDFColumnIds,
    table,
    columns: finalColumns,
    specialPDFColumns,
  });

  return (
    <div className="mb-2">
      <div className="flex flex-col lg:flex-row items-start py-4 flex-wrap gap-10  ">
        <div className="flex-1 flex items-start flex-col lg:flex-row justify-start gap-4">
          <div className="order-0">
            <SearchInput {...searchInputProps} />
          </div>
          {useRadioSort && (
            <div className="order-10">
              <RadioSort {...radioSortProps} {...radioSortTexts} />
            </div>
          )}
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
          <div>
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
                    className="cursor-pointer py-2 "
                    onClick={() => exportCsv(persistedRowsRef.current)}
                  >
                    {"CSV"}
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    className="cursor-pointer py-2 "
                    onClick={() => exportPdf(persistedRowsRef.current)}
                  >
                    {"PDF"}
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            )}
          </div>
        </div>
      </div>
      {rangeDateFilter && <div className="w-full mb-4">{rangeDateFilter}</div>}
      <div className="rounded-md border relative">
        <AnimatePresence mode="wait">
          {selectedLength > 0 && (
            <motion.div
              className="hidden md:flex absolute top-0 h-12 -left-6  items-center justify-center z-[2]"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              key="unselect-all"
            >
              <Checkbox
                checked={selectedLength > 0}
                onCheckedChange={() => setRowSelection({})}
                aria-label="Unselect all"
              />
            </motion.div>
          )}
        </AnimatePresence>
        <Table wrapperClassName="lg:overflow-visible ">
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
          <TableBody className="relative !overflow-y-visible w-full">
            {!isFinished ? (
              Array.from({ length: 10 }).map((_, i) => (
                <TableRow key={`loading-${i}`}>
                  {finalColumns.map((_, j) => (
                    <TableCell key={`loading-cell-${j}-row-${i}`}>
                      <Skeleton className="w-full h-[5vh]" />
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : table.getRowModel().rows?.length ? (
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
                  colSpan={finalColumns.length}
                  className="h-24 text-center"
                >
                  {noResults}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>

      <div className="mt-2 lg:mt-4 flex flex-col md:flex-row items-center justify-between w-full">
        <SelectedRows
          selectedLength={selectedLength}
          totalLength={pageInfo.totalElements}
          texts={selectedRowsTexts}
        />
        <DataTablePagination
          pageInfo={pageInfo}
          setPageInfo={setPageInfo}
          {...dataTablePaginationTexts}
          sizeOptions={sizeOptions}
        />
      </div>
    </div>
  );
}
