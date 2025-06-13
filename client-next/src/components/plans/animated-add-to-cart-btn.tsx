"use client";

import { motion } from "framer-motion";
import AddToCartBtn, {
  AddToCartBtnTexts,
} from "@/components/plans/add-to-cart-btn";
import React from "react";
import { WithUser } from "@/lib/user";
import { PlanResponse } from "@/types/dto";

interface Props extends WithUser {
  plan: PlanResponse;
  addToCartBtnTexts: AddToCartBtnTexts;
}

export default function AnimatedAddToCartBtn({
  authUser,
  plan,
  addToCartBtnTexts,
}: Props) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 50 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ type: "spring", stiffness: 100, damping: 10 }}
      className="sticky bottom-0 mt-4  w-fit mx-auto  bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/70 p-2 pt-2.5 rounded-md"
    >
      <AddToCartBtn
        authUser={authUser}
        plan={plan}
        {...addToCartBtnTexts}
        pulse
      />
    </motion.div>
  );
}
