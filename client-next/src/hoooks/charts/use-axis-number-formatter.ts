"use client";
import { useFormatter } from "next-intl";

export default function useAxisNumberFormatter() {
  const formatter = useFormatter();
  return (tick: any) =>
    Number.isInteger(tick) ? formatter.number(tick) : tick;
}
