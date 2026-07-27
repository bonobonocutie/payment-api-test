package com.spharos.payment.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.spharos.payment.client.dto.PortOnePaymentApiResponse;
import com.spharos.payment.config.PortOneProperties;
import com.spharos.payment.domain.entity.PaymentSession;
import com.spharos.payment.exception.BusinessException;
import com.spharos.payment.exception.ErrorCode;

class PaymentVerifierTest {

    private PaymentVerifier paymentVerifier;

    @BeforeEach
    void setUp() {
        PortOneProperties properties = new PortOneProperties(
                "secret",
                "store-id",
                "channel-key",
                "https://api.portone.io",
                "TEST"
        );
        paymentVerifier = new PaymentVerifier(properties);
    }

    @Test
    @DisplayName("금액/상태/채널이 일치하면 검증에 성공한다")
    void verifySuccess() {
        PaymentSession session = new PaymentSession(
                "payment-1",
                "포트원 테스트 상품",
                1000L,
                "CURRENCY_KRW",
                "CARD"
        );

        PortOnePaymentApiResponse response = new PortOnePaymentApiResponse(
                "payment-1",
                "PAID",
                "포트원 테스트 상품",
                new PortOnePaymentApiResponse.Amount(1000L),
                "KRW",
                new PortOnePaymentApiResponse.Channel("TEST")
        );

        assertDoesNotThrow(() -> paymentVerifier.verify(session, response));
    }

    @Test
    @DisplayName("금액이 다르면 검증에 실패한다")
    void verifyAmountMismatch() {
        PaymentSession session = new PaymentSession(
                "payment-1",
                "포트원 테스트 상품",
                1000L,
                "CURRENCY_KRW",
                "CARD"
        );

        PortOnePaymentApiResponse response = new PortOnePaymentApiResponse(
                "payment-1",
                "PAID",
                "포트원 테스트 상품",
                new PortOnePaymentApiResponse.Amount(2000L),
                "KRW",
                new PortOnePaymentApiResponse.Channel("TEST")
        );

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> paymentVerifier.verify(session, response)
        );

        org.junit.jupiter.api.Assertions.assertEquals(
                ErrorCode.PAYMENT_AMOUNT_MISMATCH,
                exception.getErrorCode()
        );
    }
}
