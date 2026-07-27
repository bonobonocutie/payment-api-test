package com.spharos.payment.client;

import com.spharos.payment.client.dto.PortOneBillingKeyApiResponse;
import com.spharos.payment.client.dto.PortOneBillingKeyPaymentRequest;
import com.spharos.payment.client.dto.PortOnePaymentApiResponse;

/**
 * PortOne 외부 결제 API에 대한 포트(인터페이스).
 * 구현체를 교체해도 서비스 계층은 변경되지 않도록 DIP를 적용한다.
 */
public interface PortOnePaymentClient {

    PortOnePaymentApiResponse getPayment(String paymentId);

    PortOneBillingKeyApiResponse getBillingKey(String billingKey);

    /**
     * 빌링키로 결제를 요청한다.
     * 성공 시 결제 단건 조회로 최종 상태를 확인한다.
     */
    void payWithBillingKey(String paymentId, PortOneBillingKeyPaymentRequest request);
}
