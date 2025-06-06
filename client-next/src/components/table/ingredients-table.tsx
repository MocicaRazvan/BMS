"use client";

import { ExtraTableProps } from "@/types/tables";
import useList, { UseListProps } from "@/hoooks/useList";
import useBinaryFilter, {
  RadioBinaryCriteriaWithCallback,
  UseBinaryTexts,
} from "@/components/list/useBinaryFilter";
import useFilterDropdown, {
  RadioFieldFilterCriteriaCallback,
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import { dietTypes } from "@/types/forms";
import {
  CustomEntityModel,
  IngredientNutritionalFactResponse,
  ResponseWithEntityCount,
} from "@/types/dto";
import React, { useCallback, useMemo } from "react";
import { ColumnDef } from "@tanstack/react-table";
import { DataTableTexts } from "@/components/table/data-table";
import { getCalories } from "@/types/responses";
import { Badge } from "@/components/ui/badge";
import { ingredientColumnActions } from "@/lib/constants";
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
import { useRouter } from "@/navigation";
import { ColumnActionsTexts } from "@/texts/components/table";
import ToggleDisplayIngredient from "@/components/dialogs/ingredients/ingredient-toggle-display";
import AlertDialogDeleteIngredient from "@/components/dialogs/ingredients/delete-ingredient";
import useClientNotFound from "@/hoooks/useClientNotFound";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
import { CreationFilterTexts } from "@/components/list/creation-filter";
import { wrapItemToString } from "@/lib/utils";
import {
  RadioSortButton,
  RadioSortDropDownWithExtraDummy,
} from "@/components/common/radio-sort";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import dynamic from "next/dynamic";
import DataTableDynamicSkeleton from "@/components/table/data-table-dynamic-skeleton";

export interface IngredientTableColumnTexts {
  id: string;
  title: string;
  display: {
    header: string;
    true: string;
    false: string;
  };
  fat: string;
  saturatedFat: string;
  carbohydrates: string;
  sugar: string;
  protein: string;
  salt: string;
  calories: string;
  unit: string;
  createdAt: string;
  name: string;
  type: string;
  count: string;
  actions: ColumnActionsTexts<typeof ingredientColumnActions>;
}

export interface IngredientTableTexts {
  dataTableTexts: DataTableTexts;
  ingredientTableColumnTexts: IngredientTableColumnTexts;
  displayFilterTexts: UseBinaryTexts;
  dietDropdownTexts: UseFilterDropdownTexts;
  search: string;
  creationFilterTexts: CreationFilterTexts;
}

export type IngredientTableProps = ExtraTableProps &
  UseListProps &
  IngredientTableTexts & {
    isSidebarOpen?: boolean;
  };

const typeColorMap = {
  CARNIVORE: "destructive",
  VEGAN: "success",
  OMNIVORE: "default",
  VEGETARIAN: "accent",
};
const DynamicDataTable = dynamic(
  () =>
    import("@/components/table/data-table").then(
      (mod) =>
        mod.DataTable<
          ResponseWithEntityCount<IngredientNutritionalFactResponse>
        >,
    ),
  {
    ssr: false,
    loading: () => <DataTableDynamicSkeleton />,
  },
);
export default function IngredientsTable({
  forWhom,
  mainDashboard,
  sortingOptions,
  sizeOptions,
  path,
  extraQueryParams,
  extraArrayQueryParam,
  extraUpdateSearchParams,
  extraCriteria,
  dataTableTexts,
  ingredientTableColumnTexts,
  isSidebarOpen = false,
  displayFilterTexts,
  dietDropdownTexts,
  search,
  creationFilterTexts,
}: IngredientTableProps) {
  const { authUser } = useAuthUserMinRole();

  const router = useRouter();
  const isAdmin = authUser?.role === "ROLE_ADMIN";
  const {
    field,
    updateFieldSearch,
    setField: setDisplay,
  } = useBinaryFilter({
    fieldKey: "display",
  });

  const { navigateToNotFound } = useClientNotFound();

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
  } = useList<
    ResponseWithEntityCount<
      CustomEntityModel<IngredientNutritionalFactResponse>
    >
  >({
    path: "/ingredients/filteredWithCount",
    extraQueryParams: {
      ...(extraQueryParams && extraQueryParams),
      ...(isAdmin && forWhom === "admin"
        ? field !== null && { display: field.toString() }
        : { display: "true" }),
      ...(dietType && { type: dietType }),
    },
    extraArrayQueryParam,
    extraUpdateSearchParams: (p) => {
      extraUpdateSearchParams?.(p);
      updateFieldSearch(p);
      updateDietType(p);
    },
    sizeOptions,
    sortingOptions,
    filterKey: "name",
  });

  const data: ResponseWithEntityCount<IngredientNutritionalFactResponse>[] =
    useMemo(
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
      filterKey: "name",
    }),
    [resetCurrentPage, setSort, sortValue, sortingOptions],
  );

  const columns: ColumnDef<
    ResponseWithEntityCount<IngredientNutritionalFactResponse>
  >[] = useMemo(
    () => [
      {
        id: ingredientTableColumnTexts.id,
        accessorKey: "model.ingredient.id",
        enableResizing: true,
        minSize: 20,
        size: 20,
        header: () => (
          <p className="font-bold text-lg text-left">
            {ingredientTableColumnTexts.id}
          </p>
        ),
        cell: ({
          row: {
            original: {
              model: {
                ingredient: { id },
              },
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
        id: ingredientTableColumnTexts.name,
        accessorKey: "model.ingredient.name",
        enableResizing: true,
        minSize: 60,
        size: 60,
        header: () => (
          <RadioSortButton sortingProperty="name" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {ingredientTableColumnTexts.name}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row, cell }) => (
          <OverflowTextTooltip
            text={row.original.model.ingredient.name}
            triggerStyle={{
              maxWidth: `calc(var(--col-${cell.column.id}-size) * 1px - 10px)`,
            }}
          />
        ),
      },
      {
        id: ingredientTableColumnTexts.type,
        accessorKey: "model.ingredient.type",
        size: 30,
        header: () => (
          <RadioSortDropDownWithExtraDummy
            trigger={
              <p className="font-bold text-lg text-left">
                {ingredientTableColumnTexts.type}
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
        cell: ({ row }) => {
          return (
            <Badge
              className="px-2"
              variant={
                typeColorMap[row.original.model.ingredient.type] as
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
              {row.original.model.ingredient.type}
            </Badge>
          );
        },
      },
      {
        id: ingredientTableColumnTexts.display.header,
        accessorKey: "model.ingredient.display",
        size: 30,
        header: () => (
          <RadioSortDropDownWithExtraDummy
            trigger={
              <p className="font-bold text-lg text-left">
                {ingredientTableColumnTexts.display.header}
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
            variant={
              row.original.model.ingredient.display ? "success" : "destructive"
            }
          >
            {row.original.model.ingredient.display
              ? ingredientTableColumnTexts.display.true
              : ingredientTableColumnTexts.display.false}
          </Badge>
        ),
      },
      {
        id: ingredientTableColumnTexts.count,
        accessorKey: "count",
        enableResizing: true,
        minSize: 30,
        size: 30,
        header: () => (
          <p className="font-bold text-lg text-left">
            {ingredientTableColumnTexts.count}
          </p>
        ),
        cell: ({ row }) => (
          <div className="max-w-16 text-nowrap overflow-x-hidden">
            <p>{row.original.count}</p>
          </div>
        ),
      },
      {
        id: ingredientTableColumnTexts.fat,
        accessorKey: "model.nutritionalFact.fat",
        enableResizing: true,
        minSize: 30,
        size: 30,
        header: () => (
          <RadioSortButton sortingProperty="fat" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {ingredientTableColumnTexts.fat}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => <p>{row.original.model.nutritionalFact.fat}</p>,
      },
      {
        id: ingredientTableColumnTexts.saturatedFat,
        accessorKey: "model.nutritionalFact.saturatedFat",
        enableResizing: true,
        minSize: 30,
        size: 30,
        header: () => (
          <RadioSortButton sortingProperty="saturatedFat" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {ingredientTableColumnTexts.saturatedFat}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => (
          <p>{row.original.model.nutritionalFact.saturatedFat}</p>
        ),
      },
      {
        id: ingredientTableColumnTexts.carbohydrates,
        accessorKey: "model.nutritionalFact.carbohydrates",
        enableResizing: true,
        minSize: 30,
        size: 30,
        header: () => (
          <RadioSortButton
            sortingProperty="carbohydrates"
            className="max-w-[90px] text-nowrap overflow-x-hidden"
            radioArgs={radioArgs}
          >
            <div className="max-w-16 text-nowrap overflow-x-hidden">
              <p className="font-bold text-lg text-left">
                {ingredientTableColumnTexts.carbohydrates}
              </p>
            </div>
          </RadioSortButton>
        ),
        cell: ({ row }) => (
          <p>{row.original.model.nutritionalFact.carbohydrates}</p>
        ),
      },

      {
        id: ingredientTableColumnTexts.sugar,
        accessorKey: "model.nutritionalFact.sugar",
        enableResizing: true,
        minSize: 30,
        size: 30,
        header: () => (
          <RadioSortButton sortingProperty="sugar" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {ingredientTableColumnTexts.sugar}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => <p>{row.original.model.nutritionalFact.sugar}</p>,
      },
      {
        id: ingredientTableColumnTexts.protein,
        accessorKey: "model.nutritionalFact.protein",
        enableResizing: true,
        minSize: 30,
        size: 30,
        header: () => (
          <RadioSortButton sortingProperty="protein" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {ingredientTableColumnTexts.protein}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => <p>{row.original.model.nutritionalFact.protein}</p>,
      },

      {
        id: ingredientTableColumnTexts.salt,
        accessorKey: "model.nutritionalFact.salt",
        enableResizing: true,
        minSize: 30,
        size: 30,
        header: () => (
          <RadioSortButton sortingProperty="salt" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {ingredientTableColumnTexts.salt}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => <p>{row.original.model.nutritionalFact.salt}</p>,
      },
      {
        id: ingredientTableColumnTexts.calories,
        enableResizing: true,
        minSize: 30,
        size: 30,
        header: () => (
          <RadioSortButton sortingProperty="calories" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {ingredientTableColumnTexts.calories}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row }) => <p>{getCalories(row.original.model)}</p>,
      },
      // {
      //   id: ingredientTableColumnTexts.unit,
      //   accessorKey: "model.nutritionalFact.unit",
      //   header: () => (
      //     <p className="font-bold text-lg text-left">
      //       {ingredientTableColumnTexts.unit}
      //     </p>
      //   ),
      //   cell: ({ row }) => (
      //     <Badge
      //       variant={
      //         row.original.model.nutritionalFact.unit === "GRAM"
      //           ? "secondary"
      //           : "default"
      //       }
      //     >
      //       {row.original.model.nutritionalFact.unit}
      //     </Badge>
      //   ),
      // },

      forWhom === "admin"
        ? {
            id: "actions",
            cell: ({ row }) => {
              const ing = row.original.model;
              const {
                deleteIngredient,
                updateIngredient,
                viewIngredient,
                label,
                toggleDisplay,
                duplicate,
                button,
              } = ingredientTableColumnTexts.actions;
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
                    <DropdownMenuLabel className="mb-3">
                      {label}
                    </DropdownMenuLabel>
                    <DropdownMenuItem
                      className="cursor-pointer"
                      onClick={() =>
                        router.push(
                          `/admin/ingredients/single/${ing.ingredient.id}`,
                        )
                      }
                    >
                      {viewIngredient}
                    </DropdownMenuItem>
                    {isAdmin && (
                      <>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                          className="cursor-pointer"
                          onClick={() =>
                            router.push(
                              `/admin/ingredients/update/${ing.ingredient.id}`,
                            )
                          }
                        >
                          {updateIngredient}
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                          className="cursor-pointer"
                          onClick={() =>
                            router.push(
                              `/admin/ingredients/duplicate/${ing.ingredient.id}`,
                            )
                          }
                        >
                          {duplicate}
                        </DropdownMenuItem>
                        <DropdownMenuSeparator />
                        <DropdownMenuItem
                          className="cursor-pointer"
                          asChild
                          onClick={(e) => e.stopPropagation()}
                        >
                          <ToggleDisplayIngredient
                            model={ing}
                            callBack={refetch}
                            authUser={authUser}
                          />
                        </DropdownMenuItem>
                        {row.original.count === 0 && (
                          <>
                            <DropdownMenuSeparator />
                            <DropdownMenuItem
                              className="cursor-pointer"
                              asChild
                              onClick={(e) => e.stopPropagation()}
                            >
                              <AlertDialogDeleteIngredient
                                token={authUser.token}
                                callBack={refetch}
                                ingredientNutritionalFactResponse={ing}
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
          }
        : {
            id: "actions",
            cell: ({ row }) => (
              <Button
                variant="default"
                size={"sm"}
                onClick={() =>
                  router.push(
                    `/trainer/ingredients/single/${row.original.model.ingredient.id}`,
                  )
                }
              >
                {ingredientTableColumnTexts.actions.viewIngredient}
              </Button>
            ),
          },
    ],
    [
      ingredientTableColumnTexts.id,
      ingredientTableColumnTexts.name,
      ingredientTableColumnTexts.type,
      ingredientTableColumnTexts.display.header,
      ingredientTableColumnTexts.display.true,
      ingredientTableColumnTexts.display.false,
      ingredientTableColumnTexts.count,
      ingredientTableColumnTexts.fat,
      ingredientTableColumnTexts.saturatedFat,
      ingredientTableColumnTexts.carbohydrates,
      ingredientTableColumnTexts.sugar,
      ingredientTableColumnTexts.protein,
      ingredientTableColumnTexts.salt,
      ingredientTableColumnTexts.calories,
      ingredientTableColumnTexts.actions,
      forWhom,
      radioArgs,
      resetCurrentPage,
      dietDropdownTexts.noFilterLabel,
      setDietType,
      dietTypeItems,
      displayFilterTexts,
      setDisplay,
      isAdmin,
      refetch,
      authUser,
      router,
    ],
  );

  const finalColumns = useMemo(() => {
    let cols = columns;
    if (forWhom !== "admin") {
      cols = cols.filter(
        (col) =>
          col.id !== ingredientTableColumnTexts.display.header &&
          col.id !== ingredientTableColumnTexts.count,
      );
    }
    return !isSidebarOpen ? cols : cols.slice(0, -4);
  }, [
    columns,
    forWhom,
    ingredientTableColumnTexts.count,
    ingredientTableColumnTexts.display.header,
    isSidebarOpen,
  ]);

  const getRowId = useCallback(
    (row: ResponseWithEntityCount<IngredientNutritionalFactResponse>) =>
      wrapItemToString(row.model.ingredient.id),
    [],
  );

  if (error?.status) {
    return navigateToNotFound();
  }

  return (
    <div className="px-1 w-full space-y-8 lg:space-y-14 ">
      <DynamicDataTable
        sizeOptions={sizeOptions}
        fileName="ingredients"
        isFinished={isFinished}
        columns={finalColumns}
        data={data || []}
        pageInfo={pageInfo}
        setPageInfo={setPageInfo}
        hidePDFColumnIds={[ingredientTableColumnTexts.calories]}
        getRowId={getRowId}
        {...dataTableTexts}
        useRadioSort={false}
        searchInputProps={{
          value: filter.name || "",
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
          filterKey: "name",
        }}
        extraCriteria={
          <div className="flex items-start justify-center gap-8 flex-1 flex-wrap">
            <div className="flex items-center justify-end gap-4 flex-1 flex-wrap">
              {extraCriteria}
              {/*{dietTypeCriteriaCallback(resetCurrentPage)}*/}
              {/*{isAdmin &&*/}
              {/*  forWhom === "admin" &&*/}
              {/*  fieldCriteriaCallBack(resetCurrentPage)}*/}
            </div>
          </div>
        }
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
