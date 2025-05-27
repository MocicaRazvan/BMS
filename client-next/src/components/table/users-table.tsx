"use client";
import { ExtraTableProps } from "@/types/tables";
import useList, { UseListProps } from "@/hoooks/useList";
import { Link, useRouter } from "@/navigation";
import { CustomEntityModel, UserDto } from "@/types/dto";
import React, { Suspense, useCallback, useMemo } from "react";
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
  RadioBinaryCriteriaWithCallback,
  UseBinaryTexts,
} from "@/components/list/useBinaryFilter";
import useFilterDropdown, {
  RadioFieldFilterCriteriaCallback,
  UseFilterDropdownTexts,
} from "@/components/list/useFilterDropdown";
import { format, parseISO } from "date-fns";
import { ColumnActionsTexts } from "@/texts/components/table";
import { userColumnActions } from "@/lib/constants";
import { useStompClient } from "react-stomp-hooks";
import { AlertDialogMakeTrainer } from "@/components/dialogs/user/make-trainer-alert";
import useClientNotFound from "@/hoooks/useClientNotFound";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
import { appendCreatedAtDesc, cn, wrapItemToString } from "@/lib/utils";
import CreationFilter, {
  CreationFilterTexts,
} from "@/components/list/creation-filter";
import {
  RadioSortButton,
  RadioSortDropDownWithExtra,
  RadioSortDropDownWithExtraDummy,
} from "@/components/common/radio-sort";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

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

type Props = ExtraTableProps & UseListProps & UserTableTexts;

