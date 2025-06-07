"use client";

import { ColumnActionsTexts } from "@/texts/components/table";
import { planColumnActions } from "@/types/constants";
import { DataTableTexts } from "@/components/table/data-table";
import { UseApprovedFilterTexts } from "@/components/list/useApprovedFilter";
import useBinaryFilter, {
  RadioBinaryCriteriaWithCallback,
  UseBinaryTexts,
} from "@/components/list/useBinaryFilter";
import useFilterDropdown, {
  RadioFieldFilterCriteriaCallback,
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import { ExtraTableProps } from "@/types/tables";
import useList, { UseListProps } from "@/hoooks/useList";
import { Link, useRouter } from "@/navigation";
import { dietTypes } from "@/types/forms";
import {
  CustomEntityModel,
  planObjectives,
  PlanResponse,
  ResponseWithEntityCount,
} from "@/types/dto";
import React, { useCallback, useMemo } from "react";
import { ColumnDef } from "@tanstack/react-table";
import { Badge, BadgeVariants } from "@/components/ui/badge";
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
import { useFormatter } from "next-intl";
import useClientNotFound from "@/hoooks/useClientNotFound";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
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
  creationFilterTexts: CreationFilterTexts;
}

export interface PlanTableProps
  extends ExtraTableProps,
    UseListProps,
    PlanTableTexts {
  isSidebarOpen?: boolean;
}
const colorMap = {
  CARNIVORE: "destructive",
  VEGAN: "success",
  OMNIVORE: "default",
  VEGETARIAN: "accent",
} as const;

