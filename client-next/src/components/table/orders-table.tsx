"use client";
import {
  DataTable,
  DataTableProps,
  DataTableTexts,
} from "@/components/table/data-table";
import { ExtraTableProps } from "@/types/tables";
import useList, { UseListProps } from "@/hoooks/useList";
import { Link } from "@/navigation/navigation";
import { useFormatter } from "next-intl";
import { CustomEntityModel, OrderDtoWithAddress } from "@/types/dto";
import React, {
  Dispatch,
  memo,
  SetStateAction,
  useCallback,
  useMemo,
  useState,
} from "react";
import { ColumnDef } from "@tanstack/react-table";
import { ColumnActionsTexts } from "@/texts/components/table";
import { orderColumnActions } from "@/types/constants";
import { format, parseISO } from "date-fns";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Button } from "@/components/ui/button";
import { MoreHorizontal } from "lucide-react";
import useClientNotFound from "@/hoooks/useClientNotFound";
import CreationFilter, {
  CreationFilterTexts,
} from "@/components/list/creation-filter";
import { wrapItemToString } from "@/lib/utils";
import {
  RadioSortButton,
  RadioSortDropDownWithExtra,
} from "@/components/common/radio-sort";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import { ListSearchInputProps } from "@/components/forms/input-serach";

export interface OrderTableColumnsTexts {
  id: string;
  date: string;
  total: string;
  plans: string;
  address: string;
  actions: ColumnActionsTexts<typeof orderColumnActions>;
}

export interface OrderTableTexts {
  dataTableTexts: DataTableTexts;
  orderTableColumnsTexts: OrderTableColumnsTexts;
  search: string;
  searchKeyLabel: Record<(typeof fieldKeys)[number], string>;
  creationFilterTexts: CreationFilterTexts;
}

export type OrdersTableProps = ExtraTableProps & OrderTableTexts & UseListProps;

const fieldKeys = ["country", "city", "state"] as const;

const specialPDFColumns = [
  {
    key: "address",
    handler: (value: object) => {
      if ("country" in value && "city" in value && "state" in value) {
        return `${value["country"]} ${value["city"]} ${value["state"]}`;
      }
      return "";
    },
  },
];

