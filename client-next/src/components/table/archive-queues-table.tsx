"use client";
import {
  Column,
  ColumnDef,
  FilterFn,
  flexRender,
  getCoreRowModel,
  getFilteredRowModel,
  getPaginationRowModel,
  getSortedRowModel,
  SortingFn,
  sortingFns,
  Table as TableType,
  useReactTable,
} from "@tanstack/react-table";
import {
  compareItems,
  RankingInfo,
  rankItem,
} from "@tanstack/match-sorter-utils";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { Button } from "@/components/ui/button";
import { ArrowDown, ArrowUpDown, Download } from "lucide-react";
import { useMemo, useState } from "react";
import useExportTable from "@/hoooks/table/use-export-table";
import { ContainerAction, NotifyContainerAction } from "@/types/dto";
import SearchInput from "@/components/forms/input-serach";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import {
  LinkedChart,
  LinkedChartTexts,
  timestampFilter,
} from "@/components/charts/linked-chart";
import useArchiveContainerNotifications from "@/hoooks/use-archive-container-notifications";
import { Checkbox } from "@/components/ui/checkbox";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
import { format } from "date-fns";
import {
  ChevronLeftIcon,
  ChevronRightIcon,
  DoubleArrowLeftIcon,
  DoubleArrowRightIcon,
} from "@radix-ui/react-icons";
import { DataTablePaginationTexts } from "@/components/table/data-table-pagination";
import { SelectedRowsTexts } from "@/components/table/selected-rows";
import PulsatingButton from "@/components/magicui/pulsating-button";
import { useTableSearchParams } from "tanstack-table-search-params";
import { useSearchParams } from "next/navigation";
import { usePathname } from "@/navigation";
import { stripNonAlphaNumeric } from "@/lib/utils";
import { containerActionColors } from "@/lib/constants";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

const fuzzyFilter: FilterFn<any> = (row, columnId, value, addMeta) => {
  const cleanedRowValue = stripNonAlphaNumeric(row.getValue(columnId) ?? "");
  const cleanedFilterValue = stripNonAlphaNumeric(value ?? "");

  const itemRank = rankItem(cleanedRowValue, cleanedFilterValue, {
    keepDiacritics: true,
  });
  addMeta({ itemRank });

  return itemRank.passed;
};
// eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain
const fuzzySort: SortingFn<any> = (rowA, rowB, columnId) => {
  let dir = 0;
  if (rowA.columnFiltersMeta[columnId]) {
    // @ts-expect-error: itemRank is expected in custom FilterMeta shape
    // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain,@typescript-eslint/no-non-null-assertion
    const rankA = rowA.columnFiltersMeta[columnId]?.itemRank!;

    // @ts-expect-error: itemRank is expected in custom FilterMeta shape
    // eslint-disable-next-line @typescript-eslint/no-non-null-asserted-optional-chain,@typescript-eslint/no-non-null-assertion
    const rankB = rowB.columnFiltersMeta[columnId]?.itemRank!;

    if (!rankA || !rankB) {
      return sortingFns.alphanumeric(rowA, rowB, columnId);
    }

    dir = compareItems(rankA as RankingInfo, rankB as RankingInfo);
  }
  return dir === 0 ? sortingFns.alphanumeric(rowA, rowB, columnId) : dir;
};

export interface ArchiveQueuesTableTexts {
  dataTablePaginationTexts: DataTablePaginationTexts;
  selectedRowsTexts: SelectedRowsTexts;
  linkedChartTexts: Omit<LinkedChartTexts, "title"> & {
    selected: string;
    default: string;
  };
  actionPlaceholder: string;
  queueNamePlaceholder: string;
  clearFilters: string;
  noResults: string;
  columns: {
    id: string;
    action: string;
    queueName: string;
    timestamp: string;
  };
  downloadSelected: string;
}
export interface ArchiveQueuesTableProps {
  texts: ArchiveQueuesTableTexts;
}