const DynamicDataTable = dynamic(
  () =>
    import("@/components/table/data-table").then(
      (mod) => mod.DataTable<ResponseWithEntityCount<PlanResponse>>,
    ),
  {
    ssr: false,
    loading: () => <DataTableDynamicSkeleton />,
  },
);
export default function PlansTable({
  planTableColumnsTexts,
  dataTableTexts,
  displayFilterTexts,
  dietDropdownTexts,
  search,
  isSidebarOpen,
  useApprovedFilterTexts,
  mainDashboard,
  sortingOptions,
  sizeOptions,
  path,
  extraQueryParams,
  extraArrayQueryParam,
  extraUpdateSearchParams,
  extraCriteria,
  objectiveDropDownTexts,
  creationFilterTexts,
  forWhom,
}: PlanTableProps) {
  const { authUser } = useAuthUserMinRole();

  const router = useRouter();
  const isAdmin = authUser?.role === "ROLE_ADMIN";
  const formatIntl = useFormatter();
  const { navigateToNotFound } = useClientNotFound();
  const {
    field: displayField,
    updateFieldSearch: updateDisplay,
    setField: setDisplay,
  } = useBinaryFilter({
    fieldKey: "display",
  });
  const {
    field: approvedField,
    updateFieldSearch: updateApproved,
    setField: setApproved,
  } = useBinaryFilter({
    fieldKey: "approved",
  });
  const {
    value: dietType,
    updateFieldDropdownFilter: updateDietType,
    items: dietTypeItems,
    setField: setDietType,
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
    items: objectiveTypeItems,
    setField: setObjectiveType,
  } = useFilterDropdown({
    items: planObjectives.map((value) => ({
      value,
      label: objectiveDropDownTexts.labels[value],
    })),
    fieldKey: "objective",
    noFilterLabel: objectiveDropDownTexts.noFilterLabel,
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
    updateUpdatedAtRange,
    updateCreatedAtRange,
  } = useList<ResponseWithEntityCount<CustomEntityModel<PlanResponse>>>({
    path,
    extraQueryParams: {
      ...(extraQueryParams && extraQueryParams),
      ...(dietType && { type: dietType }),
      ...(approvedField !== null && { approved: approvedField.toString() }),
      ...(displayField !== null && { display: displayField.toString() }),
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

  const data: ResponseWithEntityCount<PlanResponse>[] = useMemo(
    () =>
      items.map((i) => ({
        model: i.model.content,
        count: i.count,
      })),
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
    [setSort, sortingOptions, sortValue, resetCurrentPage],
  );

  const columns: ColumnDef<ResponseWithEntityCount<PlanResponse>>[] = useMemo(
    () => [
      {
        id: planTableColumnsTexts.id,
        accessorKey: "model.id",
        enableResizing: true,
        minSize: 35,
        size: 35,
        header: () => (
          <p className="font-bold text-lg text-left">
            {planTableColumnsTexts.id}
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
            triggerClassName="w-9 max-w-9"
          />
        ),
      },
      {
        id: planTableColumnsTexts.title,
        accessorKey: "model.title",
        enableResizing: true,
        minSize: 60,
        size: 60,
        header: () => (
          <RadioSortButton sortingProperty="title" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {planTableColumnsTexts.title}
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
        id: planTableColumnsTexts.type,
        accessorKey: "model.type",
        size: 30,
        header: () => (
          <RadioSortDropDownWithExtraDummy
            trigger={
              <p className="font-bold text-lg text-left">
                {planTableColumnsTexts.type}
              </p>
            }
            extraContent={
              <RadioFieldFilterCriteriaCallback
                callback={resetCurrentPage}
                fieldKey="type"
                noFilterLabel={dietDropdownTexts.noFilterLabel}
                setGlobalFilter={setDietType}
                items={dietTypeItems}
              />
            }
          />
        ),
        cell: ({ row }) => (
          <Badge
            className="px-1.5"
            variant={colorMap[row.original.model.type] as BadgeVariants}
          >
            {row.original.model.type}
          </Badge>
        ),
      },
      {
        id: planTableColumnsTexts.objective,
        accessorKey: "model.objective",
        size: 30,
        header: () => (
          <RadioSortDropDownWithExtraDummy
            trigger={
              <p className="font-bold text-lg text-left">
                {planTableColumnsTexts.objective}
              </p>
            }
            extraContent={
              <RadioFieldFilterCriteriaCallback
                callback={resetCurrentPage}
                fieldKey="objective"
                noFilterLabel={objectiveDropDownTexts.noFilterLabel}
                setGlobalFilter={setObjectiveType}
                items={objectiveTypeItems}
              />
            }
          />
        ),
        cell: ({ row }) => (
          <div className="max-w-30 text-xs font-bold text-nowrap overflow-x-hidden">
            <p>{row.original.model.objective.replace("_", " ")}</p>
          </div>
        ),
      },
      {
        id: planTableColumnsTexts.display.header,
        accessorKey: "model.display",
        size: 30,
        header: () => (
          <RadioSortDropDownWithExtraDummy
            trigger={
              <p className="font-bold text-lg text-left">
                {planTableColumnsTexts.display.header}
              </p>
            }
            extraContent={
              <RadioBinaryCriteriaWithCallback
                callback={resetCurrentPage}
                fieldKey="display"
                texts={displayFilterTexts}
                setGlobalFilter={setDisplay}
              />
            }
          />
        ),
        cell: ({ row }) => (
          <Badge
            className="px-2"
            variant={row.original.model.display ? "success" : "destructive"}
          >
            {row.original.model.display
              ? planTableColumnsTexts.display.true
              : planTableColumnsTexts.display.false}
          </Badge>
        ),
      },
      {
        id: planTableColumnsTexts.createdAt,
        accessorKey: "model.createdAt",
        header: () => (
          <RadioSortDropDownWithExtra
            radioArgs={radioArgs}
            sortingProperty="createdAt"
            trigger={
              <p className="font-bold text-lg text-left">
                {planTableColumnsTexts.createdAt}
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
        id: planTableColumnsTexts.count,
        accessorKey: "count",
        enableResizing: true,
        minSize: 35,
        size: 35,
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
        enableResizing: true,
        minSize: 35,
        size: 35,
        header: () => (
          <RadioSortButton sortingProperty="price" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {planTableColumnsTexts.price}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => (
          <div className="max-w-[4.5rem] text-nowrap overflow-x-hidden">
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
      //todo vezi cum le aduagi daca e ca e overflow
      {
        id: planTableColumnsTexts.userLikes,
        accessorKey: "model.userLikes",
        enableResizing: true,
        header: () => (
          <RadioSortButton
            sortingProperty="userLikesLength"
            radioArgs={radioArgs}
          >
            <p className="font-bold text-lg text-left">
              {planTableColumnsTexts.userLikes}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => <p>{row.original.model.userLikes.length}</p>,
      },
      {
        id: planTableColumnsTexts.userDislikes,
        accessorKey: "model.userDislikes",
        enableResizing: true,
        header: () => (
          <RadioSortButton
            sortingProperty="userDislikesLength"
            radioArgs={radioArgs}
          >
            <p className="font-bold text-lg text-left">
              {planTableColumnsTexts.userDislikes}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => <p>{row.original.model.userDislikes.length}</p>,
      },

      {
        id: planTableColumnsTexts.updatedAt,
        accessorKey: "model.updatedAt",
        header: () => (
          <RadioSortDropDownWithExtra
            radioArgs={radioArgs}
            sortingProperty="updatedAt"
            trigger={
              <p className="font-bold text-lg text-left">
                {planTableColumnsTexts.updatedAt}
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
        id: planTableColumnsTexts.approved.header,
        accessorKey: "model.approved",
        header: () => (
          <RadioSortDropDownWithExtraDummy
            trigger={
              <div className="font-bold text-lg text-left">
                {planTableColumnsTexts.approved.header}
              </div>
            }
            extraContent={
              <RadioBinaryCriteriaWithCallback
                callback={resetCurrentPage}
                fieldKey="approved"
                texts={{
                  trueText: useApprovedFilterTexts.approved,
                  falseText: useApprovedFilterTexts.notApproved,
                  all: useApprovedFilterTexts.all,
                }}
                setGlobalFilter={setApproved}
              />
            }
          />
        ),
        cell: ({ row }) => (
          <Badge
            className="px-2"
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
            duplicate,
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
                      <DropdownMenuItem asChild>
                        <Link
                          className="cursor-pointer"
                          href={`/trainer/plans/duplicate/${row.original.model.id}`}
                        >
                          {duplicate}
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
      planTableColumnsTexts.id,
      planTableColumnsTexts.title,
      planTableColumnsTexts.type,
      planTableColumnsTexts.objective,
      planTableColumnsTexts.display.header,
      planTableColumnsTexts.display.true,
      planTableColumnsTexts.display.false,
      planTableColumnsTexts.count,
      planTableColumnsTexts.price,
      planTableColumnsTexts.createdAt,
      planTableColumnsTexts.updatedAt,
      planTableColumnsTexts.approved,
      planTableColumnsTexts.actions,
      radioArgs,
      resetCurrentPage,
      dietDropdownTexts.noFilterLabel,
      setDietType,
      dietTypeItems,
      objectiveDropDownTexts.noFilterLabel,
      setObjectiveType,
      objectiveTypeItems,
      displayFilterTexts,
      setDisplay,
      formatIntl,
      creationFilterTexts,
      updateCreatedAtRange,
      updateUpdatedAtRange,
      useApprovedFilterTexts.approved,
      useApprovedFilterTexts.notApproved,
      useApprovedFilterTexts.all,
      setApproved,
      forWhom,
      mainDashboard,
      authUser,
      refetch,
      isAdmin,
      router,
    ],
  );
  const finalCols = useMemo(
    () => (!isSidebarOpen ? columns : columns.slice(0, -4)),
    [isSidebarOpen, columns],
  );

  const getRowId = useCallback(
    (row: ResponseWithEntityCount<PlanResponse>) =>
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
        fileName={`plans`}
        isFinished={isFinished}
        columns={finalCols}
        data={data || []}
        pageInfo={pageInfo}
        setPageInfo={setPageInfo}
        {...dataTableTexts}
        getRowId={getRowId}
        useRadioSort={false}
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
          sortValue,
          callback: resetCurrentPage,
          filterKey: "title",
        }}
        extraCriteria={
          <div className="flex items-start justify-center gap-8 flex-1 flex-wrap">
            <div className="flex items-center justify-end gap-4 flex-1 flex-wrap">
              {/*{dietTypeCriteriaCallback(resetCurrentPage)}*/}
              {/*{objectiveTypeCriteriaCallback(resetCurrentPage)}*/}
              {/*{displayCriteriaCallBack(resetCurrentPage)}*/}
              {/*{approvedCriteriaCallBack(resetCurrentPage)}*/}
            </div>
          </div>
        }
        chartProps={{
          aggregatorConfig: {
            "#": (_) => 1,
            "#omnivore": (r) => (r.model.type === "OMNIVORE" ? 1 : 0),
            "#vegan": (r) => (r.model.type === "VEGAN" ? 1 : 0),
            "#vegetarian": (r) => (r.model.type === "VEGETARIAN" ? 1 : 0),
            ["#" + planTableColumnsTexts.approved.header]: (p) =>
              Number(p.model.approved),
            [planTableColumnsTexts.count + " / 100"]: (p) => p.count / 100,
            [planTableColumnsTexts.count +
            " * " +
            planTableColumnsTexts.price +
            " / 1000"]: (p) => (p.count * p.model.price) / 1000,
          },
          dateField: "model.createdAt",
        }}
        showChart={true}
      />
    </div>
  );
}
