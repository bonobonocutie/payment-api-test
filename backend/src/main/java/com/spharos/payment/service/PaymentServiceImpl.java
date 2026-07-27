package com.spharos.payment.service;

import org.springframework.stereotype.Service;

import com.spharos.payment.client.PortOnePaymentClient;
import com.spharos.payment.client.dto.PortOneBillingKeyPaymentRequest;
import com.spharos.payment.client.dto.PortOnePaymentApiResponse;
import com.spharos.payment.config.PaymentTestProperties;
import com.spharos.payment.config.PortOneProperties;
import com.spharos.payment.domain.entity.PaymentSession;
import com.spharos.payment.domain.entity.RegisteredBillingKey;
import com.spharos.payment.domain.enums.PaymentSessionStatus;
import com.spharos.payment.dto.request.BillingPaymentRequest;
import com.spharos.payment.dto.request.PaymentVerifyRequest;
import com.spharos.payment.dto.response.PaymentCustomerResponse;
import com.spharos.payment.dto.response.PaymentReadyResponse;
import com.spharos.payment.dto.response.PaymentVerifyResponse;
import com.spharos.payment.exception.BusinessException;
import com.spharos.payment.exception.ErrorCode;
import com.spharos.payment.repository.BillingKeyRepository;
import com.spharos.payment.repository.PaymentSessionRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String VERIFY_SUCCESS_MESSAGE = "결제 검증에 성공했습니다.";
    private static final String BILLING_PAY_SUCCESS_MESSAGE = "등록된 결제수단으로 결제에 성공했습니다.";
    private static final String STORE_ID_PREFIX = "store-";
    private static final String CURRENCY_PREFIX = "CURRENCY_";

    private final PaymentSessionRepository paymentSessionRepository;
    private final BillingKeyRepository billingKeyRepository;
    private final PortOnePaymentClient portOnePaymentClient;
    private final PaymentVerifier paymentVerifier;
    private final PaymentIdGenerator paymentIdGenerator;
    private final PortOneProperties portOneProperties;
    private final PaymentTestProperties paymentTestProperties;

    public PaymentServiceImpl(
            PaymentSessionRepository paymentSessionRepository,
            BillingKeyRepository billingKeyRepository,
            PortOnePaymentClient portOnePaymentClient,
            PaymentVerifier paymentVerifier,
            PaymentIdGenerator paymentIdGenerator,
            PortOneProperties portOneProperties,
            PaymentTestProperties paymentTestProperties
    ) {
        this.paymentSessionRepository = paymentSessionRepository;
        this.billingKeyRepository = billingKeyRepository;
        this.portOnePaymentClient = portOnePaymentClient;
        this.paymentVerifier = paymentVerifier;
        this.paymentIdGenerator = paymentIdGenerator;
        this.portOneProperties = portOneProperties;
        this.paymentTestProperties = paymentTestProperties;
    }

    @Override
    public PaymentReadyResponse ready() {
        validateStoreId(portOneProperties.storeId());

        String paymentId = paymentIdGenerator.generate();

        PaymentSession session = new PaymentSession(
                paymentId,
                paymentTestProperties.orderName(),
                paymentTestProperties.amount(),
                paymentTestProperties.currency(),
                paymentTestProperties.payMethod()
        );
        paymentSessionRepository.save(session);

        return new PaymentReadyResponse(
                paymentId,
                portOneProperties.storeId(),
                portOneProperties.channelKey(),
                session.getOrderName(),
                session.getAmount(),
                session.getCurrency(),
                session.getPayMethod(),
                toCustomerResponse()
        );
    }

    @Override
    public PaymentVerifyResponse verify(PaymentVerifyRequest request) {
        PaymentSession session = paymentSessionRepository.findByPaymentId(request.paymentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_SESSION_NOT_FOUND));

        if (session.getStatus() == PaymentSessionStatus.VERIFIED) {
            throw new BusinessException(ErrorCode.PAYMENT_ALREADY_VERIFIED);
        }

        PortOnePaymentApiResponse portOnePayment = portOnePaymentClient.getPayment(request.paymentId());

        try {
            paymentVerifier.verify(session, portOnePayment);
            session.markVerified(portOnePayment.status());
            paymentSessionRepository.save(session);

            log.info("Payment verified. paymentId={}, amount={}", session.getPaymentId(), session.getAmount());

            return new PaymentVerifyResponse(
                    session.getPaymentId(),
                    true,
                    portOnePayment.status(),
                    session.getOrderName(),
                    session.getAmount(),
                    session.getCurrency(),
                    VERIFY_SUCCESS_MESSAGE
            );
        } catch (BusinessException exception) {
            session.markFailed(portOnePayment.status());
            paymentSessionRepository.save(session);
            throw exception;
        }
    }

    @Override
    public PaymentVerifyResponse payWithBillingKey(BillingPaymentRequest request) {
        RegisteredBillingKey registeredBillingKey = billingKeyRepository.findByBillingKey(request.billingKey())
                .orElseThrow(() -> new BusinessException(ErrorCode.BILLING_KEY_NOT_FOUND));

        String paymentId = paymentIdGenerator.generate();
        PaymentSession session = new PaymentSession(
                paymentId,
                paymentTestProperties.orderName(),
                paymentTestProperties.amount(),
                paymentTestProperties.currency(),
                paymentTestProperties.payMethod()
        );
        paymentSessionRepository.save(session);

        PaymentTestProperties.Customer customer = paymentTestProperties.customer();
        PortOneBillingKeyPaymentRequest portOneRequest = new PortOneBillingKeyPaymentRequest(
                portOneProperties.storeId(),
                registeredBillingKey.getBillingKey(),
                session.getOrderName(),
                new PortOneBillingKeyPaymentRequest.Customer(
                        customer.id(),
                        new PortOneBillingKeyPaymentRequest.Name(customer.fullName()),
                        customer.email(),
                        customer.phoneNumber()
                ),
                new PortOneBillingKeyPaymentRequest.Amount(session.getAmount()),
                normalizeCurrencyForApi(session.getCurrency())
        );

        portOnePaymentClient.payWithBillingKey(paymentId, portOneRequest);

        PortOnePaymentApiResponse portOnePayment = portOnePaymentClient.getPayment(paymentId);
        try {
            paymentVerifier.verify(session, portOnePayment);
            session.markVerified(portOnePayment.status());
            paymentSessionRepository.save(session);

            log.info(
                    "Billing key payment verified. paymentId={}, amount={}",
                    session.getPaymentId(),
                    session.getAmount()
            );

            return new PaymentVerifyResponse(
                    session.getPaymentId(),
                    true,
                    portOnePayment.status(),
                    session.getOrderName(),
                    session.getAmount(),
                    session.getCurrency(),
                    BILLING_PAY_SUCCESS_MESSAGE
            );
        } catch (BusinessException exception) {
            session.markFailed(portOnePayment.status());
            paymentSessionRepository.save(session);
            throw exception;
        }
    }

    private void validateStoreId(String storeId) {
        if (storeId == null || !storeId.startsWith(STORE_ID_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_STORE_ID);
        }
    }

    private PaymentCustomerResponse toCustomerResponse() {
        PaymentTestProperties.Customer customer = paymentTestProperties.customer();
        return new PaymentCustomerResponse(
                customer.id(),
                customer.fullName(),
                customer.phoneNumber(),
                customer.email()
        );
    }

    /**
     * REST 빌링키 결제는 KRW 형태를 기대하므로 CURRENCY_ 접두사를 제거한다.
     */
    private String normalizeCurrencyForApi(String currency) {
        if (currency == null) {
            return "KRW";
        }
        String normalized = currency.trim().toUpperCase();
        if (normalized.startsWith(CURRENCY_PREFIX)) {
            return normalized.substring(CURRENCY_PREFIX.length());
        }
        return normalized;
    }
}
