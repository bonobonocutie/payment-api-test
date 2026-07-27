package com.spharos.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

public record BillingKeyRegisterRequest(
        @NotBlank(message = "billingKey는 필수입니다.")
        String billingKey
) {
}
