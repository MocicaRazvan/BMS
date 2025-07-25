"use client";
import {
  ArchiveQueue,
  ArchiveQueuePrefix,
  ContainerAction,
  QueueInformation,
} from "@/types/dto";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import useGetQueueArchive from "@/hoooks/useGetQueueArchive";
import { BaseError, getArchiveQueuesNameByPrefix } from "@/types/responses";
import { memo, ReactNode, useCallback, useEffect, useState } from "react";
import { UseFetchStreamReturn } from "@/lib/fetchers/useFetchStream";
import { Skeleton } from "@/components/ui/skeleton";
import { useFormatter } from "next-intl";
import { Button } from "@/components/ui/button";
import { fetchStream } from "@/lib/fetchers/fetchStream";
import { motion } from "framer-motion";
import { Check, RefreshCw, XIcon } from "lucide-react";
import { Locale } from "@/navigation/navigation";
import { parseISO } from "date-fns";
import { cn, isDeepEqual } from "@/lib/utils";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover";
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import ButtonSubmit from "@/components/forms/button-submit";
import { Separator } from "@/components/ui/separator";
import { toast } from "@/components/ui/use-toast";
import { WithUser } from "@/lib/user";
import FadeTextChange from "@/components/ui/fade-text-change";
import { useArchiveQueueUpdateContext } from "@/context/archive-queue-update-context";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";
import dynamic from "next/dynamic";

export interface ArchiveQueueCardsTexts {
  title: Record<"delete" | "update", string>;
  description: string;
  badgeText: Record<"high" | "medium" | "low", string>;
  refreshButtonText: string;
  errorTitle: string;
  errorDescription: string;
  header: string;
  lastRefresh: string;
  currentConsumers: string;
  managePopTexts: ManagePopTexts;
}

interface Props extends ArchiveQueueCardsTexts {
  prefix: ArchiveQueuePrefix;
  locale: Locale;
  showHeader?: boolean;
}
type ToggleRefresh = (queueName: ArchiveQueue) => Promise<unknown>;
const aliveOptions = {
  "60000": "60s",
  "600000": "10m",
  "1200000": "20m",
};
type AliveOptionKey = keyof typeof aliveOptions;

const DynamicCronDisplay = dynamic(
  () => import("@/components/archive/cron-display"),
  {
    ssr: false,
    loading: () => <Skeleton className="min-w-12 min-h-6" />,
  },
);

const ArchiveQueueCards = memo(
  ({ prefix, locale, header, showHeader, ...rest }: Props) => {
    const { authUser } = useAuthUserMinRole();

    const queueNames = getArchiveQueuesNameByPrefix(prefix);
    const deleteQueueName = queueNames.find((q) =>
      q.includes("delete"),
    ) as ArchiveQueue;
    const updateQueueName = queueNames.find((q) =>
      q.includes("update"),
    ) as ArchiveQueue;

    const toggleRefresh = useCallback(async (queueName: ArchiveQueue) => {
      return fetchStream({
        path: "/archive/queue/evict",
        method: "PATCH",
        queryParams: { queueName },
      });
    }, []);

    const deleteQueue = useGetQueueArchive({
      queueName: deleteQueueName,
    });
    const updateQueue = useGetQueueArchive({
      queueName: updateQueueName,
    });

    return (
      <div className="w-full h-full space-y-5 md:space-y-10 pb-5">
        {showHeader && (
          <motion.h2
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            viewport={{ once: true }}
            className="text-2xl lg:text-3xl font-bold tracking-tight mb-12 capitalize "
          >
            {header}
          </motion.h2>
        )}
        <div className="grid gap-6 md:grid-cols-2 w-full max-w-[1100px] m-auto">
          <MotionCardWrapper>
            <DashboardCard
              {...deleteQueue}
              queueName={deleteQueueName}
              toggleRefresh={toggleRefresh}
              locale={locale}
              {...rest}
              authUser={authUser}
              title={rest.title.delete}
            />
          </MotionCardWrapper>
          <MotionCardWrapper>
            <DashboardCard
              {...updateQueue}
              queueName={updateQueueName}
              toggleRefresh={toggleRefresh}
              locale={locale}
              {...rest}
              authUser={authUser}
              title={rest.title.update}
            />
          </MotionCardWrapper>
        </div>
      </div>
    );
  },
  isDeepEqual,
);

