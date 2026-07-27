package com.spharos.payment.service;

import org.springframework.stereotype.Service;

import com.spharos.payment.client.PortOnePaymentClient;
import com.spharos.payment.client.dto.PortOnePaymentApiResponse;
import com.spharos.payment.config.PaymentTestProperties;
import com.spharos.payment.config.PortOneProperties;
import com.spharos.payment.domain.entity.PaymentSession;
import com.spharos.payment.domain.enums.PaymentSessionStatus;
import com.spharos.payment.dto.request.PaymentVerifyRequest;
import com.spharos.payment.dto.response.PaymentCustomerResponse;
import com.spharos.payment.dto.response.PaymentReadyResponse;
import com.spharos.payment.dto.response.PaymentVerifyResponse;
import com.spharos.payment.exception.BusinessException;
import com.spharos.payment.exception.ErrorCode;
import com.spharos.payment.repository.PaymentSessionRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private static final String VERIFY_SUCCESS_MESSAGE = "결제 검증에 성공했습니다.";
    private static final String STORE_ID_PREFIX = "store-";

    private final PaymentSessionRepository paymentSessionRepository;
    private final PortOnePaymentClient portOnePaymentClient;
    private final PaymentVerifier paymentVerifier;
    private final PaymentIdGenerator paymentIdGenerator;
    private final PortOneProperties portOneProperties;
    private final PaymentTestProperties paymentTestProperties;

    public PaymentServiceImpl(
            PaymentSessionRepository paymentSessionRepository,
            PortOnePaymentClient portOnePaymentClient,
            PaymentVerifier paymentVerifier,
            PaymentIdGenerator paymentIdGenerator,
            PortOneProperties portOneProperties,
            PaymentTestProperties paymentTestProperties
    ) {
        this.paymentSessionRepository = paymentSessionRepository;
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

        // storeId/channelKey는 브라우저 SDK에 필요하지만 공개 식별값이므로 ready 응답으로 내려준다.
        return new PaymentReadyResponse(
                paymentId,
                portOneProperties.storeId(),
                portOneProperties.channelKey(),
                session.getOrderName(),
                session.getAmount(),
                session.getCurrency(),
                session.getPayMethod(),
                new PaymentCustomerResponse(
                        paymentTestProperties.customer().fullName(),
                        paymentTestProperties.customer().phoneNumber(),
                        paymentTestProperties.customer().email()
                )
        );
    }

    /**
     * V1 imp 코드(예: iamporttest_3)를 넣으면 결제창이 열리지 않거나 프론트가 LOADING에 멈출 수 있다.
     */
    private void validateStoreId(String storeId) {
        if (storeId == null || !storeId.startsWith(STORE_ID_PREFIX)) {
            throw new BusinessException(ErrorCode.INVALID_STORE_ID);
        }
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
}
