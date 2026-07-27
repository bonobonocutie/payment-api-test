package com.spharos.payment.domain.enums;

/**
 * 내부 결제 세션 상태.
 * 주문/결제 도메인 확장 시에도 동일한 상태 모델을 재사용할 수 있다.
 */
public enum PaymentSessionStatus {
    READY,
    VERIFIED,
    FAILED
}
