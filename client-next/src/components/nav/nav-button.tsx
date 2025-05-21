"use client";
import { cn } from "@/lib/utils";
import { motion } from "framer-motion";

import React, {
  Dispatch,
  ElementType,
  Fragment,
  ReactNode,
  SetStateAction,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";
import { Link, usePathname } from "@/navigation";
import { ClassValue } from "clsx";

export interface BaseNavItem {
  isActive?: (pathName: string) => boolean;
}

export interface NavItem extends BaseNavItem {
  name?: string;
  link: string;
  Icon?: ElementType;
  iconClassName?: ClassValue;
  linkClassName?: ClassValue;
  additional?: ReactNode;
  separator?: ReactNode;
  beforeSeparator?: ReactNode;
}

interface NavItemsProps {
  items: NavItem[];
  className?: string;
  onItemClick?: () => void;
}

export const useNavGroup = <T extends BaseNavItem>(items: T[]) => {
  const pathName = usePathname();
  const [active, setActive] = useState<number | null>(null);

  const activeIndex = useMemo(() => {
    const index = items.findIndex((item) => item.isActive?.(pathName));
    return index !== -1 ? index : null;
  }, [items, pathName]);

  useEffect(() => {
    setActive(activeIndex);
  }, [activeIndex]);

  const setDefaultActive = useCallback(() => {
    setActive(activeIndex);
  }, [activeIndex]);

  return {
    active,
    setActive,
    activeIndex,
    setDefaultActive,
  };
};

export const NavButtonGroup = ({
  items,
  className,
  onItemClick,
}: NavItemsProps) => {
  const { active, setActive, setDefaultActive } = useNavGroup(items);

  return (
    <motion.div
      onMouseLeave={setDefaultActive}
      className={cn("flex flex-row items-center justify-center", className)}
    >
      {items.map((item, idx) => (
        <Fragment key={item.link + idx}>
          {item.beforeSeparator && item.beforeSeparator}
          <NavButton
            item={item}
            idx={idx}
            active={active}
            setActive={setActive}
            onItemClick={onItemClick}
          />
          {item.separator && item.separator}
        </Fragment>
      ))}
    </motion.div>
  );
};

interface NavButtonProps {
  item: NavItem;
  idx: number;
  active: number | null;
  setActive: Dispatch<SetStateAction<number | null>>;
  onItemClick?: () => void;
}

export function NavButton({
  item,
  idx,
  active,
  setActive,
  onItemClick,
}: NavButtonProps) {
  const isActive = active === idx;
  return (
    <Link
      href={item.link}
      onMouseEnter={() => setActive(idx)}
      onClick={onItemClick}
      className={cn(
        "relative px-2 md:px-3 py-1 md:py-1.5 text-foreground",
        item.linkClassName,
      )}
    >
      {isActive && (
        <motion.div
          layoutId="hovered"
          className="absolute inset-0 h-full w-full rounded-full bg-muted"
        />
      )}
      <motion.div
        className="relative z-20 flex items-center gap-2"
        animate={{ scale: isActive ? 1.05 : 1 }}
        transition={{ type: "spring", stiffness: 300, damping: 20 }}
      >
        {item.Icon && (
          <item.Icon
            className={cn("w-5 h-5 text-foreground/80", item.iconClassName)}
          />
        )}
        {item.name && <span className="text-sm md:text-lg">{item.name}</span>}
        {item.additional && item.additional}
      </motion.div>
    </Link>
  );
}
