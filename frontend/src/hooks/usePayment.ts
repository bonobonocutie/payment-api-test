"use client";

import { useCallback, useEffect, useState } from "react";

import { usePortOnePayment } from "@/hooks/usePortOnePayment";
import { paymentService } from "@/services/paymentService";
import { ApiError } from "@/types/api";
import type { PaymentReadyInfo, PaymentResultViewModel } from "@/types/payment";
import { PAYMENT_RESULT_STATUS } from "@/utils/constants";

interface UsePaymentResult {
  readyInfo: PaymentReadyInfo | null;
  isReadyLoading: boolean;
  isPaying: boolean;
  readyError: string | null;
  result: PaymentResultViewModel;
  startTestPayment: () => Promise<void>;
  reloadReadyInfo: () => Promise<void>;
}

const INITIAL_RESULT: PaymentResultViewModel = {
  status: PAYMENT_RESULT_STATUS.IDLE,
  title: "결제 대기",
  description: "테스트 결제를 시작하면 결과가 여기에 표시됩니다.",
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
 * 결제 준비 → PortOne 결제창 → 서버 검증 흐름을 오케스트레이션한다.
 * JSX에는 상태와 핸들러만 내려보내 비즈니스 로직을 UI에서 분리한다.
 */
export function usePayment(): UsePaymentResult {
  const { isRequesting, requestPayment } = usePortOnePayment();
  const [readyInfo, setReadyInfo] = useState<PaymentReadyInfo | null>(null);
  const [isReadyLoading, setIsReadyLoading] = useState(true);
  const [isVerifying, setIsVerifying] = useState(false);
  const [readyError, setReadyError] = useState<string | null>(null);
  const [result, setResult] = useState<PaymentResultViewModel>(INITIAL_RESULT);

  const reloadReadyInfo = useCallback(async () => {
    setIsReadyLoading(true);
    setReadyError(null);

    try {
      const data = await paymentService.prepareTestPayment();
      setReadyInfo(data);
    } catch (error) {
      setReadyInfo(null);
      setReadyError(toErrorMessage(error));
    } finally {
      setIsReadyLoading(false);
    }
  }, []);

  useEffect(() => {
    void reloadReadyInfo();
  }, [reloadReadyInfo]);

  const startTestPayment = useCallback(async () => {
    if (!readyInfo) {
      setResult({
        status: PAYMENT_RESULT_STATUS.FAILURE,
        title: "결제 준비 실패",
        description: readyError ?? "결제 준비 정보가 없습니다.",
      });
      return;
    }

    setResult({
      status: PAYMENT_RESULT_STATUS.LOADING,
      title: "결제 진행 중",
      description: "PortOne 결제창을 호출했습니다. 결제창에서 결제를 완료해주세요.",
      paymentId: readyInfo.paymentId,
    });

    try {
      const portOneOutcome = await requestPayment({
        storeId: readyInfo.storeId,
        channelKey: readyInfo.channelKey,
        paymentId: readyInfo.paymentId,
        orderName: readyInfo.orderName,
        totalAmount: readyInfo.amount,
        currency: readyInfo.currency,
        payMethod: readyInfo.payMethod,
        customer: readyInfo.customer,
      });

      if (portOneOutcome.type === "cancelled") {
        setResult({
          status: PAYMENT_RESULT_STATUS.CANCELLED,
          title: "결제 취소",
          description: portOneOutcome.message,
          paymentId: readyInfo.paymentId,
        });
        await reloadReadyInfo();
        return;
      }

      if (portOneOutcome.type === "failure") {
        setResult({
          status: PAYMENT_RESULT_STATUS.FAILURE,
          title: "결제 실패",
          description: portOneOutcome.message,
          paymentId: readyInfo.paymentId,
        });
        await reloadReadyInfo();
        return;
      }

      setIsVerifying(true);
      setResult({
        status: PAYMENT_RESULT_STATUS.LOADING,
        title: "결제 검증 중",
        description: "서버에서 PortOne 결제 결과를 검증하고 있습니다.",
        paymentId: portOneOutcome.paymentId,
      });

      try {
        const verifyResult = await paymentService.verifyPayment({
          paymentId: portOneOutcome.paymentId,
        });

        setResult({
          status: PAYMENT_RESULT_STATUS.SUCCESS,
          title: "결제 성공",
          description: verifyResult.message,
          paymentId: verifyResult.paymentId,
        });
      } catch (error) {
        setResult({
          status: PAYMENT_RESULT_STATUS.FAILURE,
          title: "결제 검증 실패",
          description: toErrorMessage(error),
          paymentId: portOneOutcome.paymentId,
        });
      } finally {
        setIsVerifying(false);
        await reloadReadyInfo();
      }
    } catch (error) {
      // requestPayment 중 예외가 나도 LOADING에 고착되지 않도록 처리한다.
      setResult({
        status: PAYMENT_RESULT_STATUS.FAILURE,
        title: "결제 실패",
        description: toErrorMessage(error),
        paymentId: readyInfo.paymentId,
      });
      await reloadReadyInfo();
    }
  }, [readyError, readyInfo, reloadReadyInfo, requestPayment]);

  return {
    readyInfo,
    isReadyLoading,
    isPaying: isRequesting || isVerifying,
    readyError,
    result,
    startTestPayment,
    reloadReadyInfo,
  };
}