ArchiveQueueCards.displayName = "ArchiveQueueCards";
export default ArchiveQueueCards;

const MotionCard = motion(Card);
const MotionCardWrapper = ({ children }: { children: ReactNode }) => (
  <MotionCard
    className="h-50 shadow-md"
    initial={{ opacity: 0, scale: 0.8 }}
    whileInView={{ opacity: 1, scale: 1 }}
    viewport={{ once: true }}
    transition={{
      duration: 0.5,
      delay: 0.15,
      type: "spring",
      stiffness: 200,
      damping: 15,
    }}
  >
    {children}
  </MotionCard>
);

const DashboardCard = ({
  messages,
  error,
  isFinished,
  refetch,
  queueName,
  toggleRefresh,
  locale,
  description,
  errorDescription,
  errorTitle,
  title,
  refreshButtonText,
  badgeText,
  lastRefresh,
  currentConsumers,
  managePopTexts,
  authUser,
}: UseFetchStreamReturn<QueueInformation, BaseError> &
  Omit<ArchiveQueueCardsTexts, "header" | "title"> &
  WithUser & {
    queueName: ArchiveQueue;
    toggleRefresh: ToggleRefresh;
    locale: Locale;
    title: string;
  }) => {
  if (error) {
    return (
      <div className="h-[230px] w-full ">
        <CardHeader>
          <CardTitle className="text-lg font-bold text-destructive text-center capitalize">
            {errorTitle}
          </CardTitle>
        </CardHeader>
        <CardContent className="w-full flex-1  flex flex-col items-center justify-between">
          <p className="text-lg text-destructive text-center">
            {errorDescription}
          </p>
          <div className="flex items-end justify-end w-full mt-2">
            <RefreshButton
              queueName={queueName}
              toggleRefresh={toggleRefresh}
              refetch={refetch}
              text={refreshButtonText}
            />
          </div>
        </CardContent>
      </div>
    );
  }
  if (!isFinished || !messages.length) {
    return (
      <div className="h-[230px]">
        <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
          <CardTitle className="text-lg font-medium capitalize">
            {title}
          </CardTitle>
        </CardHeader>
        <CardContent className="w-full h-[180px]">
          <Skeleton className="size-full" />
        </CardContent>
      </div>
    );
  }
  const { messageCount, timestamp, cronExpression, consumerCount } =
    messages[0];
  return (
    <DashboardSuccessCard
      queueInformation={{
        name: queueName,
        cronExpression,
        consumerCount,
        messageCount,
        timestamp,
      }}
      refetch={refetch}
      queueName={queueName}
      toggleRefresh={toggleRefresh}
      locale={locale}
      title={title}
      badgeText={badgeText}
      currentConsumers={currentConsumers}
      description={description}
      lastRefresh={lastRefresh}
      managePopTexts={managePopTexts}
      refreshButtonText={refreshButtonText}
      authUser={authUser}
    />
  );
};
DashboardCard.displayName = "DashboardCard";

const userTimeZone =
  typeof Intl !== "undefined"
    ? Intl.DateTimeFormat().resolvedOptions().timeZone
    : "UTC";

