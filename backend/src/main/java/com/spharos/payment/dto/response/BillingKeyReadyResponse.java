package com.spharos.payment.dto.response;

/**
 * 브라우저 SDK 빌링키 발급에 필요한 정보.
 */
public record BillingKeyReadyResponse(
        String storeId,
        String channelKey,
        String billingKeyMethod,
        PaymentCustomerResponse customer
) {
}
