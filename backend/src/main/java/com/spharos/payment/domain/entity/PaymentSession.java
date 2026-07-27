package com.spharos.payment.domain.entity;

import java.time.Instant;

import com.spharos.payment.domain.enums.PaymentSessionStatus;

/**
 * 결제 준비 시점에 생성되는 세션 엔티티.
 * 현재는 메모리 저장소를 사용하지만, 이후 JPA Entity로 교체해도 서비스 계층은 그대로 유지할 수 있다.
 */
public class PaymentSession {

    private final String paymentId;
    private final String orderName;
    private final Long amount;
    private final String currency;
    private final String payMethod;
    private PaymentSessionStatus status;
    private String portOneStatus;
    private final Instant createdAt;
    private Instant verifiedAt;

    public PaymentSession(
            String paymentId,
            String orderName,
            Long amount,
            String currency,
            String payMethod
    ) {
        this.paymentId = paymentId;
        this.orderName = orderName;
        this.amount = amount;
        this.currency = currency;
        this.payMethod = payMethod;
        this.status = PaymentSessionStatus.READY;
        this.createdAt = Instant.now();
    }

    public void markVerified(String portOneStatus) {
        this.status = PaymentSessionStatus.VERIFIED;
        this.portOneStatus = portOneStatus;
        this.verifiedAt = Instant.now();
    }

    public void markFailed(String portOneStatus) {
        this.status = PaymentSessionStatus.FAILED;
        this.portOneStatus = portOneStatus;
        this.verifiedAt = Instant.now();
    }

    public String getPaymentId() {
        return paymentId;
    }

    public String getOrderName() {
        return orderName;
    }

    public Long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPayMethod() {
        return payMethod;
    }

    public PaymentSessionStatus getStatus() {
        return status;
    }

    public String getPortOneStatus() {
        return portOneStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }
}
