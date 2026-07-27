package com.spharos.payment.domain.enums;

import java.util.Arrays;

/**
 * PortOne 결제 상태 중 검증에 필요한 값만 정의한다.
 */
public enum PortOnePaymentStatus {
    PAID,
    READY,
    FAILED,
    CANCELLED,
    PARTIAL_CANCELLED,
    PAY_PENDING,
    VIRTUAL_ACCOUNT_ISSUED,
    UNKNOWN;

    public static PortOnePaymentStatus from(String value) {
        if (value == null || value.isBlank()) {
            return UNKNOWN;
        }

        return Arrays.stream(values())
                .filter(status -> status.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(UNKNOWN);
    }

    public boolean isPaid() {
        return this == PAID;
    }
}
