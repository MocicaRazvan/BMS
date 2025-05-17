"use client";
import { motion, useAnimation } from "framer-motion";
import { useRef, useState, useLayoutEffect, ReactNode } from "react";
import { cn } from "@/lib/utils";

export function SwipeToDeleteItem({
  children,
  onDelete,
}: {
  children: ReactNode;
  onDelete: () => void;
}) {
  const controls = useAnimation();
  const [removed, setRemoved] = useState(false);
  const itemRef = useRef<HTMLDivElement>(null);
  const [width, setWidth] = useState(0);

  useLayoutEffect(() => {
    if (itemRef.current) {
      setWidth(itemRef.current.offsetWidth);
    }
  }, []);

  return (
    <div className="relative w-full">
      <div className="absolute inset-0 z-0 bg-destructive rounded-md" />
      <motion.div
        ref={itemRef}
        drag="x"
        dragConstraints={{ left: -width, right: 0 }} // left only
        dragElastic={{ left: 0.2, right: 0 }} // allow slight drag left, but block right
        onDragEnd={(_, info) => {
          if (info.offset.x < -width / 2) {
            controls
              .start({
                x: "-100%",
                opacity: 0,
                transition: { duration: 0.3 },
              })
              .then(() => {
                setRemoved(true);
                onDelete();
              });
          } else {
            controls.start({
              x: 0,
              transition: { type: "easeOut" },
            });
          }
        }}
        animate={controls}
        className={cn(
          "relative p-4 z-10 bg-card border rounded-md shadow-sm mb-2 touch-pan-y select-none overflow-hidden cursor-grab",
          removed && "hidden",
        )}
      >
        {children}
      </motion.div>
    </div>
  );
}
