package com.spharos.payment.dto.response;

/**
 * 브라우저 SDK 결제창 호출에 필요한 최소 정보.
 */
public record PaymentReadyResponse(
        String paymentId,
        String storeId,
        String channelKey,
        String orderName,
        Long amount,
        String currency,
        String payMethod,
        PaymentCustomerResponse customer
) {
}
