package com.spharos.payment.domain.entity;

import java.time.Instant;

/**
 * 등록된 결제수단(빌링키) 엔티티.
 * 현재는 인메모리 저장소에 보관하며, 이후 DB Entity로 교체해도 서비스 계층은 유지할 수 있다.
 */
public class RegisteredBillingKey {

    private final String billingKey;
    private final String customerId;
    private final String methodType;
    private final String cardName;
    private final String maskedCardNumber;
    private final Instant registeredAt;
    private boolean defaultMethod;

    public RegisteredBillingKey(
            String billingKey,
            String customerId,
            String methodType,
            String cardName,
            String maskedCardNumber,
            boolean defaultMethod
    ) {
        this.billingKey = billingKey;
        this.customerId = customerId;
        this.methodType = methodType;
        this.cardName = cardName;
        this.maskedCardNumber = maskedCardNumber;
        this.registeredAt = Instant.now();
        this.defaultMethod = defaultMethod;
    }

    public void markAsDefault() {
        this.defaultMethod = true;
    }

    public void clearDefault() {
        this.defaultMethod = false;
    }

    public String getBillingKey() {
        return billingKey;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getMethodType() {
        return methodType;
    }

    public String getCardName() {
        return cardName;
    }

    public String getMaskedCardNumber() {
        return maskedCardNumber;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public boolean isDefaultMethod() {
        return defaultMethod;
    }
}
