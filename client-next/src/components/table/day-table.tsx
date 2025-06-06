"use client";

import { ColumnActionsTexts } from "@/texts/components/table";
import { dayColumnActions, getColorsByDayType } from "@/lib/constants";
import { DataTableTexts } from "@/components/table/data-table";
import useFilterDropdown, {
  RadioFieldFilterCriteriaCallback,
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import { ExtraTableProps } from "@/types/tables";
import useList, { UseListProps } from "@/hoooks/useList";
import { Link, useRouter } from "@/navigation";
import useClientNotFound from "@/hoooks/useClientNotFound";
import {
  CustomEntityModel,
  DayResponse,
  DayType,
  dayTypes,
  ResponseWithEntityCount,
} from "@/types/dto";
import React, { useCallback, useMemo } from "react";
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
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
import { DayTypeBadgeTexts } from "@/components/days/day-type-badge";
import CreationFilter, {
  CreationFilterTexts,
} from "@/components/list/creation-filter";
import { wrapItemToString } from "@/lib/utils";
import {
  RadioSortButton,
  RadioSortDropDownWithExtra,
  RadioSortDropDownWithExtraDummy,
} from "@/components/common/radio-sort";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import dynamic from "next/dynamic";
import DataTableDynamicSkeleton from "@/components/table/data-table-dynamic-skeleton";

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
  creationFilterTexts: CreationFilterTexts;
}

export interface DayTableProps
  extends ExtraTableProps,
    UseListProps,
    DayTableTexts {
  isSidebarOpen?: boolean;
}

const DynamicDataTable = dynamic(
  () =>
    import("@/components/table/data-table").then(
      (mod) => mod.DataTable<ResponseWithEntityCount<DayResponse>>,
    ),
  {
    ssr: false,
    loading: () => <DataTableDynamicSkeleton />,
  },
);

const typeColors = getColorsByDayType();

const getDayTypeChart = (type: DayType) => ({
  [`#${type.replace("_", " ").toLowerCase()}`]: (
    d: ResponseWithEntityCount<DayResponse>,
  ) => (d.model.type === type ? 1 : 0),
});

