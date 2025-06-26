"use client";

import { DependencyList, useMemo, useRef } from "react";
import { isDeepEqual } from "@/lib/utils";

const MOD = 2 ** 32;

function useDeepCompareMemoize(dependencies: DependencyList) {
  const dependenciesRef = useRef<DependencyList>(dependencies);
  const signalRef = useRef<number>(0);

  if (!isDeepEqual(dependencies, dependenciesRef.current)) {
    dependenciesRef.current = dependencies;
    signalRef.current = (signalRef.current + 1) % MOD;
  }

  return useMemo(() => dependenciesRef.current, [signalRef.current]);
}
export function useDeepCompareMemo<T>(
  factory: () => T,
  dependencies: DependencyList,
) {
  return useMemo(factory, useDeepCompareMemoize(dependencies));
}
