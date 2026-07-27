package com.spharos.payment.dto.response;

import java.time.Instant;

/**
 * 프론트 목록에 노출할 마스킹된 결제수단 정보.
 * 원본 빌링키는 결제 요청에만 사용한다.
 */
public record BillingKeyResponse(
        String billingKey,
        String methodType,
        String displayName,
        String maskedCardNumber,
        boolean defaultMethod,
        Instant registeredAt
) {
}
