package com.spharos.payment.service;

import org.springframework.stereotype.Component;

import com.spharos.payment.client.dto.PortOnePaymentApiResponse;
import com.spharos.payment.config.PortOneProperties;
import com.spharos.payment.domain.entity.PaymentSession;
import com.spharos.payment.domain.enums.PortOnePaymentStatus;
import com.spharos.payment.exception.BusinessException;
import com.spharos.payment.exception.ErrorCode;

/**
 * PortOne 조회 결과와 내부 세션 정보의 일치 여부를 검증한다.
 * 검증 규칙을 서비스 본체에서 분리해 단일 책임과 확장성을 확보한다.
 */
@Component
public class PaymentVerifier {

    private static final String CURRENCY_PREFIX = "CURRENCY_";

    private final PortOneProperties portOneProperties;

    public PaymentVerifier(PortOneProperties portOneProperties) {
        this.portOneProperties = portOneProperties;
    }

    public void verify(PaymentSession session, PortOnePaymentApiResponse portOnePayment) {
        validateChannel(portOnePayment);
        validatePaidStatus(portOnePayment);
        validateAmount(session, portOnePayment);
        validateOrderName(session, portOnePayment);
        validateCurrency(session, portOnePayment);
    }

    private void validateChannel(PortOnePaymentApiResponse portOnePayment) {
        String channelType = portOnePayment.channel() == null ? null : portOnePayment.channel().type();
        String allowedChannelType = portOneProperties.allowedChannelType();

        if (channelType == null || !allowedChannelType.equalsIgnoreCase(channelType)) {
            throw new BusinessException(
                    ErrorCode.PAYMENT_CHANNEL_NOT_ALLOWED,
                    "허용 채널=" + allowedChannelType + ", 실제 채널=" + channelType
            );
        }
    }

    private void validatePaidStatus(PortOnePaymentApiResponse portOnePayment) {
        PortOnePaymentStatus status = PortOnePaymentStatus.from(portOnePayment.status());
        if (!status.isPaid()) {
            throw new BusinessException(
                    ErrorCode.PAYMENT_NOT_PAID,
                    "현재 결제 상태=" + portOnePayment.status()
            );
        }
    }

    private void validateAmount(PaymentSession session, PortOnePaymentApiResponse portOnePayment) {
        Long paidAmount = portOnePayment.amount() == null ? null : portOnePayment.amount().total();
        if (paidAmount == null || !session.getAmount().equals(paidAmount)) {
            throw new BusinessException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private void validateOrderName(PaymentSession session, PortOnePaymentApiResponse portOnePayment) {
        if (portOnePayment.orderName() == null || !session.getOrderName().equals(portOnePayment.orderName())) {
            throw new BusinessException(ErrorCode.PAYMENT_ORDER_NAME_MISMATCH);
        }
    }

    private void validateCurrency(PaymentSession session, PortOnePaymentApiResponse portOnePayment) {
        String expected = normalizeCurrency(session.getCurrency());
        String actual = normalizeCurrency(portOnePayment.currency());

        if (!expected.equals(actual)) {
            throw new BusinessException(ErrorCode.PAYMENT_CURRENCY_MISMATCH);
        }
    }

    /**
     * 브라우저 SDK는 CURRENCY_KRW, REST 응답은 KRW 형태일 수 있어 비교 전에 정규화한다.
     */
    private String normalizeCurrency(String currency) {
        if (currency == null) {
            return "";
        }

        String normalized = currency.trim().toUpperCase();
        if (normalized.startsWith(CURRENCY_PREFIX)) {
            return normalized.substring(CURRENCY_PREFIX.length());
        }
        return normalized;
    }
}