const DashboardSuccessCard = ({
  refetch,
  queueName,
  toggleRefresh,
  locale,
  description,
  title,
  refreshButtonText,
  badgeText,
  lastRefresh,
  currentConsumers,
  managePopTexts,
  queueInformation: {
    name: pName,
    cronExpression: pCronExpression,
    consumerCount: pConsumerCount,
    messageCount: pMessageCount,
    timestamp: pTimestamp,
  },
  authUser,
}: Omit<
  ArchiveQueueCardsTexts,
  "header" | "title" | "errorTitle" | "errorDescription"
> &
  WithUser & {
    queueName: ArchiveQueue;
    toggleRefresh: ToggleRefresh;
    locale: Locale;
    title: string;
    queueInformation: QueueInformation;
    refetch: () => void;
  }) => {
  const formatIntl = useFormatter();
  const {
    getAction,
    getBatchUpdates,
    getLastCronActionHandled,
    setLastCronActionHandled,
  } = useArchiveQueueUpdateContext();
  const [alive, setAlive] = useState<AliveOptionKey>("60000");
  const [triggerLoading, setTriggerLoading] = useState(false);
  const [stopLoading, setStopLoading] = useState(false);
  const [popOpen, setPopOpen] = useState(false);

  const [
    { messageCount, consumerCount, cronExpression, timestamp, name },
    setQueueInfo,
  ] = useState<QueueInformation>({
    name: pName,
    cronExpression: pCronExpression,
    consumerCount: pConsumerCount,
    messageCount: pMessageCount,
    timestamp: pTimestamp,
  });
  const action = getAction(queueName);
  const {
    count: batchUpdateMessagesCount,
    finished: batchUpdateMessagesFinished,
  } = getBatchUpdates(queueName);

  useEffect(() => {
    if (
      action.action === ContainerAction.START_CRON &&
      action.id !== getLastCronActionHandled(queueName)
    ) {
      console.log(
        "Handling cron action",
        action.id,
        action.action,
        getLastCronActionHandled(queueName),
      );
      setLastCronActionHandled(queueName, action.id);
      refetch();
    }
  }, [action.action, action.id, refetch]);

  const toggleBooleanState = useCallback(
    (
      state: boolean,
      setState: (value: boolean) => void,
      wantedState: boolean,
    ) => {
      if (state !== wantedState) {
        setState(wantedState);
      }
    },
    [],
  );

  useEffect(() => {
    if (action.action === ContainerAction.STOP && consumerCount > 0) {
      setQueueInfo((prev) => ({
        ...prev,
        consumerCount: 0,
        timestamp: new Date().toISOString(),
      }));
      toggleBooleanState(popOpen, setPopOpen, false);
      toggleBooleanState(stopLoading, setStopLoading, false);
      // toast({
      //   title,
      //   description: managePopTexts.consumerStoppedDescription,
      // });
    } else if (
      action.action === ContainerAction.START_MANUAL &&
      consumerCount === 0
    ) {
      setQueueInfo((prev) => ({
        ...prev,
        consumerCount: 1,
        timestamp: new Date().toISOString(),
      }));
      toggleBooleanState(popOpen, setPopOpen, false);
      toggleBooleanState(triggerLoading, setTriggerLoading, false);
      toast({
        title,
        description: managePopTexts.toastSchedule + aliveOptions[alive],
      });
    }
  }, [
    action.action,
    consumerCount,
    alive,
    toggleBooleanState,
    popOpen,
    stopLoading,
    managePopTexts.consumerStoppedDescription,
    managePopTexts.toastSchedule,
    triggerLoading,
    title,
  ]);

  useEffect(() => {
    if (batchUpdateMessagesFinished) {
      // refetch();
    } else if (batchUpdateMessagesCount > 0) {
      setQueueInfo((p) => ({
        ...p,
        messageCount: Math.abs(p.messageCount - batchUpdateMessagesCount),
        timestamp: new Date().toISOString(),
      }));
    }
  }, [batchUpdateMessagesCount, batchUpdateMessagesFinished]);

  return (
    <div className="h-[230px] w-full">
      <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
        <CardTitle className="text-lg font-medium capitalize">
          {title}
        </CardTitle>
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger asChild={true}>
              <Badge
                className="text-[15px]"
                variant={
                  messageCount > 1000
                    ? "destructive"
                    : messageCount > 100
                      ? "secondary"
                      : "default"
                }
              >
                {messageCount > 1000
                  ? badgeText.high
                  : messageCount > 100
                    ? badgeText.medium
                    : badgeText.low}
              </Badge>
            </TooltipTrigger>
            <TooltipContent>
              <div className="flex items-center justify-start">
                <p className="font-medium">{badgeText.low}</p>
                <p>{`: <= 100`}</p>
              </div>
              <div className="flex items-center justify-start">
                <p className="font-medium">{badgeText.medium}</p>
                <p>{`: > 100, <=1000`}</p>
              </div>
              <div className="flex items-center justify-start">
                <p className="font-medium">{badgeText.high}</p>
                <p>{`: > 1000`}</p>
              </div>
            </TooltipContent>
          </Tooltip>
        </TooltipProvider>
      </CardHeader>
      <CardContent className="w-full h-[180px] animate-in fade-in duration-250">
        <FadeTextChange
          text={formatIntl.number(messageCount)}
          className="text-2xl font-bold"
        />
        <p className="text-sm text-muted-foreground my-0.5">{description}</p>
        <div className="w-full h-full text text-muted-foreground space-y-2 ">
          <div className="flex items-center justify-items-start gap-1 w-full mt-4">
            <p>{lastRefresh}</p>
            <p className="ml-0.5 font-semibold text-foreground text-sm ">
              {formatIntl.dateTime(new Date(parseISO(timestamp + "Z")), {
                hour: "numeric",
                hour12: false,
                minute: "numeric",
                second: "numeric",
                year: "2-digit",
                month: "2-digit",
                day: "2-digit",
                timeZone: userTimeZone,
              })}
            </p>
          </div>
          <div className="flex items-end justify-between my-auto gap-2 mt-5 w-full">
            <div>
              <DynamicCronDisplay
                cronExpression={cronExpression}
                locale={locale}
              />
              <p>
                {currentConsumers}{" "}
                {consumerCount > 0 ? (
                  <Check
                    size={24}
                    className="text-success inline font-semibold"
                  />
                ) : (
                  <XIcon
                    size={24}
                    className="text-destructive inline font-semibold"
                  />
                )}
              </p>
            </div>
            <div className="flex items-center justify-end gap-2">
              <RefreshButton
                queueName={queueName}
                toggleRefresh={toggleRefresh}
                refetch={refetch}
                text={refreshButtonText}
              />
              <ManagePop
                queueInformation={{
                  name,
                  cronExpression,
                  consumerCount,
                  messageCount,
                  timestamp,
                }}
                alive={alive}
                setAlive={setAlive}
                errorMessage="error"
                refetch={refetch}
                {...managePopTexts}
                authUser={authUser}
                triggerLoading={triggerLoading}
                stopLoading={stopLoading}
                popOpen={popOpen}
                setPopOpen={setPopOpen}
                setTriggerLoading={setTriggerLoading}
                setStopLoading={setStopLoading}
              />
            </div>
          </div>
        </div>
      </CardContent>
    </div>
  );
};

