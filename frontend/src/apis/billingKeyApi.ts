import { httpClient } from "@/apis/httpClient";
import type {
  BillingKeyDefaultRequest,
  BillingKeyReadyInfo,
  BillingKeyRegisterRequest,
  RegisteredBillingKey,
} from "@/types/billing";

const BILLING_KEYS_PATH = "/billing-keys";
const BILLING_KEYS_READY_PATH = "/billing-keys/ready";
const BILLING_KEYS_DEFAULT_PATH = "/billing-keys/default";

export const billingKeyApi = {
  ready(signal?: AbortSignal): Promise<BillingKeyReadyInfo> {
    return httpClient.get<BillingKeyReadyInfo>(BILLING_KEYS_READY_PATH, signal);
  },

  list(signal?: AbortSignal): Promise<RegisteredBillingKey[]> {
    return httpClient.get<RegisteredBillingKey[]>(BILLING_KEYS_PATH, signal);
  },

  register(
    request: BillingKeyRegisterRequest,
    signal?: AbortSignal
  ): Promise<RegisteredBillingKey> {
    return httpClient.post<RegisteredBillingKey>(BILLING_KEYS_PATH, request, signal);
  },

  getDefault(signal?: AbortSignal): Promise<RegisteredBillingKey> {
    return httpClient.get<RegisteredBillingKey>(BILLING_KEYS_DEFAULT_PATH, signal);
  },

  setDefault(
    request: BillingKeyDefaultRequest,
    signal?: AbortSignal
  ): Promise<RegisteredBillingKey> {
    return httpClient.post<RegisteredBillingKey>(BILLING_KEYS_DEFAULT_PATH, request, signal);
  },
};
