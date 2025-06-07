"use client";
import { useCallback, useEffect, useState } from "react";
import { NEXT_CSRF_HEADER } from "@/types/constants";
import { getCsrfToken } from "next-auth/react";

export interface CsrfToken {
  [NEXT_CSRF_HEADER]: string;
}

export default function useCsrfToken() {
  const [csrfRawToken, setCsrfRawToken] = useState<CsrfToken | undefined>(
    undefined,
  );

  const addTokenConditionally = useCallback(() => {
    return csrfRawToken ?? {};
  }, [JSON.stringify(csrfRawToken)]);

  useEffect(() => {
    getCsrfToken().then((t) => {
      if (t) {
        setCsrfRawToken({ [NEXT_CSRF_HEADER]: t });
      } else {
        setCsrfRawToken(undefined);
        window.location.reload();
      }
    });
  }, []);

  return {
    csrfRawToken,
    addTokenConditionally,
  };
}
