package com.spharos.payment.repository;

import java.util.Optional;

import com.spharos.payment.domain.entity.PaymentSession;

/**
 * 결제 세션 저장소 추상화.
 * 현재는 인메모리 구현을 사용하고, 이후 JPA 구현으로 교체할 수 있다.
 */
public interface PaymentSessionRepository {

    PaymentSession save(PaymentSession paymentSession);

    Optional<PaymentSession> findByPaymentId(String paymentId);
}
