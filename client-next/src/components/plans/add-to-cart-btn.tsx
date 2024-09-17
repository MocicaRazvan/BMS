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
import PulsatingButton from "@/components/magicui/pulsating-button";

export interface AddToCartBtnTexts {
  successDescription: string;
  toastAction: string;
  addToCart: string;
  finishOrder: string;
  alreadyBought: string;
}

interface Props extends AddToCartBtnTexts, WithUser {
  plan: PlanResponse;
  pulse?: boolean;
}
export default function AddToCartBtn({
  authUser,
  plan,
  addToCart,
  successDescription,
  finishOrder,
  toastAction,
  alreadyBought,
  pulse = false,
}: Props) {
  const { addToCartForUser, isInCartForUser, removeFromCartForUser } =
    useCartForUser(authUser.id);
  const { isPlanInSubscription } = useSubscription();
  const { toast } = useToast();
  const router = useRouter();

  const BtnComponent = pulse ? PulsatingButton : Button;

  return (
    <div className="flex items-center justify-center px-2 ">
      {!isPlanInSubscription(plan.id) ? (
        !isInCartForUser({ id: plan.id }) ? (
          // <ShineBorder
          //   className="p-5 m-0 bg-transparent min-h-fit min-w-fit"
          //   color={["#A07CFE", "#FE8FB5", "#FFBE7B"]}
          //   borderWidth={4}
          // >
          <BtnComponent
            size="lg"
            className="inline-flex"
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
            <div className="flex-1 flex items-center justify-center ">
              <PlusSquareIcon className="mr-2" /> {addToCart}
            </div>
          </BtnComponent>
        ) : (
          // </ShineBorder>
          <PulsatingButton onClick={() => router.push("/cart")}>
            {finishOrder}
          </PulsatingButton>
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
