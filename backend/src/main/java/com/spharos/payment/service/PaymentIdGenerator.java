package com.spharos.payment.service;

import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * paymentId 생성 책임을 분리한다.
 * PG사별로 허용 문자/길이 제약이 달라질 수 있어 교체 가능하게 둔다.
 */
@Component
public class PaymentIdGenerator {

    private static final String PAYMENT_ID_PREFIX = "payment-";

    public String generate() {
        return PAYMENT_ID_PREFIX + UUID.randomUUID();
    }
}