const dayChartTypes = dayTypes.reduce(
  (acc, type) => ({ ...acc, ...getDayTypeChart(type) }),
  {},
);

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
  path,
  sortingOptions,
  sizeOptions,
  mainDashboard,
  dayTypeBadgeTexts,
  creationFilterTexts,
}: DayTableProps) {
  const { authUser } = useAuthUserMinRole();

  const router = useRouter();
  const { navigateToNotFound } = useClientNotFound();

  const {
    value: dayType,
    updateFieldDropdownFilter: updateDayType,
    items: dayTypeItems,
    setField: setDayType,
  } = useFilterDropdown({
    items: dayTypes.map((value) => ({
      value,
      label: typeDropdownTexts.labels[value],
    })),
    fieldKey: "type",
    noFilterLabel: typeDropdownTexts.noFilterLabel,
  });

  const {
    pageInfo,
    filter,
    sort,
    setSort,
    sortValue,
    items,
    isFinished,
    error,
    setPageInfo,
    refetch,
    updateFilterValue,
    clearFilterValue,
    resetCurrentPage,
    updateCreatedAtRange,
    updateUpdatedAtRange,
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

  const radioArgs = useMemo(
    () => ({
      setSort,
      sortingOptions,
      sortValue,
      callback: resetCurrentPage,
      filterKey: "title",
    }),
    [resetCurrentPage, setSort, sortValue, sortingOptions],
  );

  const columns: ColumnDef<ResponseWithEntityCount<DayResponse>>[] = useMemo(
    () => [
      {
        id: dayTableColumnTexts.id,
        accessorKey: "model.id",
        enableResizing: true,
        minSize: 35,
        size: 35,
        header: () => (
          <p className="font-bold text-lg text-left">
            {dayTableColumnTexts.id}
          </p>
        ),
        cell: ({
          row: {
            original: {
              model: { id },
            },
          },
        }) => (
          <OverflowTextTooltip
            text={wrapItemToString(id)}
            triggerClassName="w-10 max-w-10"
          />
        ),
      },
      {
        id: dayTableColumnTexts.title,
        accessorKey: "model.title",
        enableResizing: true,
        minSize: 100,
        size: 100,
        header: () => (
          <RadioSortButton sortingProperty="title" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {dayTableColumnTexts.title}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row, cell }) => (
          <OverflowTextTooltip
            text={row.original.model.title}
            triggerStyle={{
              maxWidth: `calc(var(--col-${cell.column.id}-size) * 1px - 10px)`,
            }}
          />
        ),
      },
      {
        id: dayTableColumnTexts.type,
        accessorKey: "model.type",
        header: () => (
          <RadioSortDropDownWithExtraDummy
            trigger={
              <p className="font-bold text-lg text-left">
                {dayTableColumnTexts.type}
              </p>
            }
            extraContent={
              <RadioFieldFilterCriteriaCallback
                callback={resetCurrentPage}
                fieldKey="type"
                noFilterLabel={typeDropdownTexts.noFilterLabel}
                setGlobalFilter={setDayType}
                items={dayTypeItems}
                className="capitalize"
              />
            }
          />
        ),
        cell: ({ row }) => (
          <div className="max-w-32 text-sm font-bold text-nowrap overflow-x-hidden">
            <p
              className="capitalize"
              style={{
                color: typeColors[row.original.model.type],
              }}
            >
              {dayTypeBadgeTexts.labels[row.original.model.type]}
            </p>
          </div>
        ),
      },
      {
        id: dayTableColumnTexts.count,
        accessorKey: "count",
        enableResizing: true,
        minSize: 35,
        size: 35,
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
        enableResizing: true,
        header: () => (
          <RadioSortButton
            sortingProperty="userLikesLength"
            radioArgs={radioArgs}
          >
            <p className="font-bold text-lg text-left">
              {dayTableColumnTexts.userLikes}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => <p>{row.original.model.userLikes.length}</p>,
      },
      {
        id: dayTableColumnTexts.userDislikes,
        accessorKey: "model.userDislikes",
        enableResizing: true,
        header: () => (
          <RadioSortButton
            sortingProperty="userDislikesLength"
            radioArgs={radioArgs}
          >
            <p className="font-bold text-lg text-left">
              {dayTableColumnTexts.userDislikes}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => <p>{row.original.model.userDislikes.length}</p>,
      },
      {
        id: dayTableColumnTexts.createdAt,
        accessorKey: "model.createdAt",
        header: () => (
          <RadioSortDropDownWithExtra
            radioArgs={radioArgs}
            sortingProperty="createdAt"
            trigger={
              <p className="font-bold text-lg text-left">
                {dayTableColumnTexts.createdAt}
              </p>
            }
            showNone={false}
            extraContent={
              <CreationFilter
                triggerVariant="ghost"
                triggerClassName="px-5"
                {...creationFilterTexts}
                updateCreatedAtRange={updateCreatedAtRange}
                hideUpdatedAt={true}
                showLabels={false}
              />
            }
          />
        ),
        cell: ({ row }) => (
          <p>{format(parseISO(row.original.model.createdAt), "dd/MM/yyyy")}</p>
        ),
      },
      {
        id: dayTableColumnTexts.updatedAt,
        accessorKey: "model.updatedAt",
        header: () => (
          <RadioSortDropDownWithExtra
            radioArgs={radioArgs}
            sortingProperty="updatedAt"
            trigger={
              <p className="font-bold text-lg text-left">
                {dayTableColumnTexts.updatedAt}
              </p>
            }
            extraContent={
              <CreationFilter
                triggerVariant="ghost"
                triggerClassName="px-5"
                {...creationFilterTexts}
                updateUpdatedAtRange={updateUpdatedAtRange}
                hideCreatedAt={true}
                showLabels={false}
              />
            }
          />
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
                {forWhom === "trainer" &&
                  parseInt(authUser.id) === row.original.model.userId && (
                    <>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem asChild>
                        <Link
                          className="cursor-pointer"
                          href={`/trainer/days/update/${row.original.model.id}`}
                        >
                          {update}
                        </Link>
                      </DropdownMenuItem>
                      {row.original.count === 0 && (
                        <>
                          <DropdownMenuSeparator />
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
      dayTableColumnTexts.id,
      dayTableColumnTexts.title,
      dayTableColumnTexts.type,
      dayTableColumnTexts.count,
      dayTableColumnTexts.userLikes,
      dayTableColumnTexts.userDislikes,
      dayTableColumnTexts.createdAt,
      dayTableColumnTexts.updatedAt,
      dayTableColumnTexts.actions,
      radioArgs,
      resetCurrentPage,
      typeDropdownTexts.noFilterLabel,
      setDayType,
      dayTypeItems,
      dayTypeBadgeTexts.labels,
      creationFilterTexts,
      updateCreatedAtRange,
      updateUpdatedAtRange,
      forWhom,
      authUser.id,
      authUser.token,
      refetch,
      router,
    ],
  );

  const finalCols = useMemo(
    () => (!isSidebarOpen ? columns : columns.slice(0, -2)),
    [columns, isSidebarOpen],
  );

  const getRowId = useCallback(
    (row: ResponseWithEntityCount<DayResponse>) =>
      wrapItemToString(row.model.id),
    [],
  );

  if (error?.status) {
    return navigateToNotFound();
  }

  return (
    <div className="px-1 pb-10 w-full  h-full space-y-8 lg:space-y-14">
      <DynamicDataTable
        sizeOptions={sizeOptions}
        fileName={"days"}
        isFinished={isFinished}
        columns={finalCols}
        data={data || []}
        pageInfo={pageInfo}
        setPageInfo={setPageInfo}
        getRowId={getRowId}
        {...dataTableTexts}
        searchInputProps={{
          value: filter.title || "",
          searchInputTexts: { placeholder: search },
          onChange: updateFilterValue,
          onClear: clearFilterValue,
        }}
        useRadioSort={false}
        radioSortProps={{
          setSort,
          sort,
          sortingOptions,
          sortValue,
          callback: resetCurrentPage,
          filterKey: "title",
        }}
        chartProps={{
          aggregatorConfig: {
            "#": (_) => 1,
            ...dayChartTypes,
          },
          dateField: "model.createdAt",
        }}
        showChart={true}
      />
    </div>
  );
}
