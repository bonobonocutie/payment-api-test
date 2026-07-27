import type { PaymentResultStatus } from "@/utils/constants";

export interface PaymentCustomerInfo {
  id?: string;
  fullName: string;
  phoneNumber: string;
  email: string;
}

export interface PaymentReadyInfo {
  paymentId: string;
  storeId: string;
  channelKey: string;
  orderName: string;
  amount: number;
  currency: string;
  payMethod: string;
  customer: PaymentCustomerInfo;
}

export interface PaymentVerifyRequest {
  paymentId: string;
}

export interface PaymentVerifyResult {
  paymentId: string;
  verified: boolean;
  status: string;
  orderName: string;
  amount: number;
  currency: string;
  message: string;
}

export interface PaymentResultViewModel {
  status: PaymentResultStatus;
  title: string;
  description: string;
  paymentId?: string;
}

/**
 * PortOne 브라우저 SDK 결제 요청에 필요한 최소 파라미터.
 * SDK 타입에 직접 의존하지 않도록 우리 도메인 타입으로 감싼다.
 */
export interface PortOnePaymentRequestParams {
  storeId: string;
  channelKey: string;
  paymentId: string;
  orderName: string;
  totalAmount: number;
  currency: string;
  payMethod: string;
  customer: PaymentCustomerInfo;
}

export type PortOnePaymentOutcome =
  | { type: "success"; paymentId: string }
  | { type: "failure"; message: string; code?: string }
  | { type: "cancelled"; message: string };
