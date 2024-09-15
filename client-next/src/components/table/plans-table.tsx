"use client";

import { ColumnActionsTexts } from "@/texts/components/table";
import { planColumnActions } from "@/lib/constants";
import { DataTable, DataTableTexts } from "@/components/table/data-table";
import { UseApprovedFilterTexts } from "@/components/list/useApprovedFilter";
import useBinaryFilter, {
  UseBinaryTexts,
} from "@/components/list/useBinaryFilter";
import useFilterDropdown, {
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import { ExtraTableProps } from "@/types/tables";
import { WithUser } from "@/lib/user";
import useList, { UseListProps } from "@/hoooks/useList";
import { Link, useRouter } from "@/navigation";
import { dietTypes } from "@/types/forms";
import {
  CustomEntityModel,
  planObjectives,
  PlanResponse,
  ResponseWithEntityCount,
} from "@/types/dto";
import { Suspense, useMemo } from "react";
import { ColumnDef } from "@tanstack/react-table";
import { Badge } from "@/components/ui/badge";
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
import AlertDialogApprovePlan from "@/components/dialogs/plans/approve-plan";
import ToggleDisplayPlan from "@/components/dialogs/plans/plan-toggle-display";
import AlertDialogDeletePlan from "@/components/dialogs/plans/delete-plan";
import LoadingSpinner from "@/components/common/loading-spinner";
import { useFormatter } from "next-intl";
import useClientNotFound from "@/hoooks/useClientNotFound";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";

export interface PlanTableColumnsTexts {
  id: string;
  title: string;
  type: string;
  count: string;
  userLikes: string;
  userDislikes: string;
  createdAt: string;
  updatedAt: string;
  price: string;
  objective: string;
  approved: {
    header: string;
    true: string;
    false: string;
  };
  display: {
    header: string;
    true: string;
    false: string;
  };
  actions: ColumnActionsTexts<typeof planColumnActions>;
}

export interface PlanTableTexts {
  dataTableTexts: DataTableTexts;
  useApprovedFilterTexts: UseApprovedFilterTexts;
  displayFilterTexts: UseBinaryTexts;
  dietDropdownTexts: UseFilterDropdownTexts;
  objectiveDropDownTexts: UseFilterDropdownTexts;
  planTableColumnsTexts: PlanTableColumnsTexts;
  search: string;
}

export interface PlanTableProps
  extends ExtraTableProps,
    WithUser,
    UseListProps,
    PlanTableTexts {
  isSidebarOpen?: boolean;
}

export default function PlansTable({
  planTableColumnsTexts,
  dataTableTexts,
  displayFilterTexts,
  dietDropdownTexts,
  search,
  isSidebarOpen,
  useApprovedFilterTexts,
  authUser,
  mainDashboard,
  sortingOptions,
  sizeOptions,
  path,
  extraQueryParams,
  extraArrayQueryParam,
  extraUpdateSearchParams,
  extraCriteria,
  objectiveDropDownTexts,
  forWhom,
}: PlanTableProps) {
  const router = useRouter();
  const isAdmin = authUser?.role === "ROLE_ADMIN";
  const formatIntl = useFormatter();
  const { navigateToNotFound } = useClientNotFound();
  const {
    field: displayField,
    updateFieldSearch: updateDisplay,
    fieldCriteriaCallBack: displayCriteriaCallBack,
  } = useBinaryFilter({
    fieldKey: "display",
    ...displayFilterTexts,
  });
  const {
    field: approvedField,
    updateFieldSearch: updateApproved,
    fieldCriteriaCallBack: approvedCriteriaCallBack,
  } = useBinaryFilter({
    fieldKey: "approved",
    trueText: useApprovedFilterTexts.approved,
    falseText: useApprovedFilterTexts.notApproved,
    all: useApprovedFilterTexts.all,
  });
  const {
    value: dietType,
    fieldDropdownFilterQueryParam: dietTypeQP,
    updateFieldDropdownFilter: updateDietType,
    filedFilterCriteriaCallback: dietTypeCriteriaCallback,
  } = useFilterDropdown({
    items: dietTypes.map((value) => ({
      value,
      label: dietDropdownTexts.labels[value],
    })),
    fieldKey: "type",
    noFilterLabel: dietDropdownTexts.noFilterLabel,
  });
  const {
    value: objectiveType,
    updateFieldDropdownFilter: updateObjectiveType,
    filedFilterCriteriaCallback: objectiveTypeCriteriaCallback,
  } = useFilterDropdown({
    items: planObjectives.map((value) => ({
      value,
      label: objectiveDropDownTexts.labels[value],
    })),
    fieldKey: "objective",
    noFilterLabel: objectiveDropDownTexts.noFilterLabel,
  });

  const {
    messages,
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
    error,
    setPageInfo,
    refetch,
    updateFilterValue,
    clearFilterValue,
    resetCurrentPage,
  } = useList<ResponseWithEntityCount<CustomEntityModel<PlanResponse>>>({
    path,
    extraQueryParams: {
      ...(extraQueryParams && extraQueryParams),
      ...(dietType && { type: dietType }),
      ...(approvedField && { approved: approvedField.toString() }),
      ...(displayField && { display: displayField.toString() }),
      ...(objectiveType && { objective: objectiveType }),
    },
    extraArrayQueryParam,
    extraUpdateSearchParams: (p) => {
      extraUpdateSearchParams?.(p);
      updateDisplay(p);
      updateApproved(p);
      updateDietType(p);
      updateObjectiveType(p);
    },
    sizeOptions,
    sortingOptions,
    filterKey: "title",
  });

  console.log("messages", messages);

  const data: ResponseWithEntityCount<PlanResponse>[] = useMemo(
    () =>
      items.map((i) => ({
        model: i.model.content,
        count: i.count,
      })),
    [items],
  );
  const columns: ColumnDef<ResponseWithEntityCount<PlanResponse>>[] = useMemo(
    () => [
      {
        id: planTableColumnsTexts.id,
        accessorKey: "model.id",
        header: () => (
          <p className="font-bold text-lg text-left">
            {planTableColumnsTexts.id}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.id}</p>,
      },
      {
        id: planTableColumnsTexts.title,
        accessorKey: "model.title",
        header: () => (
          <p className="font-bold text-lg text-left">
            {planTableColumnsTexts.title}
          </p>
        ),
        cell: ({ row }) => (
          <OverflowTextTooltip text={row.original.model.title} />
        ),
      },
      {
        id: planTableColumnsTexts.type,
        accessorKey: "model.type",
        header: () => (
          <p className="font-bold text-lg text-left">
            {planTableColumnsTexts.type}
          </p>
        ),
        cell: ({ row }) => {
          const colorMap = {
            CARNIVORE: "destructive",
            VEGAN: "success",
            OMNIVORE: "default",
            VEGETARIAN: "accent",
          };

          return (
            <Badge
              variant={
                colorMap[row.original.model.type] as
                  | "default"
                  | "destructive"
                  | "success"
                  | "accent"
                  | "secondary"
                  | "outline"
                  | null
                  | undefined
              }
            >
              {row.original.model.type}
            </Badge>
          );
        },
      },
      {
        id: planTableColumnsTexts.objective,
        accessorKey: "model.objective",
        header: () => (
          <p className="font-bold text-lg text-left">
            {planTableColumnsTexts.objective}
          </p>
        ),
        cell: ({ row }) => (
          <div className="max-w-28 text-sm font-bold text-nowrap overflow-x-hidden">
            <p>{row.original.model.objective}</p>
          </div>
        ),
      },
      {
        id: planTableColumnsTexts.display.header,
        accessorKey: "model.display",
        header: () => (
          <p className="font-bold text-lg text-left">
            {planTableColumnsTexts.display.header}
          </p>
        ),
        cell: ({ row }) => (
          <Badge
            variant={row.original.model.display ? "success" : "destructive"}
          >
            {row.original.model.display
              ? planTableColumnsTexts.display.true
              : planTableColumnsTexts.display.false}
          </Badge>
        ),
      },
      {
        id: planTableColumnsTexts.count,
        accessorKey: "count",
        header: () => (
          <p className="font-bold text-lg text-left">
            {planTableColumnsTexts.count}
          </p>
        ),
        cell: ({ row }) => (
          <div className="max-w-16 text-nowrap overflow-x-hidden">
            <p>{row.original.count}</p>
          </div>
        ),
      },
      {
        id: planTableColumnsTexts.price,
        accessorKey: "model.price",
        header: () => (
          <p className="font-bold text-lg text-left">
            {planTableColumnsTexts.price}
          </p>
        ),
        cell: ({ row }) => (
          <div className="max-w-16 text-nowrap overflow-x-hidden">
            <p>
              {formatIntl.number(row.original.model.price, {
                style: "currency",
                currency: "EUR",
                maximumFractionDigits: 2,
              })}
            </p>
          </div>
        ),
      },
      {
        id: planTableColumnsTexts.userLikes,
        accessorKey: "model.userLikes",
        header: () => (
          <p className="font-bold text-lg text-left">
            {planTableColumnsTexts.userLikes}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.userLikes.length}</p>,
      },
      {
        id: planTableColumnsTexts.userDislikes,
        accessorKey: "model.userDislikes",
        header: () => (
          <p className="font-bold text-lg text-left">
            {planTableColumnsTexts.userDislikes}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.userDislikes.length}</p>,
      },
      {
        id: planTableColumnsTexts.createdAt,
        accessorKey: "model.createdAt",
        header: () => (
          <p className="font-bold text-lg text-left">
            {planTableColumnsTexts.createdAt}
          </p>
        ),
        cell: ({ row }) => (
          <p>{format(parseISO(row.original.model.createdAt), "dd/MM/yyyy")}</p>
        ),
      },
      {
        id: planTableColumnsTexts.updatedAt,
        accessorKey: "model.updatedAt",
        header: () => (
          <p className="font-bold text-lg text-left">
            {planTableColumnsTexts.updatedAt}
          </p>
        ),
        cell: ({ row }) => (
          <p>{format(parseISO(row.original.model.updatedAt), "dd/MM/yyyy")}</p>
        ),
      },
      {
        id: planTableColumnsTexts.approved.header,
        accessorKey: "model.approved",
        header: () => (
          <div className="font-bold text-lg text-left">
            {planTableColumnsTexts.approved.header}
          </div>
        ),
        cell: ({ row }) => (
          <Badge
            variant={row.original.model.approved ? "success" : "destructive"}
          >
            {
              planTableColumnsTexts.approved[
                row.original.model.approved.toString() as "true" | "false"
              ]
            }
          </Badge>
        ),
      },
      {
        id: "actions",
        cell: ({ row }) => {
          const {
            display,
            label,
            view,
            hide,
            viewOwner,
            viewOwnerItems,
            update,
            approve,
            disapprove,
            button,
          } = planTableColumnsTexts.actions;
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
                      forWhom === "trainer"
                        ? `/trainer/plans/single/${row.original.model.id}`
                        : `/admin/plans/single/${row.original.model.id}`,
                    )
                  }
                >
                  {view}
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                {!(forWhom === "trainer") && (
                  <>
                    <DropdownMenuItem asChild>
                      <Link
                        href={
                          mainDashboard
                            ? `/admin/users/${row.original.model.userId}`
                            : `/users/single/${row.original.model.userId}`
                        }
                        className="cursor-pointer"
                      >
                        {viewOwner}
                      </Link>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    {mainDashboard && (
                      <>
                        <DropdownMenuItem asChild>
                          <Link
                            href={`/admin/users/${row.original.model.userId}/plans`}
                            className="cursor-pointer"
                          >
                            {viewOwnerItems}
                          </Link>
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                      </>
                    )}
                  </>
                )}

                {forWhom === "trainer" &&
                  parseInt(authUser.id) === row.original.model.userId && (
                    <>
                      <DropdownMenuItem asChild>
                        <Link
                          className="cursor-pointer"
                          href={`/trainer/plans/update/${row.original.model.id}`}
                        >
                          {update}
                        </Link>
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem
                        asChild
                        onClick={(e) => {
                          e.stopPropagation();
                        }}
                        className="mt-5 py-2"
                      >
                        <ToggleDisplayPlan
                          model={row.original.model}
                          authUser={authUser}
                          callBack={refetch}
                        />
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      {row.original.count === 0 && (
                        <>
                          {" "}
                          <DropdownMenuItem
                            asChild
                            onClick={(e) => {
                              e.stopPropagation();
                            }}
                            className="mt-5 py-2"
                          >
                            <AlertDialogDeletePlan
                              plan={row.original.model}
                              token={authUser.token}
                              title={row.original.model.title}
                              callBack={refetch}
                            />
                          </DropdownMenuItem>
                          <DropdownMenuSeparator />
                        </>
                      )}
                    </>
                  )}

                <div className="h-1" />
                {isAdmin && (
                  <DropdownMenuItem
                    asChild
                    onClick={(e) => {
                      e.stopPropagation();
                    }}
                    className="mt-5 py-2"
                  >
                    <AlertDialogApprovePlan
                      plan={row.original.model}
                      authUser={authUser}
                      callBack={refetch}
                    />
                  </DropdownMenuItem>
                )}
              </DropdownMenuContent>
            </DropdownMenu>
          );
        },
      },
    ],
    [
      authUser,
      forWhom,
      formatIntl,
      isAdmin,
      mainDashboard,
      planTableColumnsTexts.actions,
      planTableColumnsTexts.approved,
      planTableColumnsTexts.count,
      planTableColumnsTexts.createdAt,
      planTableColumnsTexts.display.false,
      planTableColumnsTexts.display.header,
      planTableColumnsTexts.display.true,
      planTableColumnsTexts.id,
      planTableColumnsTexts.objective,
      planTableColumnsTexts.price,
      planTableColumnsTexts.title,
      planTableColumnsTexts.type,
      planTableColumnsTexts.updatedAt,
      planTableColumnsTexts.userDislikes,
      planTableColumnsTexts.userLikes,
      refetch,
      router,
    ],
  );
  const finalCols = useMemo(
    () => (!isSidebarOpen ? columns : columns.slice(0, -5)),
    [isSidebarOpen, columns],
  );

  if (error?.status) {
    return navigateToNotFound();
  }

  return (
    <div className="px-1 pb-10 w-full  h-full space-y-8 lg:space-y-14">
      {planTableColumnsTexts.objective}
      <Suspense fallback={<LoadingSpinner />}>
        <DataTable
          sizeOptions={sizeOptions}
          fileName={`plans`}
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
                {dietTypeCriteriaCallback(resetCurrentPage)}
                {objectiveTypeCriteriaCallback(resetCurrentPage)}
                {displayCriteriaCallBack(resetCurrentPage)}
                {approvedCriteriaCallBack(resetCurrentPage)}
              </div>
            </div>
          }
        />
      </Suspense>
    </div>
  );
}
