package com.spharos.payment.dto.response;

public record PaymentCustomerResponse(
        String fullName,
        String phoneNumber,
        String email
) {
}
