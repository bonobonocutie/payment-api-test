"use client";

import { useCallback, useState } from "react";

import { requestPortOnePayment } from "@/services/portOneService";
import type {
  PortOnePaymentOutcome,
  PortOnePaymentRequestParams,
} from "@/types/payment";

interface UsePortOnePaymentResult {
  isRequesting: boolean;
  requestPayment: (
    params: PortOnePaymentRequestParams
  ) => Promise<PortOnePaymentOutcome>;
}

/**
 * PortOne SDK 호출 상태와 실행을 Hook으로 분리한다.
 * 컴포넌트는 SDK 세부 구현을 몰라도 된다.
 */
export function usePortOnePayment(): UsePortOnePaymentResult {
  const [isRequesting, setIsRequesting] = useState(false);

  const requestPayment = useCallback(
    async (params: PortOnePaymentRequestParams): Promise<PortOnePaymentOutcome> => {
      setIsRequesting(true);
      try {
        return await requestPortOnePayment(params);
      } finally {
        setIsRequesting(false);
      }
    },
    []
  );

  return {
    isRequesting,
    requestPayment,
  };
}
