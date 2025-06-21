"use client";

import { ColumnActionsTexts } from "@/texts/components/table";
import { recipeColumnActions } from "@/types/constants";
import {
  DataTable,
  DataTableProps,
  DataTableTexts,
} from "@/components/table/data-table";
import { UseApprovedFilterTexts } from "@/components/list/useApprovedFilter";
import useFilterDropdown, {
  RadioFieldFilterCriteriaCallback,
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import { ExtraTableProps } from "@/types/tables";
import useList, { UseListProps } from "@/hoooks/useList";
import useBinaryFilter, {
  RadioBinaryCriteriaWithCallback,
} from "@/components/list/useBinaryFilter";
import { dietTypes } from "@/types/forms";
import { Link } from "@/navigation/navigation";
import {
  CustomEntityModel,
  RecipeResponse,
  ResponseWithEntityCount,
} from "@/types/dto";
import React, { useCallback, useMemo } from "react";
import { ColumnDef } from "@tanstack/react-table";
import { format, parseISO } from "date-fns";
import { Badge } from "@/components/ui/badge";
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
import AlertDialogApproveRecipes from "@/components/dialogs/recipes/approve-recipe";
import useClientNotFound from "@/hoooks/useClientNotFound";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
import AlertDialogDeleteRecipe from "@/components/dialogs/recipes/delete-recipe";
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
import { ListSearchInputProps } from "@/components/forms/input-serach";

export interface RecipeTableColumnsTexts {
  id: string;
  title: string;
  userLikes: string;
  userDislikes: string;
  createdAt: string;
  updatedAt: string;
  type: string;
  count: string;
  approved: {
    header: string;
    true: string;
    false: string;
  };
  actions: ColumnActionsTexts<typeof recipeColumnActions>;
}

export interface RecipeTableTexts {
  dataTableTexts: DataTableTexts;
  useApprovedFilterTexts: UseApprovedFilterTexts;
  dietDropdownTexts: UseFilterDropdownTexts;
  recipeTableColumnsTexts: RecipeTableColumnsTexts;
  search: string;
  creationFilterTexts: CreationFilterTexts;
}

interface Props extends ExtraTableProps, RecipeTableTexts, UseListProps {
  isSidebarOpen?: boolean;
}

export default function RecipeTable({
  forWhom,
  dataTableTexts,
  path,
  extraQueryParams,
  sizeOptions,
  sortingOptions,
  useApprovedFilterTexts,
  dietDropdownTexts,
  search,
  recipeTableColumnsTexts,
  mainDashboard = false,
  extraArrayQueryParam,
  isSidebarOpen = false,
  creationFilterTexts,
}: Props) {
  const { authUser } = useAuthUserMinRole();

  const {
    field: approvedField,
    updateFieldSearch: updateApproveField,
    setField: setApproved,
  } = useBinaryFilter({
    fieldKey: "approved",
  });

  const {
    value: dietType,
    updateFieldDropdownFilter: updateDietType,
    items: dietTypeItems,
    setField: setDietTypeField,
  } = useFilterDropdown({
    items: dietTypes.map((value) => ({
      value,
      label: dietDropdownTexts.labels[value],
    })),
    fieldKey: "type",
    noFilterLabel: dietDropdownTexts.noFilterLabel,
  });

  const { navigateToNotFound } = useClientNotFound();

  const isAdmin = authUser?.role === "ROLE_ADMIN";

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
    initialFilterValue,
  } = useList<ResponseWithEntityCount<CustomEntityModel<RecipeResponse>>>({
    path,
    extraQueryParams: {
      ...(extraQueryParams && extraQueryParams),
      ...(approvedField !== null && { approved: approvedField.toString() }),
      ...(dietType && { type: dietType }),
    },
    extraArrayQueryParam,
    extraUpdateSearchParams: (p) => {
      updateApproveField(p);
      updateDietType(p);
    },
    sizeOptions,
    sortingOptions,
    debounceDelay: 0,
  });

  const data: ResponseWithEntityCount<RecipeResponse>[] = useMemo(
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
    [resetCurrentPage, setSort, sortValue, sortingOptions],
  );

  const columns: ColumnDef<ResponseWithEntityCount<RecipeResponse>>[] = useMemo(
    () => [
      {
        id: recipeTableColumnsTexts.id,
        accessorKey: "model.id",
        enableResizing: true,
        minSize: 35,
        size: 35,
        header: () => (
          <p className="font-bold text-lg text-left">
            {recipeTableColumnsTexts.id}
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
        id: recipeTableColumnsTexts.title,
        accessorKey: "model.title",
        enableResizing: true,
        minSize: 60,
        size: 60,
        header: () => (
          <RadioSortButton sortingProperty="title" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {recipeTableColumnsTexts.title}
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
        id: recipeTableColumnsTexts.type,
        accessorKey: "model.type",
        header: () => (
          <RadioSortDropDownWithExtraDummy
            trigger={
              <p className="font-bold text-lg text-left">
                {recipeTableColumnsTexts.type}
              </p>
            }
            // extraContent={dietTypeCriteriaRadioCallback(resetCurrentPage)}
            extraContent={
              <RadioFieldFilterCriteriaCallback
                callback={resetCurrentPage}
                fieldKey="type"
                noFilterLabel={dietDropdownTexts.noFilterLabel}
                setGlobalFilter={setDietTypeField}
                items={dietTypeItems}
              />
            }
          />
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
        id: recipeTableColumnsTexts.count,
        accessorKey: "count",
        enableResizing: true,
        minSize: 35,
        size: 35,
        header: () => (
          <p className="font-bold text-lg text-left">
            {recipeTableColumnsTexts.count}
          </p>
        ),
        cell: ({ row }) => (
          <div className="max-w-16 text-nowrap overflow-x-hidden">
            <p>{row.original.count}</p>
          </div>
        ),
      },
      {
        id: recipeTableColumnsTexts.userLikes,
        accessorKey: "model.userLikes",
        enableResizing: true,
        header: () => (
          <RadioSortButton
            sortingProperty="userLikesLength"
            radioArgs={radioArgs}
          >
            <p className="font-bold text-lg text-left">
              {recipeTableColumnsTexts.userLikes}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => <p>{row.original.model.userLikes.length}</p>,
      },
      {
        id: recipeTableColumnsTexts.userDislikes,
        accessorKey: "model.userDislikes",
        enableResizing: true,
        header: () => (
          <RadioSortButton
            sortingProperty="userDislikesLength"
            radioArgs={radioArgs}
          >
            <p className="font-bold text-lg text-left">
              {recipeTableColumnsTexts.userDislikes}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => <p>{row.original.model.userDislikes.length}</p>,
      },
      {
        id: recipeTableColumnsTexts.createdAt,
        accessorKey: "model.createdAt",
        header: () => (
          <RadioSortDropDownWithExtra
            radioArgs={radioArgs}
            sortingProperty="createdAt"
            trigger={
              <p className="font-bold text-lg text-left">
                {recipeTableColumnsTexts.createdAt}
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
        id: recipeTableColumnsTexts.updatedAt,
        accessorKey: "model.updatedAt",
        header: () => (
          <RadioSortDropDownWithExtra
            radioArgs={radioArgs}
            sortingProperty="updatedAt"
            trigger={
              <p className="font-bold text-lg text-left">
                {recipeTableColumnsTexts.updatedAt}
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
        id: recipeTableColumnsTexts.approved.header,
        accessorKey: "model.approved",
        header: () => (
          <RadioSortDropDownWithExtraDummy
            trigger={
              <div className="font-bold text-lg text-left">
                {recipeTableColumnsTexts.approved.header}
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
            variant={row.original.model.approved ? "success" : "destructive"}
          >
            {
              recipeTableColumnsTexts.approved[
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
            button,
            disapprove,
            label,
            view,
            viewOwner,
            update,
            approve,
            duplicate,
            viewOwnerItems,
          } = recipeTableColumnsTexts.actions;

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
                      forWhom === "trainer"
                        ? `/trainer/recipes/single/${row.original.model.id}`
                        : `/admin/recipes/single/${row.original.model.id}`
                    }
                  >
                    {view}
                  </Link>
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
                            href={`/admin/users/${row.original.model.userId}/recipes`}
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
                          href={`/trainer/recipes/update/${row.original.model.id}`}
                        >
                          {update}
                        </Link>
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem asChild>
                        <Link
                          className="cursor-pointer"
                          href={`/trainer/recipes/duplicate/${row.original.model.id}`}
                        >
                          {duplicate}
                        </Link>
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      {row.original.count === 0 && (
                        <DropdownMenuItem
                          asChild
                          onClick={(e) => {
                            e.stopPropagation();
                          }}
                          className="mt-5 py-2"
                        >
                          <AlertDialogDeleteRecipe
                            recipe={row.original.model}
                            token={authUser.token}
                            callBack={refetch}
                            title={row.original.model.title}
                          />
                        </DropdownMenuItem>
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
                    <AlertDialogApproveRecipes
                      recipe={row.original.model}
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
      recipeTableColumnsTexts.id,
      recipeTableColumnsTexts.title,
      recipeTableColumnsTexts.type,
      recipeTableColumnsTexts.count,
      recipeTableColumnsTexts.userLikes,
      recipeTableColumnsTexts.userDislikes,
      recipeTableColumnsTexts.createdAt,
      recipeTableColumnsTexts.updatedAt,
      recipeTableColumnsTexts.approved,
      recipeTableColumnsTexts.actions,
      radioArgs,
      resetCurrentPage,
      dietDropdownTexts.noFilterLabel,
      setDietTypeField,
      dietTypeItems,
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
    ],
  );

  const finalCols = useMemo(
    () => (isSidebarOpen ? columns.slice(0, -3) : columns),
    [columns, isSidebarOpen],
  );

  const getRowId = useCallback(
    (row: ResponseWithEntityCount<RecipeResponse>) =>
      wrapItemToString(row.model.id),
    [],
  );
  const searchInputProps: ListSearchInputProps = useMemo(
    () => ({
      initialValue: initialFilterValue,
      searchInputTexts: { placeholder: search },
      onChange: updateFilterValue,
      onClear: clearFilterValue,
    }),
    [clearFilterValue, initialFilterValue, search, updateFilterValue],
  );
  const radioSortProps = useMemo(
    () => ({
      setSort,
      sort,
      sortingOptions,
      sortValue,
      callback: resetCurrentPage,
      filterKey: "title",
    }),
    [setSort, sort, sortingOptions, sortValue, resetCurrentPage],
  );
  const chartProps: DataTableProps<
    ResponseWithEntityCount<RecipeResponse>
  >["chartProps"] = useMemo(
    () => ({
      aggregatorConfig: {
        "#": (_) => 1,
        "#omnivore": (r) => (r.model.type === "OMNIVORE" ? 1 : 0),
        "#vegan": (r) => (r.model.type === "VEGAN" ? 1 : 0),
        "#vegetarian": (r) => (r.model.type === "VEGETARIAN" ? 1 : 0),
        ["#" + recipeTableColumnsTexts.approved.header]: (r) =>
          Number(r.model.approved),
        [recipeTableColumnsTexts.count + " / 10"]: (r) => r.count / 10,
      },
      dateField: "model.createdAt",
    }),
    [recipeTableColumnsTexts.approved.header, recipeTableColumnsTexts.count],
  );

  if (error?.status) {
    return navigateToNotFound();
  }

  return (
    <div className="px-1 w-full space-y-8 lg:space-y-14 ">
      <DataTable
        sizeOptions={sizeOptions}
        fileName="recipes"
        isFinished={isFinished}
        columns={finalCols}
        data={data || []}
        pageInfo={pageInfo}
        setPageInfo={setPageInfo}
        getRowId={getRowId}
        {...dataTableTexts}
        useRadioSort={false}
        searchInputProps={searchInputProps}
        radioSortProps={radioSortProps}
        chartProps={chartProps}
        showChart={true}
        // rangeDateFilter={
        //   <CreationFilter
        //     {...creationFilterTexts}
        //     updateCreatedAtRange={updateCreatedAtRange}
        //     updateUpdatedAtRange={updateUpdatedAtRange}
        //   />
        // }
      />
    </div>
  );
}