interface RefreshButtonProps {
  queueName: ArchiveQueue;
  toggleRefresh: ToggleRefresh;
  refetch: () => void;
  text: string;
}
const RefreshButton = ({
  queueName,
  toggleRefresh,
  refetch,
  text,
}: RefreshButtonProps) => (
  <Button
    onClick={() => toggleRefresh(queueName).then(refetch)}
    variant="secondary"
    size="sm"
  >
    <RefreshCw size={16} className="mr-2" />
    {text}
  </Button>
);

interface ManagePopTexts {
  manageBtn: string;
  selectLabel: string;
  scheduleBtn: string;
  scheduleBtnLoading: string;
  stopBtn: string;
  stopBtnLoading: string;
  stopTooltip: string;
  toastSchedule: string;
  toastStop: string;
  toastCron: string;
  scheduleTooltip: string;
  consumerStoppedDescription: string;
}
interface ManagePopProps extends ManagePopTexts, WithUser {
  queueInformation: QueueInformation;
  alive: string;
  setAlive: (value: AliveOptionKey) => void;
  refetch: () => void;
  errorMessage: string;
  triggerLoading: boolean;
  stopLoading: boolean;
  popOpen: boolean;
  setPopOpen: (value: boolean) => void;
  setTriggerLoading: (value: boolean) => void;
  setStopLoading: (value: boolean) => void;
}

