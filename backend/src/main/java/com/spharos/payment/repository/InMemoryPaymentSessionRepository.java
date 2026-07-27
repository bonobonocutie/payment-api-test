package com.spharos.payment.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.spharos.payment.domain.entity.PaymentSession;

/**
 * 테스트 목적의 인메모리 저장소.
 * DB를 붙이지 않아도 검증 흐름을 완성하고, 운영 전환 시 구현체만 교체하면 된다.
 */
@Repository
public class InMemoryPaymentSessionRepository implements PaymentSessionRepository {

    private final Map<String, PaymentSession> store = new ConcurrentHashMap<>();

    @Override
    public PaymentSession save(PaymentSession paymentSession) {
        store.put(paymentSession.getPaymentId(), paymentSession);
        return paymentSession;
    }

    @Override
    public Optional<PaymentSession> findByPaymentId(String paymentId) {
        return Optional.ofNullable(store.get(paymentId));
    }
}
