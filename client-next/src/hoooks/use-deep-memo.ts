"use client";

import { DependencyList, useMemo, useRef } from "react";
import { isDeepEqual } from "@/lib/utils";

function useDeepCompareMemoize(dependencies: DependencyList) {
  const dependenciesRef = useRef<DependencyList>(dependencies);
  const signalRef = useRef<number>(0);

  if (!isDeepEqual(dependencies, dependenciesRef.current)) {
    dependenciesRef.current = dependencies;
    signalRef.current += 1;
  }

  return useMemo(() => dependenciesRef.current, [signalRef.current]);
}
export function useDeepCompareMemo<T>(
  factory: () => T,
  dependencies: DependencyList,
) {
  return useMemo(factory, useDeepCompareMemoize(dependencies));
}