const ManagePop = ({
  queueInformation: { name: queueName, consumerCount },
  alive,
  setAlive,
  errorMessage,
  manageBtn,
  stopBtnLoading,
  scheduleBtnLoading,
  stopBtn,
  scheduleBtn,
  stopTooltip,
  selectLabel,
  toastStop,
  toastSchedule,
  scheduleTooltip,
  authUser,
  popOpen,
  setPopOpen,
  triggerLoading,
  stopLoading,
  setTriggerLoading,
  setStopLoading,
}: ManagePopProps) => {
  const [scheduleError, setScheduleError] = useState<string | null>(null);
  const schedule = useCallback(async () => {
    setScheduleError(null);
    setTriggerLoading(true);
    const resp = await fetchStream<QueueInformation>({
      path: "/archive/queue",
      queryParams: {
        queueName,
      },
      token: authUser.token,
    });
    if (resp.error) {
      console.error(resp.error);
      setScheduleError(errorMessage);
      return;
    }

    fetchStream<QueueInformation>({
      path: "/archive/container/schedule",
      method: "PATCH",
      queryParams: { queueName, alive },
    }).then(async ({ messages, error }) => {
      if (error) {
        console.error(error);
        setScheduleError(errorMessage);
        setTriggerLoading(false);
        return;
      }
    });
  }, [alive, authUser.token, errorMessage, queueName, setTriggerLoading]);
  const stopContainer = useCallback(() => {
    setScheduleError(null);
    setStopLoading(true);
    fetchStream({
      path: "/archive/container/stop",
      method: "PATCH",
      queryParams: { queueName },
    }).then(({ messages, error }) => {
      if (error) {
        console.error("/archive/container/stop", error);
        setScheduleError(errorMessage);
        setStopLoading(false);
        return;
      }
    });
  }, [errorMessage, queueName, setStopLoading]);

  return (
    <Popover open={popOpen} onOpenChange={setPopOpen}>
      <PopoverTrigger asChild={true}>
        <Button variant="outline" size="sm">
          {manageBtn}
        </Button>
      </PopoverTrigger>
      <PopoverContent className="space-y-5">
        <Select value={alive} onValueChange={setAlive}>
          <SelectTrigger defaultValue="60000">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectGroup>
              <SelectLabel>{selectLabel}</SelectLabel>
              {/*<SelectItem value="60000">{"60s"}</SelectItem>*/}
              {/*<SelectItem value="600000">{"10m"}</SelectItem>*/}
              {/*<SelectItem value="1200000">{"20m"}</SelectItem>*/}
              {Object.entries(aliveOptions).map(([key, value]) => (
                <SelectItem key={key + "_" + queueName} value={key}>
                  {value}
                </SelectItem>
              ))}
            </SelectGroup>
          </SelectContent>
        </Select>
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger
              className={cn(
                (triggerLoading || consumerCount > 0) && "cursor-not-allowed",
              )}
              asChild
            >
              <ButtonSubmit
                buttonSubmitTexts={{
                  submitText: scheduleBtn,
                  loadingText: scheduleBtnLoading,
                }}
                isLoading={triggerLoading}
                size="sm"
                disable={triggerLoading || consumerCount > 0}
                onClick={schedule}
              />
            </TooltipTrigger>
            {(stopLoading || consumerCount > 0) && (
              <TooltipContent>
                <p>{scheduleTooltip}</p>
              </TooltipContent>
            )}
          </Tooltip>
        </TooltipProvider>
        <Separator />
        <TooltipProvider>
          <Tooltip>
            <TooltipTrigger
              className={cn(
                (stopLoading || !consumerCount) && "cursor-not-allowed",
              )}
              asChild={true}
            >
              <ButtonSubmit
                buttonSubmitTexts={{
                  submitText: stopBtn,
                  loadingText: stopBtnLoading,
                }}
                isLoading={stopLoading}
                size="sm"
                disable={stopLoading || !consumerCount}
                onClick={stopContainer}
                variant="destructive"
              />
            </TooltipTrigger>
            {(stopLoading || !consumerCount) && (
              <TooltipContent>
                <p>{stopTooltip}</p>
              </TooltipContent>
            )}
          </Tooltip>
        </TooltipProvider>
        {scheduleError && <p className="text-destructive">{scheduleError}</p>}
      </PopoverContent>
    </Popover>
  );
};
