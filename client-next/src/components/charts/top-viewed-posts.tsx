"use client";

import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { cn } from "@/lib/utils";
import React, { Suspense, useMemo, useState } from "react";
import useFetchStream from "@/hoooks/useFetchStream";
import { PostCountSummaryResponse } from "@/types/dto";
import { motion } from "framer-motion";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import Lottie from "react-lottie-player";
import noResultsLottie from "../../../public/lottie/noResults.json";
import Loader from "@/components/ui/spinner";
import { WithUser } from "@/lib/user";
import { Link, Locale } from "@/navigation";
import { TopRankBadge } from "@/components/charts/top-chart-wrapper";
import { useFormatter, useLocale } from "next-intl";
import {
  DateRangeParams,
  DateRangePicker,
  DateRangePickerTexts,
} from "@/components/ui/date-range-picker";
import { format, subMonths } from "date-fns";
import { ro } from "date-fns/locale";
import OverflowTextTooltip from "@/components/common/overflow-text-tooltip";
import { useAuthUserMinRole } from "@/context/auth-user-min-role-context";

export interface TopViewedPostsTexts extends PostViewCardTexts {
  title: string;
  topLabel: string;
  noResults: string;
  dateRangePickerTexts: DateRangePickerTexts;
  periodLabel: string;
}
interface Props {
  path: string;
  texts: TopViewedPostsTexts;
}

const topOptions = Array.from({ length: 10 }, (_, i) => i + 1);
const now = new Date();
const oneMonthAgo = subMonths(now, 1);
const dateFormat = "dd-MM-yyyy";
const formattedNow = format(now, dateFormat);
const formattedOneMonthAgo = format(oneMonthAgo, dateFormat);
export default function TopViewedPosts({ path, texts }: Props) {
  const { authUser } = useAuthUserMinRole();

  const locale = useLocale() as Locale;
  const [top, setTop] = useState<string>("3");
  const [dateRange, setDateRange] = useState<DateRangeParams | null>(null);

  const { messages, error, isFinished } = useFetchStream<
    PostCountSummaryResponse[]
  >({
    path,
    authToken: true,
    queryParams: {
      top,
      ...(dateRange && {
        accessedStart: dateRange.from,
        accessedEnd: dateRange.to,
      }),
    },
  });

  const dateRangePicker = useMemo(
    () => (
      <DateRangePicker
        onUpdate={({ range: { from, to } }, none) => {
          if (none) {
            setDateRange(null);
            return;
          }
          setDateRange({
            from: format(from, dateFormat),
            to: format(to || from, dateFormat),
          });
        }}
        align="center"
        locale={locale === "ro" ? ro : undefined}
        defaultPreset={"lastMonth"}
        showNone={true}
        defaultNone={true}
        showCompare={false}
        {...texts.dateRangePickerTexts}
      />
    ),
    [texts.dateRangePickerTexts, locale],
  );

  const noMessageOrError =
    isFinished && (!messages?.at(0)?.length || error !== null);

  return (
    <motion.div
      className="w-full h-full p-4 "
      initial={{ opacity: 0 }}
      whileInView={{ opacity: 1 }}
      viewport={{ once: true, amount: "some" }}
    >
      <h2 className="text-2xl lg:text-3xl font-bold tracking-tight capitalize inline ">
        {texts.title}
      </h2>
      <div className="flex flex-col md:flex-row gap-5 items-center justify-around w-full mt-2 mb-12">
        <div className="flex items-center gap-2">
          <Label className="text-lg font-semibold">{texts.periodLabel}</Label>
          {dateRangePicker}
        </div>
        <div className="flex items-center gap-2">
          <Label className="text-lg font-semibold" htmlFor="top-select">
            {texts.topLabel}
          </Label>
          <Select value={top} onValueChange={(v) => setTop(v)}>
            <SelectTrigger className="w-36" id="top-select">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {topOptions.map((option) => (
                <SelectItem
                  key={option + "select"}
                  value={`${option}`}
                  className={cn(
                    "cursor-pointer capitalize",
                    messages?.at(0)?.length === option && "text-amber",
                  )}
                >
                  {option}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </div>
      {noMessageOrError && (
        <div className="block w-full h-full">
          <h2 className="text-4xl tracking-tighter font-bold w-full max-w-3xl max-h-[550px] mx-auto">
            <p className="text-center">{texts.noResults}</p>
            <Suspense fallback={<div className="md:w-1/3 md:h-1/3 mx-auto" />}>
              <Lottie
                animationData={noResultsLottie}
                loop
                className="md:w-1/3 md:h-1/3 mx-auto"
                play
              />
            </Suspense>
          </h2>
        </div>
      )}
      {!isFinished ? (
        <div className="flex items-center justify-center h-[235px]">
          <Loader />
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {messages
            ?.at(0)
            ?.map((postSummary, index) => (
              <PostViewCard
                key={postSummary.id + "tvp" + index}
                postSummary={postSummary}
                authUser={authUser}
                texts={texts}
              />
            ))}
        </div>
      )}
    </motion.div>
  );
}

const MotionCard = motion(Card);

interface PostViewCardTexts {
  rank: string;
  viewCount: string;
  tags: string;
}

interface PostViewProps extends WithUser {
  postSummary: PostCountSummaryResponse;
  texts: PostViewCardTexts;
}
function PostViewCard({ postSummary, authUser, texts }: PostViewProps) {
  const formatter = useFormatter();
  const postLink =
    authUser.role === "ROLE_ADMIN"
      ? "/admin/posts/single/" + postSummary.id
      : "/trainer/posts/single/" + postSummary.id;
  return (
    <MotionCard
      className="flex flex-col min-h-[235px] shadow justify-between "
      initial={{ opacity: 0, scale: 0.8 }}
      whileInView={{ opacity: 1, scale: 1 }}
      viewport={{ once: true, amount: "some" }}
      transition={{
        duration: 0.5,
        delay: 0.15,
        type: "spring",
        stiffness: 200,
        damping: 15,
      }}
    >
      <CardHeader>
        <CardTitle>
          <Link
            href={postLink}
            className="hover:underline flex items-center justify-center gap-2 pb-1"
          >
            <OverflowTextTooltip
              text={postSummary.title}
              triggerClassName="max-w-[230px] md:max-w-[400px]"
            />
          </Link>
        </CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col justify-between gap-6">
        <div className="flex items-center justify-between flex-1">
          <div className="flex items-center justify-center gap-2.5">
            <TopRankBadge rankLabel={texts.rank} rank={postSummary.rank} />
          </div>
          <div className="flex items-center justify-center gap-2.5">
            <p className="text-lg">{texts.viewCount}</p>
            <p className="font-semibold text-success text-lg">
              {formatter.number(postSummary.viewCount)}
            </p>
          </div>
        </div>
        <div className="flex items-center justify-start gap-1.5 h-9 overflow-hidden">
          <p>{texts.tags}</p>
          <p className="text-sm text-muted-foreground text-wrap">
            {postSummary.tags.join(", ")}
          </p>
        </div>
      </CardContent>
    </MotionCard>
  );
}
