package com.spharos.payment.service;

import com.spharos.payment.dto.request.BillingPaymentRequest;
import com.spharos.payment.dto.request.PaymentVerifyRequest;
import com.spharos.payment.dto.response.PaymentReadyResponse;
import com.spharos.payment.dto.response.PaymentVerifyResponse;

public interface PaymentService {

    PaymentReadyResponse ready();

    PaymentVerifyResponse verify(PaymentVerifyRequest request);

    PaymentVerifyResponse payWithBillingKey(BillingPaymentRequest request);
}
