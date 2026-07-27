package com.spharos.payment.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 결제 완료 후 서버 검증 요청.
 * 금액/주문명은 세션에 저장해 두었으므로 paymentId만으로 검증한다.
 */
public record PaymentVerifyRequest(
        @NotBlank(message = "paymentId는 필수입니다.")
        String paymentId
) {
}
