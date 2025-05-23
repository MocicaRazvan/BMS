"use client";
import { DataTable, DataTableTexts } from "@/components/table/data-table";
import { ExtraTableProps } from "@/types/tables";
import useList, { UseListProps } from "@/hoooks/useList";
import { WithUser } from "@/lib/user";
import { Link, useRouter } from "@/navigation";
import { useFormatter } from "next-intl";
import { CustomEntityModel, OrderDtoWithAddress } from "@/types/dto";
import React, { Suspense, useCallback, useMemo, useState } from "react";
import { ColumnDef } from "@tanstack/react-table";
import { ColumnActionsTexts } from "@/texts/components/table";
import { orderColumnActions } from "@/lib/constants";
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
import LoadingSpinner from "@/components/common/loading-spinner";
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

type Props = ExtraTableProps & OrderTableTexts & UseListProps & WithUser;

const fieldKeys = ["country", "city", "state"] as const;
export default function OrdersTable({
  search,
  orderTableColumnsTexts,
  dataTableTexts,
  path,
  extraQueryParams,
  extraArrayQueryParam,
  extraUpdateSearchParams,
  extraCriteria,
  forWhom,
  mainDashboard,
  sortingOptions,
  sizeOptions,
  authUser,
  searchKeyLabel,
  creationFilterTexts,
}: Props) {
  const router = useRouter();
  const isAdmin = authUser?.role === "ROLE_ADMIN";
  const formatIntl = useFormatter();
  const [searchKey, setSearchKey] =
    useState<(typeof fieldKeys)[number]>("city");

  const { navigateToNotFound } = useClientNotFound();

  const handleSearchKeyChange = useCallback((value: string) => {
    setSearchKey(value as (typeof fieldKeys)[number]);
  }, []);

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

  const searchKeyCriteria = useCallback(
    (callback: () => void) => (
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
    ),
    [handleSearchKeyChange, searchKey],
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
    setSortValue,
    items,
    updateSortState,
    isFinished,
    clearFilterValue,
    resetCurrentPage,
    updateFilterValue,
    updateFilterValueFromString,
    filterValue,
    updateCreatedAtRange,
  } = useList<CustomEntityModel<OrderDtoWithAddress>>({
    path,
    sizeOptions,
    filterKey: searchKey,
    sortingOptions,
    extraUpdateSearchParams: updateSearchKeyParams,
  });

  const data: OrderDtoWithAddress[] = useMemo(
    () => items.map(({ content }) => content),
    [items],
  );

  const radioArgs = useMemo(
    () => ({
      setSort,
      sortingOptions,
      setSortValue,
      sortValue,
      callback: resetCurrentPage,
      filterKey: searchKey,
    }),
    [
      resetCurrentPage,
      searchKey,
      setSort,
      setSortValue,
      sortValue,
      sortingOptions,
    ],
  );

  const columns: ColumnDef<OrderDtoWithAddress>[] = useMemo(
    () => [
      {
        id: orderTableColumnsTexts.id,
        accessorKey: "order.id",
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
                <DropdownMenuItem
                  className="cursor-pointer"
                  onClick={() =>
                    router.push(
                      //todo make for trainer_ also
                      forWhom === "admin"
                        ? `/admin/orders/single/${row.original.order.id}`
                        : `/orders/single/${row.original.order.id}`,
                    )
                  }
                >
                  {view}
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
      router,
      radioArgs,
      updateCreatedAtRange,
      resetCurrentPage,
    ],
  );

  const getRowId = useCallback(
    (row: OrderDtoWithAddress) => wrapItemToString(row.order.id),
    [],
  );

  if (error?.status) {
    return navigateToNotFound();
  }

  return (
    <div className="px-1 pb-10 w-full  h-full space-y-8 lg:space-y-14">
      <Suspense fallback={<LoadingSpinner />}>
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
          specialPDFColumns={[
            {
              key: "address",
              handler: (value: object) => {
                if ("country" in value && "city" in value && "state" in value) {
                  return `${value["country"]} ${value["city"]} ${value["state"]}`;
                }
                return "";
              },
            },
          ]}
          {...dataTableTexts}
          searchInputProps={{
            value: filter[searchKey] || "",
            searchInputTexts: {
              placeholder: `${search} ${searchKeyLabel[searchKey]}...`,
            },
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
            filterKey: searchKey,
          }}
          extraCriteria={
            <div className="order-[2]">
              {searchKeyCriteria(resetCurrentPage)}
            </div>
          }
          chartProps={{
            aggregatorConfig: {
              [orderTableColumnsTexts.plans + " / 10"]: (p) =>
                p.order.planIds.length / 10,
              [orderTableColumnsTexts.plans +
              " * " +
              orderTableColumnsTexts.total +
              " / 10"]: (p) => (p.order.planIds.length * p.order.total) / 100,
            },
            dateField: "order.createdAt",
          }}
          showChart={true}
          // rangeDateFilter={
          // <CreationFilter
          //   {...creationFilterTexts}
          //   updateCreatedAtRange={updateCreatedAtRange}
          //   hideUpdatedAt={true}
          // />

          // }
        />
      </Suspense>
    </div>
  );
}
