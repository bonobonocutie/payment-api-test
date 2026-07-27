package com.spharos.payment.dto.response;

public record PaymentCustomerResponse(
        String id,
        String fullName,
        String phoneNumber,
        String email
) {
}
