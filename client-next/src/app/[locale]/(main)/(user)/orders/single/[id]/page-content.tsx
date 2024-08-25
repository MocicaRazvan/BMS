"use client";

import { WithUser } from "@/lib/user";
import useFetchStream from "@/hoooks/useFetchStream";
import {
  CustomEntityModel,
  CustomInvoiceDto,
  OrderDtoWithAddress,
  PageableResponse,
  PlanResponse,
  ResponseWithUserDtoEntity,
} from "@/types/dto";
import { checkOwnerOrAdmin, isSuccessCheckReturn } from "@/lib/utils";
import LoadingSpinner from "@/components/common/loading-spinner";
import React, { useEffect, useState } from "react";
import { fetchStream } from "@/hoooks/fetchStream";
import Image from "next/image";
import { Link, useRouter } from "@/navigation";
import { Button } from "@/components/ui/button";

import { Card } from "@/components/ui/card";

import { useFormatter } from "next-intl";
import { format } from "date-fns";
import useClientNotFound from "@/hoooks/useClientNotFound";
import { motion } from "framer-motion";

export interface SingleOrderPageContentTexts {
  title: string;
  total: string;
  seeInvoice: string;
}
interface Props extends WithUser, SingleOrderPageContentTexts {
  id: string;
}
export default function SingleOrderPageContent({
  authUser,
  id,
  total,
  title,
  seeInvoice,
}: Props) {
  const [invoice, setInvoice] = useState<CustomInvoiceDto | null>(null);
  const [isMounted, setIsMounted] = useState(false);
  const formatIntl = useFormatter();
  const router = useRouter();
  const { navigateToNotFound } = useClientNotFound();

  useEffect(() => {
    setIsMounted(true);
  }, []);

  const {
    messages: ordersAddress,
    error: orderError,
    isFinished: orderFinished,
  } = useFetchStream<CustomEntityModel<OrderDtoWithAddress>>({
    path: `/orders/${id}`,
    method: "GET",
    authToken: true,
    useAbortController: false,
  });
  console.log("HOOKS After useFetchStream for orders");

  const {
    messages: plans,
    error: plansError,
    isFinished: plansIsFinished,
  } = useFetchStream<PageableResponse<ResponseWithUserDtoEntity<PlanResponse>>>(
    {
      path: `/orders/subscriptionsByOrder/${id}`,
      method: "GET",
      authToken: true,
    },
  );
  console.log("HOOKS After useFetchStream for plans");
  console.log("HOOKS Before useEffect for invoice fetching");

  useEffect(() => {
    if (ordersAddress.length > 0) {
      fetchStream<CustomInvoiceDto>({
        path: `/orders/invoices/${ordersAddress[0].content.order.stripeInvoiceId}`,
        method: "GET",
        token: authUser.token,
        successCallback: (data) => {
          setInvoice(data);
        },
      }).catch(() => navigateToNotFound());
    }
  }, [authUser.token, JSON.stringify(ordersAddress)]);
  console.log("HOOKS After useEffect for invoice fetching");

  if (!isMounted) return null;

  if (!orderFinished || !plansIsFinished)
    return (
      <section className="w-full min-h-[calc(100vh-4rem)] flex items-center justify-center transition-all">
        <LoadingSpinner />
      </section>
    );

  if (orderError?.status || plansError?.status) {
    return navigateToNotFound();
  }

  const ownerReturn = checkOwnerOrAdmin(
    authUser,
    ordersAddress?.[0]?.content?.order,
    navigateToNotFound,
  );

  if (React.isValidElement(ownerReturn)) {
    return ownerReturn;
  }

  if (!isSuccessCheckReturn(ownerReturn)) {
    return navigateToNotFound();
  }

  return (
    <section className="w-full  min-h-[calc(100vh-4rem)] flex-col items-center justify-center transition-all px-6 py-10 relative pb-14 max-w-[1000px] mx-auto ">
      <div className="flex w-full items-end justify-center gap-5 my-10">
        <h1 className="font-bold tracking-tighter text-3xl md:text-5xl  text-center">
          {title}
        </h1>
        <span className="md:text-lg">
          {format(
            new Date(ordersAddress[0].content.order.createdAt),
            "dd/MM/yyyy HH:mm",
          )}
        </span>
      </div>
      <div className="grid md:grid-cols-3 gap-12 w-full">
        <div className="md:col-span-2 space-y-4 w-full ">
          {plans.map(
            (
              {
                content: {
                  model: { content: plan },
                },
              },
              i,
            ) => (
              <div
                key={plan.id + "" + i + (plans?.length || -1)}
                className="border rounded-lg px-2 py-4 flex items-center justify-between hover:shadow-md transition-all duration-300 shadow-foreground hover:shadow-foreground/40 hover:scale-[1.02]"
              >
                <Image
                  src={plan.images[0]}
                  width={150}
                  height={150}
                  className="rounded-lg overflow-hidden w-36 h-36 object-cover"
                  alt={plan.title}
                />
                <div className="ml-12 flex-1 space-y-2 flex items-start justify-around">
                  <Link
                    href={`/subscriptions/single/${plan.id}`}
                    className="text-3xl tracking-tighter hover:underline"
                  >
                    {plan.title}
                  </Link>
                  <p className="text-lg tracking-tight font-bold">
                    {formatIntl.number(plan.price, {
                      style: "currency",
                      currency: "EUR",
                      maximumFractionDigits: 2,
                    })}
                  </p>
                </div>
              </div>
            ),
          )}
        </div>
        <div className="col-span-1 md:sticky top-20 right-0 self-start w-full">
          <div className="grid items-start gap-4 md:gap-8 w-full">
            <Card className="p-4 w-full py-4">
              <div className="grid items-start gap-2 w-full">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="font-semibold ">{total}</h3>
                    </div>
                    <div className="flex items-center gap-2">
                      <h3 className="font-semibold text-lg">
                        {formatIntl.number(
                          ordersAddress[0].content.order.total,
                          {
                            style: "currency",
                            currency: "EUR",
                            maximumFractionDigits: 2,
                          },
                        )}
                      </h3>
                    </div>
                  </div>
                  {invoice && (
                    <Button asChild>
                      <motion.a
                        initial={{ scale: 0, opacity: 0 }}
                        animate={{ scale: 1, opacity: 1 }}
                        transition={{ duration: 0.5 }}
                        href={invoice.url}
                        target={"_blank"}
                      >
                        {seeInvoice}
                      </motion.a>
                    </Button>
                  )}
                </div>
              </div>
            </Card>
          </div>
        </div>
      </div>
    </section>
  );
}