export default function ArchiveQueuesTable({ texts }: ArchiveQueuesTableProps) {
  const { authUser } = useAuthUserMinRole();

  const columns = useMemo<ColumnDef<NotifyContainerAction, any>[]>(
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
        enableColumnFilter: false,
      },
      {
        id: "id",
        accessorKey: "id",
        header: () => (
          <div className="font-bold text-lg text-left">{texts.columns.id}</div>
        ),
        cell: ({ row }) => (
          <OverflowTextTooltip
            text={row.original.id}
            triggerClassName="max-w-[320px]"
          />
        ),
        enableColumnFilter: false,
      },
      {
        id: "action",
        accessorKey: "action",
        header: ({ column }) => (
          <HeaderSortingButton column={column} text={texts.columns.action} />
        ),
        filterFn: fuzzyFilter,
        sortingFn: fuzzySort,
        cell: ({ row }) => (
          <p
            className={`text-${containerActionColors[row.original.action]} font-medium`}
          >
            {row.original.action.replace("_", " ")}
          </p>
        ),
      },
      {
        id: "queueName",
        accessorKey: "queueName",
        header: ({ column }) => (
          <HeaderSortingButton column={column} text={texts.columns.queueName} />
        ),
        filterFn: fuzzyFilter,
        sortingFn: fuzzySort,
        cell: ({ row }) => <p>{row.original.queueName}</p>,
      },
      {
        id: "timestamp",
        accessorKey: "timestamp",
        header: ({ column }) => (
          <HeaderSortingButton column={column} text={texts.columns.timestamp} />
        ),
        sortingFn: "datetime",
        cell: ({ row }) => {
          const formattedDate = format(
            new Date(row.original.timestamp) + "Z",
            "dd/MM/yyyy HH:mm:ss",
          );

          return <p className="text-left font-medium"> {formattedDate}</p>;
        },
        filterFn: timestampFilter,
      },
    ],
    [JSON.stringify(texts)],
  );

  const { notifications, isFinished } =
    useArchiveContainerNotifications(authUser);

  return (
    <div>
      <DataTable
        columns={columns}
        data={notifications}
        isFinished={isFinished}
        texts={texts}
      />
    </div>
  );
}
function DataTable({
  columns,
  data,
  isFinished,
  texts,
}: {
  data: NotifyContainerAction[];
  columns: ColumnDef<NotifyContainerAction>[];
  isFinished: boolean;
  texts: ArchiveQueuesTableTexts;
}) {
  const [rowSelection, setRowSelection] = useState({});
  const query = useSearchParams();
  const pathname = usePathname();
  const stateAndOnChanges = useTableSearchParams(
    {
      replace: (url) => {
        const searchParams = new URLSearchParams(url.split("?")[1]);
        window.history.replaceState(null, "", `?${searchParams.toString()}`);
      },
      query,
      pathname,
    },
    {
      defaultValues: {
        sorting: [{ id: "timestamp", desc: true }],
        pagination: {
          pageIndex: 0,
          pageSize: 10,
        },
      },
      debounceMilliseconds: 500,
    },
  );
  const table = useReactTable({
    data,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getPaginationRowModel: getPaginationRowModel(),
    getSortedRowModel: getSortedRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    onRowSelectionChange: setRowSelection,
    autoResetPageIndex: true,
    enableMultiSort: true,
    ...stateAndOnChanges,
    state: {
      ...stateAndOnChanges.state,
      rowSelection,
    },
  });

  const { exportPdf, exportCsv } = useExportTable<
    NotifyContainerAction,
    unknown
  >({
    lastLengthColumns: [],
    dateColumns: [],
    currencyColumns: [],
    fileName: "archive-queue",
    hidePDFColumnIds: ["select"],
    table,
    columns,
    specialPDFColumns: [],
  });
  return (
    <div>
      <div className="flex flex-col sm:flex-row items-center justify-between w-full py-4 ">
        <div className="flex flex-col sm:flex-row items-center justify-start gap-2.5">
          <SearchInput
            searchInputTexts={{
              placeholder: texts.actionPlaceholder,
            }}
            value={
              (table.getColumn("action")?.getFilterValue() as string) ?? ""
            }
            onChange={(event) =>
              table.getColumn("action")?.setFilterValue(event.target.value)
            }
            onClear={() => table.getColumn("action")?.setFilterValue("")}
            className="max-w-sm"
          />
          <SearchInput
            searchInputTexts={{
              placeholder: texts.queueNamePlaceholder,
            }}
            value={
              (table.getColumn("queueName")?.getFilterValue() as string) ?? ""
            }
            onChange={(event) =>
              table.getColumn("queueName")?.setFilterValue(event.target.value)
            }
            onClear={() => table.getColumn("queueName")?.setFilterValue("")}
            className="max-w-sm"
            autoFocus={false}
          />
        </div>
        <div className="flex items-center justify-end gap-2">
          <Button
            variant="outline"
            onClick={() => {
              // setColumnFilters([]);
              stateAndOnChanges.onColumnFiltersChange([]);
            }}
          >
            {texts.clearFilters}
          </Button>
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
          <div>
            {table.getFilteredSelectedRowModel().rows.length > 0 && (
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <div>
                    <PulsatingButton className="flex items-center justify-center py-2 px-1.5">
                      <div className="flex items-center justify-center gap-2">
                        {texts.downloadSelected}
                        <Download className="h-5 w-5" />
                      </div>
                    </PulsatingButton>
                  </div>
                </DropdownMenuTrigger>

                <DropdownMenuContent align="end">
                  <DropdownMenuItem
                    className="cursor-pointer py-2 "
                    onClick={() =>
                      exportCsv(table.getFilteredSelectedRowModel().rows)
                    }
                  >
                    {"CSV"}
                  </DropdownMenuItem>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    className="cursor-pointer py-2 "
                    onClick={() =>
                      exportPdf(table.getFilteredSelectedRowModel().rows)
                    }
                  >
                    {"PDF"}
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            )}
          </div>
        </div>
      </div>
      <div className="rounded-md border">
        <Table wrapperClassName="lg:overflow-visible ">
          <TableHeader className="bg-accent/50">
            {table.getHeaderGroups().map((headerGroup) => (
              <TableRow key={headerGroup.id} className="bg-muted">
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
                  {columns.map((_, j) => (
                    <TableCell
                      key={`loading-cell-${j}-row-${i}`}
                      className="py-5"
                    >
                      <Skeleton className="w-full h-[20px]" />
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : table.getRowModel().rows?.length ? (
              table.getRowModel().rows.map((row) => (
                <TableRow
                  key={row.id}
                  data-state={row.getIsSelected() && "selected"}
                  className="lg:hover:relative z-20  hover:bg-muted group"
                >
                  {row.getVisibleCells().map((cell) => (
                    <TableCell key={cell.id} className="py-5">
                      <div className="group-hover:scale-[1.055] transition-transform duration-200 ease-in-out">
                        {flexRender(
                          cell.column.columnDef.cell,
                          cell.getContext(),
                        )}
                      </div>
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell
                  colSpan={columns.length}
                  className="h-24 text-center"
                >
                  {texts.noResults}
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </div>
      <div className="mt-3">
        <DataTablePagination
          table={table}
          paginationTexts={texts.dataTablePaginationTexts}
          selectedRowsTexts={texts.selectedRowsTexts}
        />
      </div>
      <div className="h-[500px] md:h-[625px] mt-12">
        {table.getFilteredRowModel().rows.length > 0 ? (
          <LinkedChart
            data={
              table.getFilteredSelectedRowModel().rows.length > 0
                ? table
                    .getFilteredSelectedRowModel()
                    .rows.map((row) => row.original)
                : table.getFilteredRowModel().rows.map((row) => row.original)
            }
            columns={columns}
            dateField="timestamp"
            aggregatorConfig={{
              "#": (_) => 1,
              "#Start Cron": (r) =>
                r.action === ContainerAction.START_CRON ? 1 : 0,
              "#Stop": (r) => (r.action === ContainerAction.STOP ? 1 : 0),
              "#Start Manual": (r) =>
                r.action === ContainerAction.START_MANUAL ? 1 : 0,
            }}
            chartType="bar"
            texts={{
              ...texts.linkedChartTexts,
              title:
                table.getFilteredSelectedRowModel().rows.length > 0
                  ? texts.linkedChartTexts.selected
                  : texts.linkedChartTexts.default,
            }}
          />
        ) : (
          <Skeleton className="size-full" />
        )}
      </div>
    </div>
  );
}

function DataTablePagination<TData>({
  table,
  paginationTexts,
  selectedRowsTexts,
}: {
  table: TableType<TData>;
  paginationTexts: DataTablePaginationTexts;
  selectedRowsTexts: SelectedRowsTexts;
}) {
  return (
    <div className="flex items-center justify-between px-2">
      <div className="flex-1 text-sm text-muted-foreground">
        {table.getFilteredSelectedRowModel().rows.length} {selectedRowsTexts.of}
        {table.getFilteredRowModel().rows.length}{" "}
        {selectedRowsTexts.rowsSelected}
      </div>
      <div className="flex items-center space-x-6 lg:space-x-8">
        <div className="flex items-center space-x-2">
          <p className="text-sm font-medium">{paginationTexts.pageSize}</p>
          <Select
            value={`${table.getState().pagination.pageSize}`}
            onValueChange={(value) => {
              table.setPageSize(Number(value));
            }}
          >
            <SelectTrigger className="h-8 w-[70px]">
              <SelectValue placeholder={table.getState().pagination.pageSize} />
            </SelectTrigger>
            <SelectContent side="top">
              {[10, 20, 30, 40, 100].map((pageSize) => (
                <SelectItem key={pageSize} value={`${pageSize}`}>
                  {pageSize}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
        <div className="flex w-[100px] items-center justify-center text-sm font-medium">
          {paginationTexts.page} {table.getState().pagination.pageIndex + 1}{" "}
          {paginationTexts.of} {table.getPageCount()}
        </div>
        <div className="flex items-center space-x-2">
          <Button
            variant="outline"
            className="hidden h-8 w-8 p-0 lg:flex"
            onClick={() => table.setPageIndex(0)}
            disabled={!table.getCanPreviousPage()}
          >
            <span className="sr-only">{paginationTexts.firstPage}</span>
            <DoubleArrowLeftIcon className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            className="h-8 w-8 p-0"
            onClick={() => table.previousPage()}
            disabled={!table.getCanPreviousPage()}
          >
            <span className="sr-only">{paginationTexts.previousPage}</span>
            <ChevronLeftIcon className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            className="h-8 w-8 p-0"
            onClick={() => table.nextPage()}
            disabled={!table.getCanNextPage()}
          >
            <span className="sr-only">{paginationTexts.nextPage}</span>
            <ChevronRightIcon className="h-4 w-4" />
          </Button>
          <Button
            variant="outline"
            className="hidden h-8 w-8 p-0 lg:flex"
            onClick={() => table.setPageIndex(table.getPageCount() - 1)}
            disabled={!table.getCanNextPage()}
          >
            <span className="sr-only">{paginationTexts.lastPage}</span>
            <DoubleArrowRightIcon className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
}
const HeaderSortingButton = <T,>({
  column,
  text,
}: {
  column: Column<T>;
  text: string;
}) => (
  <Button
    variant="ghost"
    className="px-1.5 py-1 text-lg"
    size="lg"
    onClick={() => {
      const sortState = column.getIsSorted();
      if (sortState === "asc") {
        column.toggleSorting(true);
      } else if (sortState === "desc") {
        column.clearSorting();
      } else {
        column.toggleSorting();
      }
    }}
  >
    {text}
    {column.getIsSorted() === "desc" ? (
      <ArrowDown className="ml-2 h-4 w-4" />
    ) : column.getIsSorted() === "asc" ? (
      <ArrowDown className="ml-2 h-4 w-4 rotate-180" />
    ) : (
      <ArrowUpDown className="ml-2 h-4 w-4" />
    )}
  </Button>
);
