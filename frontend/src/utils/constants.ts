export const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export const PAYMENT_RESULT_STATUS = {
  IDLE: "IDLE",
  LOADING: "LOADING",
  SUCCESS: "SUCCESS",
  FAILURE: "FAILURE",
  CANCELLED: "CANCELLED",
} as const;

export type PaymentResultStatus =
  (typeof PAYMENT_RESULT_STATUS)[keyof typeof PAYMENT_RESULT_STATUS];
