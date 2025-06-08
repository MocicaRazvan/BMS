"use client";

import {
  ColumnDef,
  ColumnFiltersState,
  flexRender,
  getCoreRowModel,
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
  memo,
  MutableRefObject,
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
import { AnimatePresence, motion } from "framer-motion";
import { Skeleton } from "@/components/ui/skeleton";
import { UseExportTableArgs } from "@/hoooks/table/use-export-table";
import { Checkbox } from "@/components/ui/checkbox";
import SelectedRows, {
  SelectedRowsTexts,
} from "@/components/table/selected-rows";
import {
  LinkedChartComponent,
  LinkedChartProps,
  LinkedChartTexts,
} from "@/components/charts/linked-chart";
import { ColumnResizer } from "@/components/table/column-resizer";
import { useDraggable } from "react-use-draggable-scroll";
import { cn, isDeepEqual } from "@/lib/utils";
import useTableColResize, {
  calcVar,
  createColumnKeySize,
  createColumnLeft,
  createHeaderKeySize,
  createHeaderLeft,
} from "@/hoooks/table/use-table-col-resize";
import useTableRowSelection from "@/hoooks/table/use-table-row-selection";
import dynamic from "next/dynamic";
import ExportTableDropDown from "@/components/table/export-table-dropdown";

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
  linkedChartTexts: Omit<LinkedChartTexts, "title"> & {
    persisted: string;
    table: string;
  };
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
  chartProps?: Omit<Partial<LinkedChartProps<TData>>, "data" | "texts">;
  showChart?: boolean;
  stickyColumnIds?: string[];
}

const DynamicChart = dynamic(
  () =>
    import("@/components/charts/linked-chart").then(
      (m) => m.MemoizedLinkedChart,
    ),
  {
    ssr: false,
    loading: () => <Skeleton className="w-full h-full" />,
  },
) as LinkedChartComponent;

