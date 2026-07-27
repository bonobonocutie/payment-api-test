import { paymentApi } from "@/apis/paymentApi";
import type {
  PaymentReadyInfo,
  PaymentVerifyRequest,
  PaymentVerifyResult,
} from "@/types/payment";

/**
 * 결제 도메인 유스케이스를 표현하는 서비스 계층.
 * UI는 이 계층만 호출하고, 실제 HTTP는 api 계층에 위임한다.
 */
export const paymentService = {
  prepareTestPayment(signal?: AbortSignal): Promise<PaymentReadyInfo> {
    return paymentApi.ready(signal);
  },

  verifyPayment(
    request: PaymentVerifyRequest,
    signal?: AbortSignal
  ): Promise<PaymentVerifyResult> {
    return paymentApi.verify(request, signal);
  },
};
