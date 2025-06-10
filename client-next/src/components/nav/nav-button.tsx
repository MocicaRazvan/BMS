"use client";
import { cn } from "@/lib/utils";

import React, {
  CSSProperties,
  Dispatch,
  ElementType,
  Fragment,
  ReactNode,
  SetStateAction,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { Link, usePathname } from "@/navigation/navigation";
import { ClassValue } from "clsx";
import { v4 as uuidv4 } from "uuid";

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
  const containerRef = useRef<HTMLDivElement>(null);
  const [highlightStyle, setHighlightStyle] = useState<CSSProperties>({});
  const { active, setActive, setDefaultActive } = useNavGroup(items);
  const uniqueId = useMemo(() => uuidv4(), []);

  const createItemKey = useCallback(
    (idx: number) => `nav-button-${idx}-${uniqueId}`,
    [uniqueId],
  );

  useEffect(() => {
    if (active !== null) {
      const btn = document.getElementById(createItemKey(active));
      const cont = containerRef.current;
      if (btn instanceof HTMLElement && cont) {
        setHighlightStyle({
          left: btn.offsetLeft,
          top: btn.offsetTop,
          width: btn.offsetWidth,
          height: btn.offsetHeight,
        });
      }
    }
  }, [active, createItemKey]);
  return (
    <div
      ref={containerRef}
      onMouseLeave={setDefaultActive}
      className={cn(
        "relative flex flex-row items-center justify-center",
        className,
      )}
    >
      <div
        className="absolute rounded-full bg-primary/10 dark:bg-muted/90 transition-all spring-bounce-30 spring-duration-300 backdrop-blur-3xl
         supports-[backdrop-filter]:bg-primary/5 dark:supports-[backdrop-filter]:bg-muted/50"
        style={highlightStyle}
      />

      {items.map((item, idx) => (
        <Fragment key={item.link + idx + uniqueId}>
          {item.beforeSeparator && item.beforeSeparator}
          <NavButton
            id={createItemKey(idx)}
            item={item}
            idx={idx}
            setActive={setActive}
            onItemClick={onItemClick}
          />
          {item.separator && item.separator}
        </Fragment>
      ))}
    </div>
  );
};

interface NavButtonProps {
  item: NavItem;
  idx: number;
  setActive: Dispatch<SetStateAction<number | null>>;
  onItemClick?: () => void;
  id: string;
}

export function NavButton({
  id,
  item,
  idx,
  setActive,
  onItemClick,
}: NavButtonProps) {
  return (
    <Link
      id={id}
      href={item.link}
      onMouseEnter={() => setActive(idx)}
      onClick={onItemClick}
      className={cn(
        "relative px-2 md:px-3 py-1 md:py-1.5 text-foreground inline-block hover:scale-105 transition-transform duration-200 ease-in-out",
        item.linkClassName,
      )}
    >
      <div className="relative z-20 flex items-center gap-2">
        {item.Icon && (
          <item.Icon
            className={cn("w-5 h-5 text-foreground/80", item.iconClassName)}
          />
        )}
        {item.name && <span className="md:text-lg">{item.name}</span>}
        {item.additional && item.additional}
      </div>
    </Link>
  );
}
