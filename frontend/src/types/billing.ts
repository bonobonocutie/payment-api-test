import type { PaymentCustomerInfo } from "@/types/payment";

export interface BillingKeyReadyInfo {
  storeId: string;
  channelKey: string;
  billingKeyMethod: string;
  customer: PaymentCustomerInfo;
}

export interface RegisteredBillingKey {
  billingKey: string;
  methodType: string;
  displayName: string;
  maskedCardNumber: string;
  defaultMethod: boolean;
  registeredAt: string;
}

export interface BillingKeyRegisterRequest {
  billingKey: string;
}

export interface BillingKeyDefaultRequest {
  billingKey: string;
}

export interface BillingPaymentRequest {
  billingKey: string;
}

export interface PortOneIssueBillingKeyParams {
  storeId: string;
  channelKey: string;
  billingKeyMethod: string;
  customer: PaymentCustomerInfo;
}

export type PortOneIssueBillingKeyOutcome =
  | { type: "success"; billingKey: string }
  | { type: "failure"; message: string; code?: string }
  | { type: "cancelled"; message: string };
