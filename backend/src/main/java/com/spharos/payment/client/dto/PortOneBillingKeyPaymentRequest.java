package com.spharos.payment.client.dto;

/**
 * PortOne 빌링키 결제 API 요청 body.
 * customer.name은 문자열 mid 아니라 { full } 객체여야 한다. (스펙: CustomerNameInput)
 */
public record PortOneBillingKeyPaymentRequest(
        String storeId,
        String billingKey,
        String orderName,
        Customer customer,
        Amount amount,
        String currency
) {
    public record Customer(
            String id,
            Name name,
            String email,
            String phoneNumber
    ) {
    }

    public record Name(String full) {
    }

    public record Amount(Long total) {
    }
}
