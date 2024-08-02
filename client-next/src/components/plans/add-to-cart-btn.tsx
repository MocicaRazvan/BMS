"use client";

import { WithUser } from "@/lib/user";
import { useCartForUser } from "@/context/cart-context";
import { useToast } from "@/components/ui/use-toast";
import { Button } from "@/components/ui/button";
import { ToastAction } from "@/components/ui/toast";
import { PlusSquareIcon } from "lucide-react";
import { useRouter } from "@/navigation";
import { PlanResponse } from "@/types/dto";
import { useSubscription } from "@/context/subscriptions-context";

export interface AddToCartBtnTexts {
  successDescription: string;
  toastAction: string;
  addToCart: string;
  finishOrder: string;
  alreadyBought: string;
}

interface Props extends AddToCartBtnTexts, WithUser {
  plan: PlanResponse;
}
export default function AddToCartBtn({
  authUser,
  plan,
  addToCart,
  successDescription,
  finishOrder,
  toastAction,
  alreadyBought,
}: Props) {
  const { addToCartForUser, isInCartForUser, removeFromCartForUser } =
    useCartForUser(authUser.id);
  const { isPlanInSubscription } = useSubscription();
  const { toast } = useToast();
  const router = useRouter();

  return (
    <div className="flex items-center justify-center px-2 ">
      {!isPlanInSubscription(plan.id) ? (
        !isInCartForUser({ id: plan.id }) ? (
          <Button
            onClick={() => {
              addToCartForUser(plan);
              toast({
                title: plan.title,
                description: successDescription,

                action: (
                  <ToastAction
                    altText={toastAction}
                    onClick={() => removeFromCartForUser({ id: plan.id })}
                  >
                    {toastAction}
                  </ToastAction>
                ),
              });
            }}
          >
            <PlusSquareIcon className="mr-2" /> {addToCart}
          </Button>
        ) : (
          <Button onClick={() => router.push("/cart")}>{finishOrder}</Button>
        )
      ) : (
        <Button
          variant={"secondary"}
          className="capitalize"
          onClick={() => router.push(`/subscriptions/single/${plan.id}`)}
        >
          {alreadyBought}
        </Button>
      )}
    </div>
  );
}
