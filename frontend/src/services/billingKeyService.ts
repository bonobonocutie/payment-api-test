import { billingKeyApi } from "@/apis/billingKeyApi";
import type {
  BillingKeyDefaultRequest,
  BillingKeyReadyInfo,
  BillingKeyRegisterRequest,
  RegisteredBillingKey,
} from "@/types/billing";

export const billingKeyService = {
  prepareIssue(signal?: AbortSignal): Promise<BillingKeyReadyInfo> {
    return billingKeyApi.ready(signal);
  },

  listRegisteredMethods(signal?: AbortSignal): Promise<RegisteredBillingKey[]> {
    return billingKeyApi.list(signal);
  },

  registerIssuedKey(
    request: BillingKeyRegisterRequest,
    signal?: AbortSignal
  ): Promise<RegisteredBillingKey> {
    return billingKeyApi.register(request, signal);
  },

  setDefaultMethod(
    request: BillingKeyDefaultRequest,
    signal?: AbortSignal
  ): Promise<RegisteredBillingKey> {
    return billingKeyApi.setDefault(request, signal);
  },
};
