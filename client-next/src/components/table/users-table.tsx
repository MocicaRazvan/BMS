"use client";
import { ExtraTableProps } from "@/types/tables";
import useList, { UseListProps } from "@/hoooks/useList";
import { WithUser } from "@/lib/user";
import { Link, useRouter } from "@/navigation";
import { CustomEntityModel, UserDto } from "@/types/dto";
import { Suspense, useCallback, useMemo } from "react";
import { ColumnDef } from "@tanstack/react-table";
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
import LoadingSpinner from "@/components/common/loading-spinner";
import { DataTable, DataTableTexts } from "@/components/table/data-table";
import useBinaryFilter, {
  UseBinaryTexts,
} from "@/components/list/useBinaryFilter";
import useFilterDropdown, {
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import { format, parseISO } from "date-fns";
import { ColumnActionsTexts } from "@/texts/components/table";
import { userColumnActions } from "@/lib/constants";
import { useStompClient } from "react-stomp-hooks";
import { AlertDialogMakeTrainer } from "@/components/dialogs/user/make-trainer-alert";
import useClientNotFound from "@/hoooks/useClientNotFound";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
import { appendCreatedAtDesc, cn } from "@/lib/utils";
import CreationFilter, {
  CreationFilterTexts,
} from "@/components/list/creation-filter";

export interface UserTableColumnsTexts {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  provider: string;
  emailVerified: {
    header: string;
    trueText: string;
    falseText: string;
  };
  createdAt: string;
  actions: ColumnActionsTexts<typeof userColumnActions>;
}

export interface UserTableTexts {
  dataTableTexts: DataTableTexts;
  userTableColumnsTexts: UserTableColumnsTexts;
  useProviderFilterDropdownTexts: UseFilterDropdownTexts;
  useRoleFilterTexts: UseFilterDropdownTexts;
  useBinaryEmailVerifiedTexts: UseBinaryTexts;
  search: string;
  creationFilterTexts: CreationFilterTexts;
}

type Props = ExtraTableProps & UseListProps & WithUser & UserTableTexts;

export default function UsersTable({
  authUser,
  mainDashboard,
  extraQueryParams,
  extraArrayQueryParam,
  extraUpdateSearchParams,
  extraCriteria,
  path,
  sortingOptions,
  sizeOptions,
  forWhom,
  dataTableTexts,
  userTableColumnsTexts,
  useProviderFilterDropdownTexts,
  useRoleFilterTexts,
  useBinaryEmailVerifiedTexts,
  search,
  creationFilterTexts,
}: Props) {
  const stompClient = useStompClient();
  const router = useRouter();
  const isAdmin = authUser?.role === "ROLE_ADMIN";

  const { navigateToNotFound } = useClientNotFound();

  const { fieldCriteria, field, updateFieldSearch, fieldCriteriaCallBack } =
    useBinaryFilter({
      fieldKey: "emailVerified",
      ...useBinaryEmailVerifiedTexts,
    });

  const {
    value: provider,
    updateFieldDropdownFilter: updateProvider,
    fieldDropdownFilterQueryParam: providerDropdownFilterQueryParam,
    filedFilterCriteria: providerFilterCriteria,
    filedFilterCriteriaCallback: providerFilterCriteriaCallback,
  } = useFilterDropdown({
    items: ["LOCAL", "GITHUB", "GOOGLE"].map((value) => ({
      value,
      label: useProviderFilterDropdownTexts.labels[value],
    })),
    fieldKey: "provider",
    noFilterLabel: useProviderFilterDropdownTexts.noFilterLabel,
  });
  const {
    value: role,
    updateFieldDropdownFilter: updateRole,
    fieldDropdownFilterQueryParam: roleDropdownFilterQueryParam,
    filedFilterCriteria: roleFilterCriteria,
    filedFilterCriteriaCallback: roleFilterCriteriaCallback,
  } = useFilterDropdown({
    items: ["ROLE_USER", "ROLE_ADMIN", "ROLE_TRAINER"].map((value) => ({
      value,
      label: useRoleFilterTexts.labels[value],
    })),
    fieldKey: "role",
    noFilterLabel: useRoleFilterTexts.noFilterLabel,
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
    updateUpdatedAtRange,
    updateCreatedAtRange,
  } = useList<CustomEntityModel<UserDto>>({
    path,
    extraQueryParams: {
      ...(extraQueryParams && extraQueryParams),
      ...(field !== null && { emailVerified: field.toString() }),
      // backend accepts lists alos for providers and role
      ...(provider && { providers: provider }),
      ...(role && { roles: role }),
    },
    extraArrayQueryParam,
    extraUpdateSearchParams: (p) => {
      extraUpdateSearchParams?.(p);
      updateProvider(p);
      updateRole(p);
      updateFieldSearch(p);
    },
    sizeOptions,
    sortingOptions,
    filterKey: "email",
  });

  const data = useMemo(() => items.map((i) => i.content), [items]);

  const handleStartChat = useCallback(
    (recUser: UserDto) => {
      if (stompClient && stompClient?.connected && recUser && authUser) {
        stompClient?.publish({
          destination: "/app/addChatRoom",
          body: JSON.stringify({
            users: [
              { email: recUser.email, connectedStatus: "OFFLINE" },
              {
                email: authUser.email,
                connectedStatus: "ONLINE",
              },
            ],
          }),
        });
        router.push(`/chat?email=${recUser.email}`);
      }
    },
    [authUser, router, stompClient?.connected],
  );

  const columns: ColumnDef<UserDto>[] = useMemo(
    () => [
      {
        id: userTableColumnsTexts.id,
        accessorKey: "id",
        header: () => (
          <p className="font-bold text-lg text-left">
            {userTableColumnsTexts.id}
          </p>
        ),
      },
      {
        id: userTableColumnsTexts.email,
        accessorKey: "email",
        header: () => (
          <p className="font-bold text-lg text-left">
            {userTableColumnsTexts.email}
          </p>
        ),
        cell: ({ row }) => (
          <OverflowTextTooltip
            text={row.original.email}
            triggerClassName="max-w-[200px]"
          />
        ),
      },
      {
        id: userTableColumnsTexts.firstName,
        accessorKey: "firstName",
        header: () => (
          <p className="font-bold text-lg text-left">
            {userTableColumnsTexts.firstName}
          </p>
        ),
      },
      {
        id: userTableColumnsTexts.lastName,
        accessorKey: "lastName",
        header: () => (
          <p className="font-bold text-lg text-left">
            {userTableColumnsTexts.lastName}
          </p>
        ),
      },

      {
        id: userTableColumnsTexts.emailVerified.header,
        accessorKey: "emailVerified",
        header: () => (
          <p className="font-bold text-lg text-left">
            {userTableColumnsTexts.emailVerified.header}
          </p>
        ),
        cell: ({ row }) => (
          <p className="font-bold ">
            {row.original.emailVerified
              ? userTableColumnsTexts.emailVerified.trueText
              : userTableColumnsTexts.emailVerified.falseText}
          </p>
        ),
      },
      {
        id: userTableColumnsTexts.provider,
        accessorKey: "provider",
        header: () => (
          <p className="font-bold text-lg text-left">
            {userTableColumnsTexts.provider}
          </p>
        ),
        cell: ({ row }) => (
          <p className="font-bold ">{row.original.provider}</p>
        ),
      },
      {
        id: userTableColumnsTexts.role,
        accessorKey: "role",
        header: () => (
          <p className={"font-bold text-lg text-left"}>
            {userTableColumnsTexts.role}
          </p>
        ),
        cell: ({ row }) => (
          <p
            className={cn(
              "font-bold",
              row.original.role === "ROLE_ADMIN"
                ? "text-destructive"
                : row.original.role === "ROLE_TRAINER"
                  ? "text-success"
                  : "text-primary",
            )}
          >
            {(row.original.role as string)?.split("_")?.[1]}
          </p>
        ),
      },
      {
        id: userTableColumnsTexts.createdAt,
        accessorKey: "createdAt",
        header: () => (
          <p className="font-bold text-lg text-left">
            {userTableColumnsTexts.createdAt}
          </p>
        ),
        cell: ({ row }) => (
          <p>{format(parseISO(row.original.createdAt), "dd/MM/yyyy")}</p>
        ),
      },

      {
        id: "actions",
        cell: ({ row }) => {
          const user = row.original;
          const {
            button,
            startChat,
            viewUser,
            viewOrders,
            viewPlans,
            viewPosts,
            viewRecipes,
            copyEmail,
            label,
            makeTrainer,
            viewMonthlySales,
            viewDailySales,
          } = userTableColumnsTexts.actions;

          return (
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" className="h-8 w-8 p-0">
                  <span className="sr-only">{button}</span>
                  <MoreHorizontal className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuLabel className="mb-3">{label}</DropdownMenuLabel>
                <DropdownMenuItem
                  className="cursor-pointer"
                  onClick={() => navigator.clipboard.writeText(user.email)}
                >
                  {copyEmail}
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  className="cursor-pointer"
                  onClick={() =>
                    router.push(
                      forWhom === "admin"
                        ? `/admin/users/${user.id}`
                        : `/users/single/${user.id}`,
                    )
                  }
                >
                  {viewUser}
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem asChild className="cursor-pointer">
                  <Link
                    href={appendCreatedAtDesc(`/admin/users/${user.id}/orders`)}
                    className="cursor-pointer"
                  >
                    {viewOrders}
                  </Link>
                </DropdownMenuItem>
                {user.role !== "ROLE_USER" && (
                  <>
                    <DropdownMenuSeparator />

                    <DropdownMenuItem asChild className="cursor-pointer">
                      <Link
                        href={appendCreatedAtDesc(
                          `/admin/users/${user.id}/posts`,
                        )}
                        className="cursor-pointer"
                      >
                        {viewPosts}
                      </Link>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem asChild className="cursor-pointer">
                      <Link
                        href={appendCreatedAtDesc(
                          `/admin/users/${user.id}/recipes`,
                        )}
                        className="cursor-pointer"
                      >
                        {viewRecipes}
                      </Link>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem asChild className="cursor-pointer">
                      <Link
                        href={appendCreatedAtDesc(
                          `/admin/users/${user.id}/plans`,
                        )}
                        className="cursor-pointer"
                      >
                        {viewPlans}
                      </Link>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem asChild className="cursor-pointer">
                      <Link
                        href={`/admin/users/${user.id}/monthlySales`}
                        className="cursor-pointer"
                      >
                        {viewMonthlySales}
                      </Link>
                    </DropdownMenuItem>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem asChild className="cursor-pointer">
                      <Link
                        href={`/admin/users/${user.id}/dailySales`}
                        className="cursor-pointer"
                      >
                        {viewDailySales}
                      </Link>
                    </DropdownMenuItem>
                  </>
                )}
                {isAdmin && user.role === "ROLE_USER" && (
                  <>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem asChild>
                      <AlertDialogMakeTrainer
                        user={user}
                        successCallback={refetch}
                        authUser={authUser}
                        buttonProps={{
                          size: "sm",
                          className:
                            "text-md border-destructive text-destructive",
                          variant: "outline",
                        }}
                        anchorText={makeTrainer}
                      />
                    </DropdownMenuItem>
                  </>
                )}
                {user.id !== parseInt(authUser.id) && (
                  <>
                    <DropdownMenuSeparator />
                    <DropdownMenuItem asChild>
                      <Button
                        variant="outline"
                        className="cursor-pointer border-success text-success !hover:outline-none !hover:ring-0 !hover:border-success !hover:text-success"
                        onClick={() => handleStartChat(user)}
                      >
                        {startChat}
                      </Button>
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
      userTableColumnsTexts.id,
      userTableColumnsTexts.email,
      userTableColumnsTexts.firstName,
      userTableColumnsTexts.lastName,
      userTableColumnsTexts.emailVerified.header,
      userTableColumnsTexts.emailVerified.trueText,
      userTableColumnsTexts.emailVerified.falseText,
      userTableColumnsTexts.provider,
      userTableColumnsTexts.role,
      userTableColumnsTexts.createdAt,
      userTableColumnsTexts.actions,
      isAdmin,
      refetch,
      authUser,
      router,
      handleStartChat,
    ],
  );

  if (error?.status) {
    return navigateToNotFound();
  }

  return (
    <div className="px-1 w-full space-y-8 lg:space-y-14">
      <Suspense fallback={<LoadingSpinner />}>
        <DataTable
          sizeOptions={sizeOptions}
          fileName={`users`}
          isFinished={isFinished}
          columns={columns}
          data={data || []}
          pageInfo={pageInfo}
          setPageInfo={setPageInfo}
          {...dataTableTexts}
          searchInputProps={{
            value: filter.email || "",
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
                {fieldCriteriaCallBack(resetCurrentPage)}
                {providerFilterCriteriaCallback(resetCurrentPage)}
                {roleFilterCriteriaCallback(resetCurrentPage)}
              </div>
            </div>
          }
          rangeDateFilter={
            <CreationFilter
              {...creationFilterTexts}
              updateCreatedAtRange={updateCreatedAtRange}
              updateUpdatedAtRange={updateUpdatedAtRange}
            />
          }
        />
      </Suspense>
    </div>
  );
}