export default function OrdersTable({
  search,
  orderTableColumnsTexts,
  dataTableTexts,
  path,
  extraQueryParams,
  extraArrayQueryParam,
  extraUpdateSearchParams,
  forWhom,
  mainDashboard,
  sortingOptions,
  sizeOptions,
  searchKeyLabel,
  creationFilterTexts,
}: OrdersTableProps) {
  const { authUser } = useAuthUserMinRole();

  const isAdmin = authUser?.role === "ROLE_ADMIN";
  const formatIntl = useFormatter();
  const [searchKey, setSearchKey] =
    useState<(typeof fieldKeys)[number]>("city");

  const { navigateToNotFound } = useClientNotFound();

  const updateSearchKeyParams = useCallback(
    (searchParams: URLSearchParams) => {
      fieldKeys.forEach((key) => {
        if (key !== searchKey) {
          searchParams.delete(key);
        }
      });
    },
    [searchKey],
  );

  const {
    messages,
    error,
    setPageInfo,
    refetch,
    pageInfo,
    filter,
    setFilter,
    debouncedFilter,
    sort,
    setSort,
    sortValue,
    items,
    updateSortState,
    isFinished,
    clearFilterValue,
    resetCurrentPage,
    updateFilterValue,
    updateFilterValueFromString,
    filterValue,
    updateCreatedAtRange,
    initialFilterValue,
  } = useList<CustomEntityModel<OrderDtoWithAddress>>({
    path,
    sizeOptions,
    filterKey: searchKey,
    sortingOptions,
    extraUpdateSearchParams: updateSearchKeyParams,
    debounceDelay: 0,
  });

  const data: OrderDtoWithAddress[] = useMemo(
    () => items.map(({ content }) => content),
    [items],
  );

  const radioArgs = useMemo(
    () => ({
      setSort,
      sortingOptions,
      sortValue,
      callback: resetCurrentPage,
      filterKey: searchKey,
    }),
    [resetCurrentPage, searchKey, setSort, sortValue, sortingOptions],
  );

  const columns: ColumnDef<OrderDtoWithAddress>[] = useMemo(
    () => [
      {
        id: orderTableColumnsTexts.id,
        accessorKey: "order.id",
        enableResizing: true,
        minSize: 35,
        size: 35,
        header: () => (
          <p className="font-bold text-lg text-left">
            {orderTableColumnsTexts.id}
          </p>
        ),
        cell: ({
          row: {
            original: {
              order: { id },
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
        id: orderTableColumnsTexts.date,
        accessorKey: "order.createdAt",
        size: 215,
        header: () => (
          <RadioSortDropDownWithExtra
            radioArgs={radioArgs}
            sortingProperty="createdAt"
            trigger={
              <p className="font-bold text-lg text-left">
                {orderTableColumnsTexts.date}
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
          <p>
            {format(
              parseISO(row.original.order.createdAt),
              "dd/MM/yyyy HH:mm:ss",
            )}
          </p>
        ),
      },
      {
        id: orderTableColumnsTexts.total,
        accessorKey: "order.total",
        enableResizing: true,
        minSize: 200,
        size: 200,
        header: () => (
          <RadioSortButton sortingProperty="total" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {orderTableColumnsTexts.total}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => (
          <div className="max-w-40 text-nowrap overflow-x-hidden">
            <p>
              {formatIntl.number(row.original.order.total, {
                style: "currency",
                currency: "EUR",
                maximumFractionDigits: 2,
              })}
            </p>
          </div>
        ),
      },
      {
        id: orderTableColumnsTexts.plans,
        accessorKey: "order.planIds",
        enableResizing: false,
        minSize: 20,
        size: 20,
        header: () => (
          <p className="font-bold text-lg text-left">
            {orderTableColumnsTexts.plans}
          </p>
        ),
        cell: ({ row }) => (
          <p className="max-w-16">{row.original.order.planIds.length}</p>
        ),
      },
      {
        id: orderTableColumnsTexts.address,
        accessorKey: "address",
        enableResizing: true,
        minSize: 280,
        size: 400,
        header: () => (
          <p className="font-bold text-lg text-left">
            {orderTableColumnsTexts.address}
          </p>
        ),
        cell: ({ row }) => (
          <p>{`${row.original.address.country} ${row.original.address.city} ${row.original.address.state}`}</p>
        ),
      },
      {
        id: "actions",
        cell: ({ row }) => {
          const { label, button, view, viewOwner, viewOwnerItems } =
            orderTableColumnsTexts.actions;
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
                <DropdownMenuItem className="cursor-pointer" asChild>
                  <Link
                    href={
                      forWhom === "admin"
                        ? `/admin/orders/single/${row.original.order.id}`
                        : `/orders/single/${row.original.order.id}`
                    }
                  >
                    {view}
                  </Link>
                </DropdownMenuItem>
                {isAdmin && forWhom === "admin" && (
                  <>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem asChild>
                      <Link
                        href={
                          mainDashboard
                            ? `/admin/users/${row.original.order.userId}`
                            : `/users/single/${row.original.order.userId}`
                        }
                        className="cursor-pointer"
                      >
                        {viewOwner}
                      </Link>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem asChild>
                      <Link
                        href={`/admin/users/${row.original.order.userId}/orders`}
                        className="cursor-pointer"
                      >
                        {viewOwnerItems}
                      </Link>
                    </DropdownMenuItem>
                  </>
                )}
              </DropdownMenuContent>
            </DropdownMenu>
          );
        },
      },
    ],
    [
      forWhom,
      formatIntl,
      isAdmin,
      orderTableColumnsTexts.actions,
      orderTableColumnsTexts.address,
      orderTableColumnsTexts.date,
      orderTableColumnsTexts.id,
      orderTableColumnsTexts.plans,
      orderTableColumnsTexts.total,
      radioArgs,
      updateCreatedAtRange,
      resetCurrentPage,
    ],
  );

  const getRowId = useCallback(
    (row: OrderDtoWithAddress) => wrapItemToString(row.order.id),
    [],
  );

  const StaticExtraCriteria = useCallback(
    () => (
      <div className="order-[2]">
        <OrderExtraCriteria
          setSearchKey={setSearchKey}
          search={search}
          searchKey={searchKey}
          callback={resetCurrentPage}
          searchKeyLabel={searchKeyLabel}
        />
      </div>
    ),
    [search, searchKey, resetCurrentPage, searchKeyLabel],
  );

  const searchInputProps: ListSearchInputProps = useMemo(
    () => ({
      initialValue: initialFilterValue,
      searchInputTexts: {
        placeholder: `${search} ${searchKeyLabel[searchKey]}...`,
      },
      onChange: updateFilterValue,
      onClear: clearFilterValue,
    }),
    [
      clearFilterValue,
      initialFilterValue,
      search,
      searchKey,
      searchKeyLabel,
      updateFilterValue,
    ],
  );

  const radioSortProps = useMemo(
    () => ({
      setSort,
      sort,
      sortingOptions,
      sortValue,
      callback: resetCurrentPage,
      filterKey: searchKey,
    }),
    [resetCurrentPage, searchKey, setSort, sort, sortValue, sortingOptions],
  );
  const chartProps: DataTableProps<OrderDtoWithAddress>["chartProps"] = useMemo(
    () => ({
      aggregatorConfig: {
        [orderTableColumnsTexts.plans + " / 10"]: (p) =>
          p.order.planIds.length / 10,
        [orderTableColumnsTexts.plans +
        " * " +
        orderTableColumnsTexts.total +
        " / 10"]: (p) => (p.order.planIds.length * p.order.total) / 100,
      },
      dateField: "order.createdAt",
    }),
    [orderTableColumnsTexts.plans, orderTableColumnsTexts.total],
  );
  if (error?.status) {
    return navigateToNotFound();
  }
  return (
    <div className="px-1 pb-10 w-full  h-full space-y-8 lg:space-y-14">
      <DataTable
        sizeOptions={sizeOptions}
        fileName={`orders`}
        isFinished={isFinished}
        columns={columns}
        data={data || []}
        pageInfo={pageInfo}
        setPageInfo={setPageInfo}
        getRowId={getRowId}
        useRadioSort={false}
        specialPDFColumns={specialPDFColumns}
        {...dataTableTexts}
        searchInputProps={searchInputProps}
        radioSortProps={radioSortProps}
        ExtraCriteria={StaticExtraCriteria}
        chartProps={chartProps}
        showChart={true}
      />
    </div>
  );
}

const OrderExtraCriteria = memo(
  ({
    setSearchKey,
    search,
    searchKey,
    callback,
    searchKeyLabel,
  }: Pick<OrderTableTexts, "search" | "searchKeyLabel"> & {
    searchKey: (typeof fieldKeys)[number];
    setSearchKey: Dispatch<SetStateAction<(typeof fieldKeys)[number]>>;
    callback: () => void;
  }) => {
    const handleSearchKeyChange = useCallback(
      (value: string) => {
        setSearchKey(value as (typeof fieldKeys)[number]);
      },
      [setSearchKey],
    );

    return (
      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant={"outline"} className="capitalize">
            {`${search} ${searchKeyLabel[searchKey]}`}
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent>
          <DropdownMenuRadioGroup
            value={searchKey}
            onValueChange={(e) => {
              handleSearchKeyChange(e);
              callback();
            }}
          >
            {fieldKeys.map((key) => (
              <DropdownMenuRadioItem
                value={key}
                key={key}
                className="capitalize"
              >
                {`${search} ${searchKeyLabel[key]}`}
              </DropdownMenuRadioItem>
            ))}
          </DropdownMenuRadioGroup>
        </DropdownMenuContent>
      </DropdownMenu>
    );
  },
);

OrderExtraCriteria.displayName = "OrderExtraCriteria";
