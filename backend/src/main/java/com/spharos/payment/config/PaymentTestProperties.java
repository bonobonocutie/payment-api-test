package com.spharos.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 테스트 결제용 상품/구매자 정보.
 * 실제 주문 도메인이 생기면 이 설정 대신 주문 서비스 결과로 대체하면 된다.
 */
@ConfigurationProperties(prefix = "payment.test")
public record PaymentTestProperties(
        String orderName,
        Long amount,
        String currency,
        String payMethod,
        Customer customer
) {
    public record Customer(
            String fullName,
            String phoneNumber,
            String email
    ) {
    }
}
