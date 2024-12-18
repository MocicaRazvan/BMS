"use client";

import { WithUser } from "@/lib/user";
import { Link, useRouter } from "@/navigation";
import { useToast } from "@/components/ui/use-toast";
import { useCartForUser } from "@/context/cart-context";
import { Button } from "@/components/ui/button";
import { Trash2 } from "lucide-react";
import { Card } from "@/components/ui/card";
import { ToastAction } from "@/components/ui/toast";
import { useEffect, useState } from "react";
import { useFormatter } from "next-intl";
import { CheckoutSchemaTexts } from "@/types/forms";
import CheckoutDrawer, {
  CheckoutDrawerTexts,
} from "@/components/forms/checkout-drawer";
import { getCheckoutSchemaTexts } from "@/texts/components/forms-server";
import CustomImage from "@/components/common/custom-image";

export interface CartPageContentTexts {
  emptyCart: string;
  seeThePlans: string;
  title: string;
  subtotal: string;
  toastRemovedTitle: string;
  toastRemovedDescription: string;
  toastUndo: string;
  toastRemoveAllTitle: string;
  toastRemoveAllDescription: string;
  clearAll: string;
  checkoutDrawerTexts: CheckoutDrawerTexts;
}

interface Props extends WithUser, CartPageContentTexts {}

export default function CartPageContent({
  authUser,
  emptyCart,
  clearAll,
  toastRemoveAllDescription,
  toastRemoveAllTitle,
  toastRemovedDescription,
  toastRemovedTitle,
  toastUndo,
  title,
  subtotal,
  seeThePlans,
  checkoutDrawerTexts,
}: Props) {
  const [checkoutSchemaTexts, setCheckoutSchemaTexts] =
    useState<CheckoutSchemaTexts | null>(null);

  const router = useRouter();
  const { toast } = useToast();
  const {
    usersCart,
    usersCartTotalPrice,
    removeFromCartForUser,
    clearCartForUser,
    addToCartForUser,
  } = useCartForUser(authUser.id);
  const formatIntl = useFormatter();

  useEffect(() => {
    getCheckoutSchemaTexts(usersCartTotalPrice).then(setCheckoutSchemaTexts);
  }, [usersCartTotalPrice]);

  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  if (!isMounted) return null;

  if (usersCart.total === 0) {
    return (
      <section className="w-full  min-h-[calc(100vh-4rem)] flex-col items-center justify-center transition-all px-6 py-10 pb-14 ">
        <h1 className="text-4xl tracking-tighter font-bold text-center mt-10">
          {emptyCart}
        </h1>
        <div className="w-full mx-auto flex items-center justify-center mt-10">
          <Button
            className="text-lg"
            onClick={() => router.push("/plans/approved")}
          >
            {seeThePlans}
          </Button>
        </div>
      </section>
    );
  }
  return (
    <section className="w-full  min-h-[calc(100vh-4rem)] flex-col items-center justify-center transition-all px-6 py-10 relative pb-14 max-w-[1000px] mx-auto ">
      <h1 className="font-bold tracking-tighter text-3xl md:text-5xl my-10 text-center">
        {title}
      </h1>
      <div className="grid md:grid-cols-3 gap-12 w-full">
        <div className="md:col-span-2 space-y-4 w-full ">
          {usersCart.plans.map((plan) => (
            <div
              key={plan.id}
              className="border rounded-lg px-2 py-4 flex items-center justify-between hover:shadow-md transition-all duration-300 shadow-foreground hover:shadow-foreground/40 hover:scale-[1.02]"
            >
              <CustomImage
                thumblinator
                src={plan.images[0]}
                width={150}
                height={150}
                className="rounded-lg overflow-hidden w-36 h-36 object-cover"
                alt={plan.title}
              />
              <div className="ml-12 flex-1 space-y-2">
                <Link
                  href={`/plans/single/${plan.id}`}
                  className="text-2xl tracking-tighter hover:underline"
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
              <div>
                <Button
                  variant="destructive"
                  onClick={() => {
                    removeFromCartForUser({ id: plan.id });
                    toast({
                      title: plan.title,
                      description: toastRemovedDescription,
                      variant: "destructive",
                      action: (
                        <ToastAction
                          altText={toastUndo}
                          onClick={() => addToCartForUser(plan)}
                        >
                          {toastUndo}
                        </ToastAction>
                      ),
                    });
                  }}
                >
                  <Trash2 />
                </Button>
              </div>
            </div>
          ))}
        </div>
        <div className="col-span-1 md:sticky top-20 right-0 self-start w-full">
          <div className="grid items-start gap-4 md:gap-8 w-full">
            <Card className="p-4 w-full py-4">
              <div className="grid items-start gap-2 w-full">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="flex items-center gap-2">
                      <h3 className="font-semibold ">{subtotal}</h3>
                    </div>
                    <div className="flex items-center gap-2">
                      <h3 className="font-semibold text-lg">
                        {formatIntl.number(usersCartTotalPrice, {
                          style: "currency",
                          currency: "EUR",
                          maximumFractionDigits: 2,
                        })}
                      </h3>
                    </div>
                  </div>
                  <Button
                    variant="destructive"
                    onClick={() => {
                      const old = usersCart.plans;
                      clearCartForUser();
                      toast({
                        title: toastRemoveAllTitle,
                        description: toastRemoveAllDescription,
                        variant: "destructive",
                        action: (
                          <ToastAction
                            altText={toastUndo}
                            onClick={() => old.forEach(addToCartForUser)}
                          >
                            {toastUndo}
                          </ToastAction>
                        ),
                      });
                    }}
                  >
                    <Trash2 /> {clearAll}
                  </Button>
                </div>
                <div className=" mt-6 w-full h-14">
                  {checkoutSchemaTexts && (
                    <CheckoutDrawer
                      {...checkoutDrawerTexts}
                      authUser={authUser}
                      plans={usersCart.plans}
                      totalPrice={usersCartTotalPrice}
                      clearCartForUser={clearCartForUser}
                      checkoutSchemaTexts={checkoutSchemaTexts}
                    />
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
