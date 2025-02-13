"use client";

import { useCartForUser } from "@/context/cart-context";
import { Button } from "../ui/button";
import { ShoppingCartIcon, Trash2 } from "lucide-react";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "../ui/dropdown-menu";
import { Fragment, useEffect, useState } from "react";
import { ScrollArea } from "../ui/scroll-area";
import { cn } from "@/lib/utils";
import { useToast } from "../ui/use-toast";
import { ToastAction } from "../ui/toast";
import { Link, useRouter } from "@/navigation";
import { WithUser } from "@/lib/user";
import { useFormatter } from "next-intl";
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from "@/components/ui/tooltip";

export interface CartPopsTexts {
  undo: string;
  total: string;
  checkout: string;
  toastDescription: string;
  emptyCart: string;
}

interface Props extends WithUser {
  cartPopTexts: CartPopsTexts;
}

export default function CartPop({ authUser, cartPopTexts }: Props) {
  const {
    usersCart,
    removeFromCartForUser,
    usersCartTotalPrice,
    addToCartForUser,
  } = useCartForUser(authUser.id);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const router = useRouter();
  const { toast } = useToast();
  const formatIntl = useFormatter();

  useEffect(() => {
    if (usersCart.total === 0) {
      setDropdownOpen(false);
    }
  }, [usersCart?.total]);

  return (
    <DropdownMenu
      modal={false}
      open={dropdownOpen}
      onOpenChange={setDropdownOpen}
    >
      <DropdownMenuTrigger asChild>
        <div className="relative">
          <TooltipProvider>
            <Tooltip>
              <TooltipTrigger asChild>
                <div>
                  <Button variant="outline" disabled={usersCart.total === 0}>
                    <ShoppingCartIcon />
                  </Button>
                  {usersCart.total > 0 && (
                    <div className="absolute top-[-2px] right-[-10px] rounded-full w-7 h-7 bg-destructive flex items-center justify-center">
                      <p>{usersCart.total}</p>
                    </div>
                  )}
                </div>
              </TooltipTrigger>
              {usersCart.total === 0 && (
                <TooltipContent side="bottom" className="w-52 text-center">
                  <Link
                    href={"/plans/approved"}
                    className="text-center font-semibold hover:underline"
                  >
                    {cartPopTexts?.emptyCart}
                  </Link>
                </TooltipContent>
              )}
            </Tooltip>
          </TooltipProvider>
        </div>
      </DropdownMenuTrigger>
      {usersCart.total > 0 && (
        <DropdownMenuContent className="w-80">
          <DropdownMenuGroup>
            <ScrollArea
              className={cn(
                "w-full px-1",
                usersCart.total < 5
                  ? `h-[calc(${usersCart.total}rem+3.5rem)]`
                  : "h-60",
              )}
            >
              {usersCart.plans.map((plan, index) => (
                <Fragment key={plan.id}>
                  <DropdownMenuItem onClick={(e) => e.preventDefault()}>
                    <div className="py-1 h-10 flex items-center justify-between w-full ">
                      <div>
                        <div className="transition-all hover:underline hover:scale-110">
                          <Link
                            href={`/plans/single/${plan.id}`}
                            className="transition-all hover:underline hover:scale-105"
                          >
                            <p className="text-lg mb-1">
                              {plan.title.length > 10
                                ? plan.title.substring(0, 10) + "..."
                                : plan.title}
                            </p>
                          </Link>
                        </div>
                        <p className="font-bold">
                          {formatIntl.number(plan.price, {
                            style: "currency",
                            currency: "EUR",
                            maximumFractionDigits: 2,
                          })}
                        </p>
                      </div>
                      <Button
                        variant="destructive"
                        className="py-1 px-2"
                        onClick={() => {
                          removeFromCartForUser({ id: plan.id });
                          toast({
                            title: plan.title,
                            description: cartPopTexts?.toastDescription,
                            variant: "destructive",
                            action: (
                              <ToastAction
                                altText="Undo"
                                onClick={() => addToCartForUser(plan)}
                              >
                                {cartPopTexts?.undo}
                              </ToastAction>
                            ),
                          });
                        }}
                      >
                        <Trash2 className="w-4 h-16" />
                      </Button>
                    </div>
                  </DropdownMenuItem>

                  <DropdownMenuSeparator />
                </Fragment>
              ))}
              <div className="h-12 mt-2 px-1 py-2 flex items-center justify-between">
                <p className="font-bold text-center">
                  {cartPopTexts?.total}
                  <span className="ml-1">
                    {formatIntl.number(usersCartTotalPrice, {
                      style: "currency",
                      currency: "EUR",
                      maximumFractionDigits: 2,
                    })}
                  </span>
                </p>
                <Button
                  variant="default"
                  className="py-1 px-2"
                  onClick={() => {
                    router.push("/cart");
                  }}
                >
                  {cartPopTexts?.checkout}
                </Button>
              </div>
            </ScrollArea>
          </DropdownMenuGroup>
        </DropdownMenuContent>
      )}
    </DropdownMenu>
  );
}
