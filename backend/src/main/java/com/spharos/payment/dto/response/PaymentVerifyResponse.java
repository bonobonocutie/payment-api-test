package com.spharos.payment.dto.response;

/**
 * 결제 검증 결과.
 * 성공 여부 확인이 목적이므로 최소 필드만 노출한다.
 */
public record PaymentVerifyResponse(
        String paymentId,
        boolean verified,
        String status,
        String orderName,
        Long amount,
        String currency,
        String message
) {
}