export default function UsersTable({
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
  const { authUser } = useAuthUserMinRole();

  const stompClient = useStompClient();
  const router = useRouter();
  const isAdmin = authUser?.role === "ROLE_ADMIN";

  const { navigateToNotFound } = useClientNotFound();

  const {
    field,
    updateFieldSearch,
    setField: setEmailVerified,
  } = useBinaryFilter({
    fieldKey: "emailVerified",
  });

  const {
    value: provider,
    updateFieldDropdownFilter: updateProvider,
    items: providerItems,
    setField: setProvider,
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
    items: roleItems,
    setField: setRole,
  } = useFilterDropdown({
    items: ["ROLE_USER", "ROLE_ADMIN", "ROLE_TRAINER"].map((value) => ({
      value,
      label: useRoleFilterTexts.labels[value],
    })),
    fieldKey: "role",
    noFilterLabel: useRoleFilterTexts.noFilterLabel,
  });

  const {
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
    updateCreatedAtRange,
  } = useList<CustomEntityModel<UserDto>>({
    path,
    extraQueryParams: {
      ...(extraQueryParams && extraQueryParams),
      ...(field !== null && { emailVerified: field.toString() }),
      // backend accepts lists also for providers and role
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

  const radioArgs = useMemo(
    () => ({
      setSort,
      sortingOptions,
      setSortValue,
      sortValue,
      callback: resetCurrentPage,
      filterKey: "email",
    }),
    [resetCurrentPage, setSort, setSortValue, sortValue, sortingOptions],
  );

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
        enableResizing: true,
        minSize: 35,
        size: 35,
        header: () => (
          <p className="font-bold text-lg text-left">
            {userTableColumnsTexts.id}
          </p>
        ),
        cell: ({
          row: {
            original: { id },
          },
        }) => (
          <OverflowTextTooltip
            text={wrapItemToString(id)}
            triggerClassName="w-10 max-w-10"
          />
        ),
      },
      {
        id: userTableColumnsTexts.email,
        accessorKey: "email",
        enableResizing: true,
        minSize: 220,
        size: 220,
        header: () => (
          <RadioSortButton sortingProperty="email" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {userTableColumnsTexts.email}
            </p>
          </RadioSortButton>
        ),
        cell: ({ row, cell }) => (
          <OverflowTextTooltip
            text={row.original.email}
            triggerStyle={{
              maxWidth: `calc(var(--col-${cell.column.id}-size) * 1px - 10px)`,
            }}
          />
        ),
      },
      {
        id: userTableColumnsTexts.firstName,
        accessorKey: "firstName",
        enableResizing: true,
        minSize: 110,
        size: 110,
        header: () => (
          <RadioSortButton sortingProperty="firstName" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {userTableColumnsTexts.firstName}
            </p>
          </RadioSortButton>
        ),
      },
      {
        id: userTableColumnsTexts.lastName,
        accessorKey: "lastName",
        enableResizing: true,
        minSize: 110,
        size: 110,
        header: () => (
          <RadioSortButton sortingProperty="lastName" radioArgs={radioArgs}>
            <p className="font-bold text-lg text-left">
              {userTableColumnsTexts.lastName}
            </p>
          </RadioSortButton>
        ),
      },

      {
        id: userTableColumnsTexts.emailVerified.header,
        accessorKey: "emailVerified",
        header: () => (
          <RadioSortDropDownWithExtraDummy
            trigger={
              <p className="font-bold text-lg text-left">
                {userTableColumnsTexts.emailVerified.header}
              </p>
            }
            extraContent={
              <RadioBinaryCriteriaWithCallback
                callback={resetCurrentPage}
                fieldKey="emailVerified"
                texts={useBinaryEmailVerifiedTexts}
                setGlobalFilter={setEmailVerified}
              />
            }
          />
        ),
        cell: ({ row }) => (
          <p className="font-bold">
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
          <RadioSortDropDownWithExtraDummy
            trigger={
              <p className="font-bold text-lg text-left">
                {userTableColumnsTexts.provider}
              </p>
            }
            extraContent={
              <RadioFieldFilterCriteriaCallback
                callback={resetCurrentPage}
                fieldKey="provider"
                noFilterLabel={useProviderFilterDropdownTexts.noFilterLabel}
                setGlobalFilter={setProvider}
                items={providerItems}
              />
            }
          />
        ),
        cell: ({ row }) => (
          <p className="font-bold ">{row.original.provider}</p>
        ),
      },
      {
        id: userTableColumnsTexts.role,
        accessorKey: "role",
        header: () => (
          <RadioSortDropDownWithExtraDummy
            trigger={
              <p className={"font-bold text-lg text-left"}>
                {userTableColumnsTexts.role}
              </p>
            }
            extraContent={
              <RadioFieldFilterCriteriaCallback
                callback={resetCurrentPage}
                fieldKey="role"
                noFilterLabel={useRoleFilterTexts.noFilterLabel}
                setGlobalFilter={setRole}
                items={roleItems}
              />
            }
          />
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
          <RadioSortDropDownWithExtra
            radioArgs={radioArgs}
            sortingProperty="createdAt"
            trigger={
              <p className="font-bold text-lg text-left">
                {userTableColumnsTexts.createdAt}
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
                          className:
                            "border-destructive text-destructive w-full",
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
                        className="border-success text-success w-full"
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
      radioArgs,
      resetCurrentPage,
      useBinaryEmailVerifiedTexts,
      setEmailVerified,
      useProviderFilterDropdownTexts.noFilterLabel,
      setProvider,
      providerItems,
      useRoleFilterTexts.noFilterLabel,
      setRole,
      roleItems,
      creationFilterTexts,
      updateCreatedAtRange,
      isAdmin,
      refetch,
      authUser,
      router,
      forWhom,
      handleStartChat,
    ],
  );

  const getRowId = useCallback((row: UserDto) => wrapItemToString(row.id), []);

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
          getRowId={getRowId}
          {...dataTableTexts}
          useRadioSort={false}
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
            filterKey: "email",
          }}
          extraCriteria={
            <div className="flex items-start justify-center gap-8 flex-1 flex-wrap">
              <div className="flex items-center justify-end gap-4 flex-1 flex-wrap">
                {extraCriteria}
              </div>
            </div>
          }
          chartProps={{
            aggregatorConfig: {
              "#": (_) => 1,
              "#Trainer": (r) => (r.role === "ROLE_TRAINER" ? 1 : 0),
              "#User": (r) => (r.role === "ROLE_USER" ? 1 : 0),
              ["# " + userTableColumnsTexts.emailVerified.header]: (r) =>
                Number(r.emailVerified),
              "#Google": (r) => (r.provider === "GOOGLE" ? 1 : 0),
              "#Github": (r) => (r.provider === "GITHUB" ? 1 : 0),
              "#Local": (r) => (r.provider === "LOCAL" ? 1 : 0),
            },
            dateField: "createdAt",
          }}
          showChart={true}
          // rangeDateFilter={
          //   <CreationFilter
          //     {...creationFilterTexts}
          //     updateCreatedAtRange={updateCreatedAtRange}
          //     updateUpdatedAtRange={updateUpdatedAtRange}
          //   />
          // }
        />
      </Suspense>
    </div>
  );
}
