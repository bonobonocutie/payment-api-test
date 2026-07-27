import { httpClient } from "@/apis/httpClient";
import type { BillingPaymentRequest } from "@/types/billing";
import type {
  PaymentReadyInfo,
  PaymentVerifyRequest,
  PaymentVerifyResult,
} from "@/types/payment";

const PAYMENT_READY_PATH = "/payment/ready";
const PAYMENT_VERIFY_PATH = "/payment/verify";
const PAYMENT_BILLING_PATH = "/payment/billing";

/**
 * 결제 REST 엔드포인트 호출만 담당한다.
 * 비즈니스 규칙은 service 계층에 둔다.
 */
export const paymentApi = {
  ready(signal?: AbortSignal): Promise<PaymentReadyInfo> {
    return httpClient.get<PaymentReadyInfo>(PAYMENT_READY_PATH, signal);
  },

  verify(request: PaymentVerifyRequest, signal?: AbortSignal): Promise<PaymentVerifyResult> {
    return httpClient.post<PaymentVerifyResult>(PAYMENT_VERIFY_PATH, request, signal);
  },

  payWithBillingKey(
    request: BillingPaymentRequest,
    signal?: AbortSignal
  ): Promise<PaymentVerifyResult> {
    return httpClient.post<PaymentVerifyResult>(PAYMENT_BILLING_PATH, request, signal);
  },
};
