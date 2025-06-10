"use client";
import {
  // eslint-disable-next-line no-restricted-imports
  useRouter as useIntlRouter,
  usePathname,
} from "@/navigation/navigation";
import { useCallback, useEffect } from "react";
import NProgress from "nprogress";

type IntlRouter = typeof useIntlRouter;
type ReturnIntlRouter = ReturnType<IntlRouter>;
type ReplaceFn = ReturnIntlRouter["replace"];
type PushFn = ReturnIntlRouter["push"];

export const useRouter: IntlRouter = () => {
  const router = useIntlRouter();
  const pathname = usePathname();

  useEffect(() => {
    NProgress.done();
  }, [pathname]);

  const replace: ReplaceFn = useCallback(
    (href, options) => {
      href !== pathname && NProgress.start();
      router.replace(href, options);
    },
    [router, pathname],
  );

  const push: PushFn = useCallback(
    (href, options) => {
      href !== pathname && NProgress.start();
      router.push(href, options);
    },
    [router, pathname],
  );

  return {
    ...router,
    replace,
    push,
  };
};
