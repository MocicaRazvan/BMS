"use client";

import { ColumnActionsTexts } from "@/texts/components/table";
import { dayColumnActions } from "@/lib/constants";
import { DataTable, DataTableTexts } from "@/components/table/data-table";
import useFilterDropdown, {
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import { ExtraTableProps } from "@/types/tables";
import { WithUser } from "@/lib/user";
import useList, { UseListProps } from "@/hoooks/useList";
import { Link, useRouter } from "@/navigation";
import { useFormatter } from "next-intl";
import useClientNotFound from "@/hoooks/useClientNotFound";
import {
  CustomEntityModel,
  DayResponse,
  dayTypes,
  ResponseWithEntityCount,
} from "@/types/dto";
import { Suspense, useMemo } from "react";
import { ColumnDef } from "@tanstack/react-table";
import { format, parseISO } from "date-fns";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { MoreHorizontal } from "lucide-react";
import AlertDialogDeleteDay from "@/components/dialogs/days/delete-day";
import LoadingSpinner from "@/components/common/loading-spinner";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
import { DayTypeBadgeTexts } from "@/components/days/day-type-badge";

export interface DayTableColumnsTexts {
  id: string;
  title: string;
  count: string;
  userLikes: string;
  userDislikes: string;
  createdAt: string;
  updatedAt: string;
  type: string;
  actions: ColumnActionsTexts<typeof dayColumnActions>;
}

export interface DayTableTexts {
  dataTableTexts: DataTableTexts;
  dayTableColumnTexts: DayTableColumnsTexts;
  typeDropdownTexts: UseFilterDropdownTexts;
  search: string;
  dayTypeBadgeTexts: DayTypeBadgeTexts;
}

export interface DayTableProps
  extends ExtraTableProps,
    WithUser,
    UseListProps,
    DayTableTexts {
  isSidebarOpen?: boolean;
}
export default function DaysTable({
  dayTableColumnTexts,
  dataTableTexts,
  typeDropdownTexts,
  isSidebarOpen,
  search,
  extraUpdateSearchParams,
  extraCriteria,
  forWhom,
  extraQueryParams,
  extraArrayQueryParam,
  authUser,
  path,
  sortingOptions,
  sizeOptions,
  mainDashboard,
  dayTypeBadgeTexts,
}: DayTableProps) {
  const router = useRouter();
  const formatIntl = useFormatter();
  const { navigateToNotFound } = useClientNotFound();

  const {
    value: dayType,
    updateFieldDropdownFilter: updateDayType,
    filedFilterCriteriaCallback: dayTypeFilterCriteriaCallback,
  } = useFilterDropdown({
    items: dayTypes.map((value) => ({
      value,
      label: typeDropdownTexts.labels[value],
    })),
    fieldKey: "type",
    noFilterLabel: typeDropdownTexts.noFilterLabel,
  });

  const {
    messages,
    pageInfo,
    filter,
    sort,
    setSort,
    sortValue,
    setSortValue,
    items,
    isFinished,
    error,
    setPageInfo,
    refetch,
    updateFilterValue,
    clearFilterValue,
    resetCurrentPage,
  } = useList<ResponseWithEntityCount<CustomEntityModel<DayResponse>>>({
    path,
    extraQueryParams: {
      ...(extraQueryParams && extraQueryParams),
      ...(dayType && { type: dayType }),
    },
    extraArrayQueryParam,
    extraUpdateSearchParams: (p) => {
      extraUpdateSearchParams?.(p);
      updateDayType(p);
    },
    sizeOptions,
    sortingOptions,
    filterKey: "title",
  });

  const data: ResponseWithEntityCount<DayResponse>[] = useMemo(
    () => items.map((i) => ({ model: i.model.content, count: i.count })),
    [items],
  );

  const columns: ColumnDef<ResponseWithEntityCount<DayResponse>>[] = useMemo(
    () => [
      {
        id: dayTableColumnTexts.id,
        accessorKey: "model.id",
        header: () => (
          <p className="font-bold text-lg text-left">
            {dayTableColumnTexts.id}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.id}</p>,
      },
      {
        id: dayTableColumnTexts.title,
        accessorKey: "model.title",
        header: () => (
          <p className="font-bold text-lg text-left">
            {dayTableColumnTexts.title}
          </p>
        ),
        cell: ({ row }) => (
          <OverflowTextTooltip text={row.original.model.title} />
        ),
      },
      {
        id: dayTableColumnTexts.type,
        accessorKey: "model.type",
        header: () => (
          <p className="font-bold text-lg text-left">
            {dayTableColumnTexts.type}
          </p>
        ),
        cell: ({ row }) => (
          <div className="max-w-32 text-sm font-bold text-nowrap overflow-x-hidden">
            <p>{dayTypeBadgeTexts.labels[row.original.model.type]}</p>
          </div>
        ),
      },
      {
        id: dayTableColumnTexts.count,
        accessorKey: "count",
        header: () => (
          <p className="font-bold text-lg text-left">
            {dayTableColumnTexts.count}
          </p>
        ),
        cell: ({ row }) => (
          <div className="max-w-16 text-nowrap overflow-x-hidden">
            <p>{row.original.count}</p>
          </div>
        ),
      },
      {
        id: dayTableColumnTexts.userLikes,
        accessorKey: "model.userLikes",
        header: () => (
          <p className="font-bold text-lg text-left">
            {dayTableColumnTexts.userLikes}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.userLikes.length}</p>,
      },
      {
        id: dayTableColumnTexts.userDislikes,
        accessorKey: "model.userDislikes",
        header: () => (
          <p className="font-bold text-lg text-left">
            {dayTableColumnTexts.userDislikes}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.userDislikes.length}</p>,
      },
      {
        id: dayTableColumnTexts.createdAt,
        accessorKey: "model.createdAt",
        header: () => (
          <p className="font-bold text-lg text-left">
            {dayTableColumnTexts.createdAt}
          </p>
        ),
        cell: ({ row }) => (
          <p>{format(parseISO(row.original.model.createdAt), "dd/MM/yyyy")}</p>
        ),
      },
      {
        id: dayTableColumnTexts.updatedAt,
        accessorKey: "model.updatedAt",
        header: () => (
          <p className="font-bold text-lg text-left">
            {dayTableColumnTexts.updatedAt}
          </p>
        ),
        cell: ({ row }) => (
          <p>{format(parseISO(row.original.model.updatedAt), "dd/MM/yyyy")}</p>
        ),
      },
      {
        id: "actions",
        cell: ({ row }) => {
          const { update, label, view, button, duplicate } =
            dayTableColumnTexts.actions;
          return (
            <DropdownMenu modal>
              <DropdownMenuTrigger asChild>
                <Button
                  variant="ghost"
                  className="h-8 w-8 p-0 hover:bg-background"
                >
                  <span className="sr-only">{button}</span>
                  <MoreHorizontal className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuLabel className="mb-3">{label}</DropdownMenuLabel>
                <DropdownMenuItem
                  className="cursor-pointer"
                  onClick={() =>
                    router.push(`/trainer/days/single/${row.original.model.id}`)
                  }
                >
                  {view}
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                {forWhom === "trainer" &&
                  parseInt(authUser.id) === row.original.model.userId && (
                    <>
                      <DropdownMenuItem asChild>
                        <Link
                          className="cursor-pointer"
                          href={`/trainer/days/update/${row.original.model.id}`}
                        >
                          {update}
                        </Link>
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      {row.original.count === 0 && (
                        <>
                          <DropdownMenuItem
                            asChild
                            onClick={(e) => {
                              e.stopPropagation();
                            }}
                            className="mt-5 py-2"
                          >
                            <AlertDialogDeleteDay
                              day={row.original.model}
                              token={authUser.token}
                              callBack={refetch}
                            />
                          </DropdownMenuItem>
                          <DropdownMenuSeparator />
                        </>
                      )}
                    </>
                  )}
              </DropdownMenuContent>
            </DropdownMenu>
          );
        },
      },
    ],
    [
      authUser.id,
      authUser.token,
      dayTableColumnTexts.actions,
      dayTableColumnTexts.count,
      dayTableColumnTexts.createdAt,
      dayTableColumnTexts.id,
      dayTableColumnTexts.title,
      dayTableColumnTexts.type,
      dayTableColumnTexts.updatedAt,
      dayTableColumnTexts.userDislikes,
      dayTableColumnTexts.userLikes,
      forWhom,
      refetch,
      router,
    ],
  );

  const finalCols = useMemo(
    () => (!isSidebarOpen ? columns : columns.slice(0, -2)),
    [columns, isSidebarOpen],
  );

  if (error?.status) {
    return navigateToNotFound();
  }
  return (
    <div className="px-1 pb-10 w-full  h-full space-y-8 lg:space-y-14">
      <Suspense fallback={<LoadingSpinner />}>
        <DataTable
          sizeOptions={sizeOptions}
          fileName={"days"}
          isFinished={isFinished}
          columns={finalCols}
          data={data || []}
          pageInfo={pageInfo}
          setPageInfo={setPageInfo}
          {...dataTableTexts}
          searchInputProps={{
            value: filter.title || "",
            searchInputTexts: { placeholder: search },
            onChange: updateFilterValue,
            onClear: clearFilterValue,
          }}
          radioSortProps={{
            setSort,
            sort,
            sortingOptions,
            setSortValue,
            sortValue,
            callback: resetCurrentPage,
          }}
          extraCriteria={
            <div className="flex items-start justify-center gap-8 flex-1 flex-wrap">
              <div className="flex items-center justify-end gap-4 flex-1 flex-wrap">
                {dayTypeFilterCriteriaCallback(resetCurrentPage)}
              </div>
            </div>
          }
        />
      </Suspense>
    </div>
  );
}
