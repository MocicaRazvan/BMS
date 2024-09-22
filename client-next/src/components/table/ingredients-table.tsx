"use client";

import { ExtraTableProps } from "@/types/tables";
import { WithUser } from "@/lib/user";
import useList, { UseListProps } from "@/hoooks/useList";
import useBinaryFilter, {
  UseBinaryTexts,
} from "@/components/list/useBinaryFilter";
import useFilterDropdown, {
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import { dietTypes } from "@/types/forms";
import {
  CustomEntityModel,
  IngredientNutritionalFactResponse,
  ResponseWithEntityCount,
} from "@/types/dto";
import { Suspense, useMemo } from "react";
import { ColumnDef } from "@tanstack/react-table";
import { DataTable, DataTableTexts } from "@/components/table/data-table";
import LoadingSpinner from "@/components/common/loading-spinner";
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
}

export type IngredientTableProps = ExtraTableProps &
  WithUser &
  UseListProps &
  IngredientTableTexts & {
    isSidebarOpen?: boolean;
  };

export default function IngredientsTable({
  forWhom,
  authUser,
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
}: IngredientTableProps) {
  const router = useRouter();
  const isAdmin = authUser?.role === "ROLE_ADMIN";
  const { field, updateFieldSearch, fieldCriteriaCallBack } = useBinaryFilter({
    fieldKey: "display",
    ...displayFilterTexts,
  });

  const { navigateToNotFound } = useClientNotFound();

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

  const columns: ColumnDef<
    ResponseWithEntityCount<IngredientNutritionalFactResponse>
  >[] = useMemo(
    () => [
      {
        id: ingredientTableColumnTexts.id,
        accessorKey: "model.ingredient.id",
        header: () => (
          <p className="font-bold text-lg text-left">
            {ingredientTableColumnTexts.id}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.ingredient.id}</p>,
      },
      {
        id: ingredientTableColumnTexts.name,
        accessorKey: "model.ingredient.name",
        header: () => (
          <p className="font-bold text-lg text-left">
            {ingredientTableColumnTexts.name}
          </p>
        ),
        cell: ({ row }) => (
          <OverflowTextTooltip text={row.original.model.ingredient.name} />
        ),
      },
      {
        id: ingredientTableColumnTexts.type,
        accessorKey: "model.ingredient.type",
        header: () => (
          <p className="font-bold text-lg text-left">
            {ingredientTableColumnTexts.type}
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
                colorMap[row.original.model.ingredient.type] as
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
        header: () => (
          <p className="font-bold text-lg text-left">
            {ingredientTableColumnTexts.display.header}
          </p>
        ),
        cell: ({ row }) => (
          <Badge
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
        header: () => (
          <p className="font-bold text-lg text-left">
            {ingredientTableColumnTexts.fat}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.nutritionalFact.fat}</p>,
      },
      {
        id: ingredientTableColumnTexts.saturatedFat,
        accessorKey: "model.nutritionalFact.saturatedFat",
        header: () => (
          <p className="font-bold text-lg text-left">
            {ingredientTableColumnTexts.saturatedFat}
          </p>
        ),
        cell: ({ row }) => (
          <p>{row.original.model.nutritionalFact.saturatedFat}</p>
        ),
      },
      {
        id: ingredientTableColumnTexts.carbohydrates,
        accessorKey: "model.nutritionalFact.carbohydrates",
        header: () => (
          <div className="max-w-16 text-nowrap overflow-x-hidden">
            <p className="font-bold text-lg text-left">
              {ingredientTableColumnTexts.carbohydrates}
            </p>
          </div>
        ),
        cell: ({ row }) => (
          <p>{row.original.model.nutritionalFact.carbohydrates}</p>
        ),
      },

      {
        id: ingredientTableColumnTexts.sugar,
        accessorKey: "model.nutritionalFact.sugar",
        header: () => (
          <p className="font-bold text-lg text-left">
            {ingredientTableColumnTexts.sugar}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.nutritionalFact.sugar}</p>,
      },
      {
        id: ingredientTableColumnTexts.protein,
        accessorKey: "model.nutritionalFact.protein",
        header: () => (
          <p className="font-bold text-lg text-left">
            {ingredientTableColumnTexts.protein}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.nutritionalFact.protein}</p>,
      },

      {
        id: ingredientTableColumnTexts.salt,
        accessorKey: "model.nutritionalFact.salt",
        header: () => (
          <p className="font-bold text-lg text-left">
            {ingredientTableColumnTexts.salt}
          </p>
        ),
        cell: ({ row }) => <p>{row.original.model.nutritionalFact.salt}</p>,
      },
      {
        id: ingredientTableColumnTexts.calories,
        header: () => (
          <p className="font-bold text-lg text-left">
            {ingredientTableColumnTexts.calories}
          </p>
        ),
        cell: ({ row }) => <p>{getCalories(row.original.model)}</p>,
      },
      {
        id: ingredientTableColumnTexts.unit,
        accessorKey: "model.nutritionalFact.unit",
        header: () => (
          <p className="font-bold text-lg text-left">
            {ingredientTableColumnTexts.unit}
          </p>
        ),
        cell: ({ row }) => (
          <Badge
            variant={
              row.original.model.nutritionalFact.unit === "GRAM"
                ? "secondary"
                : "default"
            }
          >
            {row.original.model.nutritionalFact.unit}
          </Badge>
        ),
      },

      // {
      //   id: ingredientTableColumnTexts.createdAt,
      //   accessorKey: "ingredient.createdAt",
      //   header: () => (
      //     <p className="font-bold text-lg text-left">
      //       {ingredientTableColumnTexts.createdAt}
      //     </p>
      //   ),
      //   cell: ({ row }) => (
      //     <p>
      //       {format(parseISO(row.original.ingredient.createdAt), "dd/MM/yyyy")}
      //     </p>
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
                        </DropdownMenuItem>{" "}
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
      authUser,
      forWhom,
      ingredientTableColumnTexts.actions,
      ingredientTableColumnTexts.calories,
      ingredientTableColumnTexts.carbohydrates,
      ingredientTableColumnTexts.display.false,
      ingredientTableColumnTexts.display.header,
      ingredientTableColumnTexts.display.true,
      ingredientTableColumnTexts.fat,
      ingredientTableColumnTexts.id,
      ingredientTableColumnTexts.name,
      ingredientTableColumnTexts.protein,
      ingredientTableColumnTexts.salt,
      ingredientTableColumnTexts.saturatedFat,
      ingredientTableColumnTexts.sugar,
      ingredientTableColumnTexts.type,
      ingredientTableColumnTexts.unit,
      ingredientTableColumnTexts.count,
      isAdmin,
      refetch,
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

  if (error?.status) {
    return navigateToNotFound();
  }

  return (
    <div className="px-1 w-full space-y-8 lg:space-y-14 overflow-x-hidden ">
      <Suspense fallback={<LoadingSpinner />}>
        <DataTable
          sizeOptions={sizeOptions}
          fileName="ingredients"
          isFinished={isFinished}
          columns={finalColumns}
          data={data || []}
          pageInfo={pageInfo}
          setPageInfo={setPageInfo}
          hidePDFColumnIds={[ingredientTableColumnTexts.calories]}
          {...dataTableTexts}
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
            setSortValue,
            sortValue,
            callback: resetCurrentPage,
          }}
          extraCriteria={
            <div className="flex items-start justify-center gap-8 flex-1 flex-wrap">
              <div className="flex items-center justify-end gap-4 flex-1 flex-wrap">
                {extraCriteria}
                {dietTypeCriteriaCallback(resetCurrentPage)}
                {isAdmin &&
                  forWhom === "admin" &&
                  fieldCriteriaCallBack(resetCurrentPage)}
              </div>
            </div>
          }
        />
      </Suspense>
    </div>
  );
}
