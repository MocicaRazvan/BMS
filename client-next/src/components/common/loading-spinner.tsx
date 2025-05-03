"use client";
import Loader from "@/components/ui/spinner";
import { cn } from "@/lib/utils";
import { motion } from "framer-motion";

interface LoadingSpinnerProps {
  sectionClassName?: string;
  loaderClassName?: string;
}

export default function LoadingSpinner({
  sectionClassName,
  loaderClassName,
}: LoadingSpinnerProps) {
  return (
    <motion.section
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ delay: 1 }}
      className="w-full flex items-center justify-center"
    >
      <div
        className={cn(
          "w-full min-h-[calc(100vh-12rem)] flex items-center justify-center transition-all overflow-hidden z-[1]",
          sectionClassName,
        )}
      >
        <Loader className={cn("w-full", loaderClassName)} />
      </div>
    </motion.section>
  );
}
