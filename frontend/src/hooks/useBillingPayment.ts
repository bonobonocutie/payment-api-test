"use client";

import { useCallback, useState } from "react";

import { paymentService } from "@/services/paymentService";
import { ApiError } from "@/types/api";
import type { PaymentResultViewModel } from "@/types/payment";
import { PAYMENT_RESULT_STATUS } from "@/utils/constants";

interface UseBillingPaymentResult {
  isPaying: boolean;
  result: PaymentResultViewModel;
  payWithSelectedMethod: (billingKey: string | null) => Promise<void>;
}

const INITIAL_RESULT: PaymentResultViewModel = {
  status: PAYMENT_RESULT_STATUS.IDLE,
  title: "결제 대기",
  description: "등록된 결제수단을 선택한 뒤 결제하세요.",
};

function toErrorMessage(error: unknown): string {
  if (error instanceof ApiError) {
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return "알 수 없는 오류가 발생했습니다.";
}

/**
 * 등록된 빌링키로 서버 결제를 수행한다. (결제창 없음)
 */
export function useBillingPayment(): UseBillingPaymentResult {
  const [isPaying, setIsPaying] = useState(false);
  const [result, setResult] = useState<PaymentResultViewModel>(INITIAL_RESULT);

  const payWithSelectedMethod = useCallback(async (billingKey: string | null) => {
    if (!billingKey) {
      setResult({
        status: PAYMENT_RESULT_STATUS.FAILURE,
        title: "결제 실패",
        description: "결제할 수단을 선택해주세요.",
      });
      return;
    }

    setIsPaying(true);
    setResult({
      status: PAYMENT_RESULT_STATUS.LOADING,
      title: "결제 진행 중",
      description: "등록된 결제수단으로 서버 결제를 요청하고 있습니다.",
    });

    try {
      const verifyResult = await paymentService.payWithRegisteredMethod({ billingKey });
      setResult({
        status: PAYMENT_RESULT_STATUS.SUCCESS,
        title: "결제 성공",
        description: verifyResult.message,
        paymentId: verifyResult.paymentId,
      });
    } catch (error) {
      setResult({
        status: PAYMENT_RESULT_STATUS.FAILURE,
        title: "결제 실패",
        description: toErrorMessage(error),
      });
    } finally {
      setIsPaying(false);
    }
  }, []);

  return {
    isPaying,
    result,
    payWithSelectedMethod,
  };
}
