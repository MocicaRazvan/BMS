"use client";
import { Button } from "@/components/ui/button";
import { motion } from "framer-motion";
import React from "react";
interface Props {
  seeInvoice: string;
  invoiceUrl: string;
}
export default function InvoiceLink({ seeInvoice, invoiceUrl }: Props) {
  return (
    <Button asChild>
      <motion.a
        initial={{ scale: 0, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        transition={{ duration: 0.4, ease: "easeOut" }}
        href={invoiceUrl}
        target="_blank"
      >
        {seeInvoice}
      </motion.a>
    </Button>
  );
}
