package com.spharos.payment.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.spharos.payment.client.PortOnePaymentClient;
import com.spharos.payment.client.dto.PortOneBillingKeyApiResponse;
import com.spharos.payment.config.PaymentTestProperties;
import com.spharos.payment.config.PortOneProperties;
import com.spharos.payment.domain.entity.RegisteredBillingKey;
import com.spharos.payment.dto.request.BillingKeyDefaultRequest;
import com.spharos.payment.dto.request.BillingKeyRegisterRequest;
import com.spharos.payment.dto.response.BillingKeyReadyResponse;
import com.spharos.payment.dto.response.BillingKeyResponse;
import com.spharos.payment.dto.response.PaymentCustomerResponse;
import com.spharos.payment.exception.BusinessException;
import com.spharos.payment.exception.ErrorCode;
import com.spharos.payment.repository.BillingKeyRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BillingKeyServiceImpl implements BillingKeyService {

    private static final String CHANNEL_KEY_PREFIX = "channel-key-";
    private static final String DEFAULT_METHOD_TYPE = "CARD";
    private static final String DEFAULT_CARD_NAME = "등록 카드";
    private static final String DEFAULT_MASKED_NUMBER = "****";

    private final BillingKeyRepository billingKeyRepository;
    private final PortOnePaymentClient portOnePaymentClient;
    private final PortOneProperties portOneProperties;
    private final PaymentTestProperties paymentTestProperties;

    public BillingKeyServiceImpl(
            BillingKeyRepository billingKeyRepository,
            PortOnePaymentClient portOnePaymentClient,
            PortOneProperties portOneProperties,
            PaymentTestProperties paymentTestProperties
    ) {
        this.billingKeyRepository = billingKeyRepository;
        this.portOnePaymentClient = portOnePaymentClient;
        this.portOneProperties = portOneProperties;
        this.paymentTestProperties = paymentTestProperties;
    }

    @Override
    public BillingKeyReadyResponse ready() {
        validateBillingChannelKey(portOneProperties.billingChannelKey());

        return new BillingKeyReadyResponse(
                portOneProperties.storeId(),
                portOneProperties.billingChannelKey(),
                paymentTestProperties.billingKeyMethod(),
                toCustomerResponse()
        );
    }

    @Override
    public BillingKeyResponse register(BillingKeyRegisterRequest request) {
        if (billingKeyRepository.existsByBillingKey(request.billingKey())) {
            throw new BusinessException(ErrorCode.BILLING_KEY_ALREADY_REGISTERED);
        }

        PortOneBillingKeyApiResponse portOneBillingKey = portOnePaymentClient.getBillingKey(request.billingKey());
        validateBillingKey(portOneBillingKey);

        PortOneBillingKeyApiResponse.Method method = resolvePrimaryMethod(portOneBillingKey);
        PortOneBillingKeyApiResponse.Card card = method == null ? null : method.card();

        // 첫 등록 카드는 자동으로 기본 결제수단이 된다.
        boolean makeDefault = billingKeyRepository.count() == 0;

        RegisteredBillingKey registered = new RegisteredBillingKey(
                request.billingKey(),
                paymentTestProperties.customer().id(),
                resolveMethodType(method),
                resolveCardName(card),
                resolveMaskedNumber(card),
                makeDefault
        );
        billingKeyRepository.save(registered);

        log.info(
                "Billing key registered. billingKey={}, default={}",
                maskBillingKey(request.billingKey()),
                makeDefault
        );
        return toResponse(registered);
    }

    @Override
    public List<BillingKeyResponse> list() {
        return billingKeyRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public BillingKeyResponse setDefault(BillingKeyDefaultRequest request) {
        RegisteredBillingKey target = billingKeyRepository.findByBillingKey(request.billingKey())
                .orElseThrow(() -> new BusinessException(ErrorCode.BILLING_KEY_NOT_FOUND));

        // 기본 결제수단은 항상 하나만 유지한다.
        for (RegisteredBillingKey method : billingKeyRepository.findAll()) {
            if (method.getBillingKey().equals(target.getBillingKey())) {
                method.markAsDefault();
            } else {
                method.clearDefault();
            }
            billingKeyRepository.save(method);
        }

        log.info("Default billing key updated. billingKey={}", maskBillingKey(request.billingKey()));
        return toResponse(target);
    }

    @Override
    public BillingKeyResponse getDefault() {
        return billingKeyRepository.findDefault()
                .map(this::toResponse)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEFAULT_BILLING_KEY_NOT_FOUND));
    }

    private void validateBillingKey(PortOneBillingKeyApiResponse portOneBillingKey) {
        if (portOneBillingKey.billingKey() == null || portOneBillingKey.billingKey().isBlank()) {
            throw new BusinessException(ErrorCode.BILLING_KEY_INVALID);
        }
        if (portOneBillingKey.isDeleted()) {
            throw new BusinessException(ErrorCode.BILLING_KEY_DELETED);
        }
    }

    private void validateBillingChannelKey(String billingChannelKey) {
        if (billingChannelKey == null
                || !billingChannelKey.startsWith(CHANNEL_KEY_PREFIX)
                || billingChannelKey.contains("xxxxxxxx")) {
            throw new BusinessException(ErrorCode.INVALID_BILLING_CHANNEL_KEY);
        }
    }

    private PortOneBillingKeyApiResponse.Method resolvePrimaryMethod(PortOneBillingKeyApiResponse response) {
        if (response.methods() == null || response.methods().isEmpty()) {
            return null;
        }
        return response.methods().get(0);
    }

    private String resolveMethodType(PortOneBillingKeyApiResponse.Method method) {
        if (method == null || method.type() == null || method.type().isBlank()) {
            return DEFAULT_METHOD_TYPE;
        }
        return method.type();
    }

    private String resolveCardName(PortOneBillingKeyApiResponse.Card card) {
        if (card == null) {
            return DEFAULT_CARD_NAME;
        }
        if (card.name() != null && !card.name().isBlank()) {
            return card.name();
        }
        if (card.brand() != null && !card.brand().isBlank()) {
            return card.brand();
        }
        return DEFAULT_CARD_NAME;
    }

    private String resolveMaskedNumber(PortOneBillingKeyApiResponse.Card card) {
        if (card == null || card.number() == null || card.number().isBlank()) {
            return DEFAULT_MASKED_NUMBER;
        }
        return card.number();
    }

    private BillingKeyResponse toResponse(RegisteredBillingKey entity) {
        return new BillingKeyResponse(
                entity.getBillingKey(),
                entity.getMethodType(),
                entity.getCardName(),
                entity.getMaskedCardNumber(),
                entity.isDefaultMethod(),
                entity.getRegisteredAt()
        );
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

    private String maskBillingKey(String billingKey) {
        if (billingKey == null || billingKey.length() < 8) {
            return "****";
        }
        return billingKey.substring(0, 4) + "****" + billingKey.substring(billingKey.length() - 4);
    }
}