export function DataTable<TData extends Record<string, any>, TValue = any>({
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
  chartProps,
  linkedChartTexts,
  showChart = false,
  stickyColumnIds = ["ID", "id", "select"],
}: DataTableProps<TData, TValue>) {
  const [columnVisibility, setColumnVisibility] = useState<VisibilityState>({});
  const [columnFilters, setColumnFilters] = useState<ColumnFiltersState>([]);
  const scrollRef =
    useRef<HTMLDivElement>() as MutableRefObject<HTMLInputElement>;
  const { events } = useDraggable(scrollRef, {
    isMounted: true,
    safeDisplacement: 15,
  });
  const {
    rowSelection,
    setRowSelection,
    persistedRows,
    updatePersistedRows,
    selectedLength,
    clearRowSelection,
  } = useTableRowSelection<TData>();

  const finalColumns: ColumnDef<TData, TValue>[] = useMemo(
    () => [
      {
        id: "select",
        enableResizing: false,
        size: 35,
        minSize: 35,
        maxSize: 35,
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
          <div className="w-full pe-1">
            <Checkbox
              checked={row.getIsSelected()}
              onCheckedChange={(value) => row.toggleSelected(!!value)}
              aria-label="Select row"
            />
          </div>
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
    columnResizeMode: "onChange",
    getRowId,
    defaultColumn: {
      minSize: 35,
      maxSize: 800,
      enableResizing: false,
    },
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

  const [columnSizeVars, leftSizeVars] = useTableColResize({ table });

  const chartData: {
    data: TData[];
    type: Extract<
      keyof DataTableTexts["linkedChartTexts"],
      "persisted" | "table"
    >;
  } = useMemo(
    () =>
      persistedRows.length > 0
        ? { data: persistedRows.map((r) => r.original), type: "persisted" }
        : {
            data: table.getFilteredRowModel().rows.map((row) => row.original),
            type: "table",
          },
    [persistedRows, table, data.length],
  );
  useEffect(() => {
    updatePersistedRows({
      getRowId,
      table,
    });
  }, [getRowId, updatePersistedRows, table]);

  return (
    <div className="mb-2">
      <div className="flex flex-col lg:flex-row items-start py-4 flex-wrap gap-10">
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
        <div className="w-full lg:w-fit flex items-center justify-between gap-4 ml-auto">
          <ExportTableDropDown
            lastLengthColumns={lastLengthColumns}
            dateColumns={dateColumns}
            currencyColumns={currencyColumns}
            fileName={fileName}
            hidePDFColumnIds={hidePDFColumnIds}
            table={table}
            columns={finalColumns}
            specialPDFColumns={specialPDFColumns}
            downloadSelected={downloadSelected}
            persistedRows={persistedRows}
            selectedLength={selectedLength}
          />
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
      {rangeDateFilter && <div className="w-full mb-4">{rangeDateFilter}</div>}
      <div className="rounded-md border relative p-0">
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
                onCheckedChange={clearRowSelection}
                aria-label="Unselect all"
              />
            </motion.div>
          )}
        </AnimatePresence>

        <Table
          wrapperClassName="max-w-full scrollbar-hide"
          style={{
            ...columnSizeVars,
            ...leftSizeVars,
            width: table.getTotalSize(),
            minWidth: "100%",
            userSelect: "none",
          }}
          wrapperRef={scrollRef}
          {...events}
        >
          <TableHeader
            className="bg-accent/50 w-full"
            onMouseDown={(e) => {
              e.stopPropagation();
            }}
          >
            {table.getHeaderGroups().map((headerGroup) => (
              <TableRow key={headerGroup.id} className="bg-muted">
                {headerGroup.headers.map((header) => {
                  const isSticky = stickyColumnIds?.includes(header.column.id);
                  return (
                    <TableHead
                      key={header.id}
                      className={cn(
                        "relative group",
                        isSticky && "sticky z-10 bg-inherit",
                      )}
                      style={{
                        width: calcVar(createHeaderKeySize(header?.id)),
                        ...(isSticky && {
                          left: calcVar(createHeaderLeft(header?.id)),
                        }),
                      }}
                    >
                      {header.isPlaceholder
                        ? null
                        : flexRender(
                            header.column.columnDef.header,
                            header.getContext(),
                          )}
                      <ColumnResizer header={header} />
                    </TableHead>
                  );
                })}
              </TableRow>
            ))}
          </TableHeader>
          {table.getState().columnSizingInfo.isResizingColumn ? (
            <MemoizedTableBody
              table={table}
              isFinished={isFinished}
              noResults={noResults}
              finalColumns={finalColumns}
              stickyColumnIds={stickyColumnIds}
            />
          ) : (
            <DataTableBody
              table={table}
              isFinished={isFinished}
              noResults={noResults}
              finalColumns={finalColumns}
              stickyColumnIds={stickyColumnIds}
            />
          )}
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
      {showChart && (
        <div
          className={cn(
            "h-[500px] md:h-[625px] mt-12",
            isFinished && chartData.data.length === 0 && "hidden",
          )}
        >
          {chartData.data.length > 0 ? (
            <DynamicChart
              data={chartData.data}
              columns={columns}
              dateField="createdAt"
              chartType="bar"
              texts={{
                ...linkedChartTexts,
                title: linkedChartTexts[chartData.type],
              }}
              {...chartProps}
            />
          ) : !isFinished ? (
            <Skeleton className="h-full w-full" />
          ) : (
            <></>
          )}
        </div>
      )}
    </div>
  );
}

interface DataTableBodyProp<TData extends Record<string, any>> {
  table: ReturnType<typeof useReactTable<TData>>;
  isFinished: boolean;
  noResults: string;
  finalColumns: ColumnDef<TData, any>[];
  stickyColumnIds?: string[];
}

function DataTableBody<TData extends Record<string, any>>({
  table,
  isFinished,
  noResults,
  finalColumns,
  stickyColumnIds,
}: DataTableBodyProp<TData>) {
  return (
    <TableBody className="relative w-full ">
      {!isFinished ? (
        Array.from({ length: 10 }).map((_, i) => (
          <TableRow key={`loading-${i}`} className="hover:bg-background">
            {finalColumns.map((_, j) => (
              <TableCell key={`loading-cell-${j}-row-${i}`}>
                <Skeleton className="w-full h-[33px] min-w-4" />
              </TableCell>
            ))}
          </TableRow>
        ))
      ) : table.getRowModel().rows?.length ? (
        table.getRowModel().rows.map((row, i) => (
          <TableRow
            key={row.id}
            data-state={row.getIsSelected() && "selected"}
            className="lg:hover:relative hover:bg-muted group"
          >
            {row.getVisibleCells().map((cell) => {
              const isSticky = stickyColumnIds?.includes(cell.column.id);
              return (
                <TableCell
                  key={cell.id}
                  className={cn(isSticky && "sticky left-0 z-10")}
                  style={{
                    width: calcVar(createColumnKeySize(cell.column.id)),
                    ...(isSticky && {
                      left: calcVar(createColumnLeft(cell.column.id)),
                    }),
                  }}
                >
                  <div className="group-hover:scale-[1.055] transition-transform duration-200 ease-in-out">
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </div>
                </TableCell>
              );
            })}
          </TableRow>
        ))
      ) : (
        <TableRow>
          <TableCell
            colSpan={finalColumns.length}
            className="h-24 text-center text-lg font-semibold"
          >
            {noResults}
          </TableCell>
        </TableRow>
      )}
    </TableBody>
  );
}

const MemoizedTableBody = memo(
  DataTableBody,
  (prev, next) =>
    prev.table.options.data === next.table.options.data &&
    prev.isFinished === next.isFinished &&
    prev.noResults === next.noResults &&
    prev.finalColumns === next.finalColumns &&
    isDeepEqual(prev.stickyColumnIds, next.stickyColumnIds),
) as typeof DataTableBody;

// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
MemoizedTableBody.displayName = "MemoizedTableBody";
