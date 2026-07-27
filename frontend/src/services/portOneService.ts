import * as PortOne from "@portone/browser-sdk/v2";
import type { Currency, PaymentPayMethod } from "@portone/browser-sdk/v2";

import type {
  PortOnePaymentOutcome,
  PortOnePaymentRequestParams,
} from "@/types/payment";

const USER_CANCEL_CODE = "FAILURE_TYPE_USER_CANCEL";
const STORE_ID_PREFIX = "store-";

function toCurrency(value: string): Currency {
  // 서버에서 CURRENCY_KRW / KRW 모두 올 수 있어 SDK Currency 타입으로 정규화한다.
  return value as Currency;
}

function toPayMethod(value: string): PaymentPayMethod {
  return value as PaymentPayMethod;
}

function validateStoreId(storeId: string): void {
  // V1 imp 코드(예: iamporttest_3)를 넣으면 결제창이 열리지 않거나 Promise가 끝나지 않을 수 있다.
  if (!storeId.startsWith(STORE_ID_PREFIX)) {
    throw new Error(
      `잘못된 Store ID 형식입니다. PortOne V2 콘솔의 store-... 값을 사용하세요. (현재값: ${storeId})`
    );
  }
}

/**
 * PortOne 브라우저 SDK 호출을 캡슐화한다.
 * SDK 버전 변경 시 이 파일만 수정하면 되도록 경계를 만든다.
 */
export async function requestPortOnePayment(
  params: PortOnePaymentRequestParams
): Promise<PortOnePaymentOutcome> {
  validateStoreId(params.storeId);

  try {
    const response = await PortOne.requestPayment({
      storeId: params.storeId,
      channelKey: params.channelKey,
      paymentId: params.paymentId,
      orderName: params.orderName,
      totalAmount: params.totalAmount,
      currency: toCurrency(params.currency),
      payMethod: toPayMethod(params.payMethod),
      // 다수 PG(이니시스/스마트로 등)에서 구매자 연락처를 요구한다.
      customer: {
        fullName: params.customer.fullName,
        phoneNumber: params.customer.phoneNumber,
        email: params.customer.email,
      },
      // windowType을 강제하지 않는다. 토스페이먼츠 등은 PC에서 POPUP을 지원하지 않는다.
      redirectUrl: window.location.href,
    });

    if (!response) {
      return {
        type: "cancelled",
        message: "결제창이 닫혔거나 응답이 없습니다. 팝업 차단 여부를 확인해주세요.",
      };
    }

    if (response.code != null) {
      if (response.code === USER_CANCEL_CODE) {
        return {
          type: "cancelled",
          message: response.message ?? "사용자가 결제를 취소했습니다.",
        };
      }

      return {
        type: "failure",
        code: response.code,
        message: response.message ?? "PortOne 결제 요청에 실패했습니다.",
      };
    }

    return {
      type: "success",
      paymentId: response.paymentId,
    };
  } catch (error) {
    const message =
      error instanceof Error
        ? error.message
        : "PortOne 결제창 호출 중 오류가 발생했습니다.";

    return {
      type: "failure",
      message,
    };
  }
}
