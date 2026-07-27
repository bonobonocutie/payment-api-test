"use client";

import { useCallback, useEffect, useMemo, useState } from "react";

import { requestPortOneIssueBillingKey } from "@/services/portOneService";
import { billingKeyService } from "@/services/billingKeyService";
import { ApiError } from "@/types/api";
import type { RegisteredBillingKey } from "@/types/billing";
import type { PaymentResultViewModel } from "@/types/payment";
import { PAYMENT_RESULT_STATUS } from "@/utils/constants";

interface UseBillingKeysResult {
  methods: RegisteredBillingKey[];
  selectedBillingKey: string | null;
  defaultBillingKey: string | null;
  isLoading: boolean;
  isRegistering: boolean;
  isUpdatingDefault: boolean;
  error: string | null;
  result: PaymentResultViewModel;
  selectBillingKey: (billingKey: string) => void;
  reloadMethods: () => Promise<void>;
  registerPaymentMethod: () => Promise<void>;
  setAsDefault: (billingKey: string) => Promise<void>;
}

const INITIAL_RESULT: PaymentResultViewModel = {
  status: PAYMENT_RESULT_STATUS.IDLE,
  title: "결제수단 대기",
  description: "결제수단을 등록하면 목록에 표시됩니다.",
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

function resolvePreferredBillingKey(
  list: RegisteredBillingKey[],
  current: string | null
): string | null {
  const defaultMethod = list.find((item) => item.defaultMethod);
  if (defaultMethod) {
    return defaultMethod.billingKey;
  }
  if (current && list.some((item) => item.billingKey === current)) {
    return current;
  }
  return list.length > 0 ? list[0].billingKey : null;
}

/**
 * 결제수단 목록/기본수단 관리 + PortOne 빌링키 발급 오케스트레이션.
 */
export function useBillingKeys(): UseBillingKeysResult {
  const [methods, setMethods] = useState<RegisteredBillingKey[]>([]);
  const [selectedBillingKey, setSelectedBillingKey] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isRegistering, setIsRegistering] = useState(false);
  const [isUpdatingDefault, setIsUpdatingDefault] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<PaymentResultViewModel>(INITIAL_RESULT);

  const defaultBillingKey = useMemo(
    () => methods.find((method) => method.defaultMethod)?.billingKey ?? null,
    [methods]
  );

  const reloadMethods = useCallback(async () => {
    setIsLoading(true);
    setError(null);

    try {
      const list = await billingKeyService.listRegisteredMethods();
      setMethods(list);
      setSelectedBillingKey((current) => resolvePreferredBillingKey(list, current));
    } catch (loadError) {
      setMethods([]);
      setSelectedBillingKey(null);
      setError(toErrorMessage(loadError));
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    void reloadMethods();
  }, [reloadMethods]);

  const registerPaymentMethod = useCallback(async () => {
    setIsRegistering(true);
    setResult({
      status: PAYMENT_RESULT_STATUS.LOADING,
      title: "결제수단 등록 중",
      description: "PortOne 결제수단 등록 창을 호출했습니다.",
    });

    try {
      const ready = await billingKeyService.prepareIssue();
      const issueOutcome = await requestPortOneIssueBillingKey({
        storeId: ready.storeId,
        channelKey: ready.channelKey,
        billingKeyMethod: ready.billingKeyMethod,
        customer: ready.customer,
      });

      if (issueOutcome.type === "cancelled") {
        setResult({
          status: PAYMENT_RESULT_STATUS.CANCELLED,
          title: "등록 취소",
          description: issueOutcome.message,
        });
        return;
      }

      if (issueOutcome.type === "failure") {
        setResult({
          status: PAYMENT_RESULT_STATUS.FAILURE,
          title: "등록 실패",
          description: issueOutcome.message,
        });
        return;
      }

      const registered = await billingKeyService.registerIssuedKey({
        billingKey: issueOutcome.billingKey,
      });

      const defaultLabel = registered.defaultMethod ? " (기본 결제수단으로 설정됨)" : "";
      setResult({
        status: PAYMENT_RESULT_STATUS.SUCCESS,
        title: "등록 성공",
        description: `${registered.displayName} (${registered.maskedCardNumber})가 등록되었습니다.${defaultLabel}`,
      });
      await reloadMethods();
      setSelectedBillingKey(registered.billingKey);
    } catch (registerError) {
      setResult({
        status: PAYMENT_RESULT_STATUS.FAILURE,
        title: "등록 실패",
        description: toErrorMessage(registerError),
      });
    } finally {
      setIsRegistering(false);
    }
  }, [reloadMethods]);

  const setAsDefault = useCallback(
    async (billingKey: string) => {
      setIsUpdatingDefault(true);
      try {
        const updated = await billingKeyService.setDefaultMethod({ billingKey });
        setResult({
          status: PAYMENT_RESULT_STATUS.SUCCESS,
          title: "기본 결제수단 변경",
          description: `${updated.displayName} (${updated.maskedCardNumber})를 기본 결제수단으로 설정했습니다.`,
        });
        await reloadMethods();
        setSelectedBillingKey(billingKey);
      } catch (updateError) {
        setResult({
          status: PAYMENT_RESULT_STATUS.FAILURE,
          title: "기본 결제수단 변경 실패",
          description: toErrorMessage(updateError),
        });
      } finally {
        setIsUpdatingDefault(false);
      }
    },
    [reloadMethods]
  );

  return {
    methods,
    selectedBillingKey,
    defaultBillingKey,
    isLoading,
    isRegistering,
    isUpdatingDefault,
    error,
    result,
    selectBillingKey: setSelectedBillingKey,
    reloadMethods,
    registerPaymentMethod,
    setAsDefault,
  };
}
