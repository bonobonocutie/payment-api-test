package com.spharos.payment.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.spharos.payment.client.dto.PortOneBillingKeyApiResponse;
import com.spharos.payment.client.dto.PortOneBillingKeyPaymentRequest;
import com.spharos.payment.client.dto.PortOnePaymentApiResponse;
import com.spharos.payment.config.PortOneProperties;
import com.spharos.payment.exception.BusinessException;
import com.spharos.payment.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PortOneRestPaymentClient implements PortOnePaymentClient {

    private static final String AUTHORIZATION_PREFIX = "PortOne ";
    private static final String PAYMENT_PATH = "/payments/{paymentId}";
    private static final String BILLING_KEY_PATH = "/billing-keys/{billingKey}";
    private static final String PAY_WITH_BILLING_KEY_PATH = "/payments/{paymentId}/billing-key";

    private final RestClient restClient;

    public PortOneRestPaymentClient(
            RestClient.Builder restClientBuilder,
            PortOneProperties portOneProperties
    ) {
        this.restClient = restClientBuilder
                .baseUrl(portOneProperties.apiBaseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, AUTHORIZATION_PREFIX + portOneProperties.apiSecret())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public PortOnePaymentApiResponse getPayment(String paymentId) {
        try {
            PortOnePaymentApiResponse response = restClient.get()
                    .uri(PAYMENT_PATH, paymentId)
                    .retrieve()
                    .body(PortOnePaymentApiResponse.class);

            if (response == null) {
                throw new BusinessException(ErrorCode.PORTONE_API_ERROR, "PortOne 결제 조회 응답이 비어 있습니다.");
            }

            return response;
        } catch (RestClientResponseException exception) {
            throw mapPortOneException("payment lookup", paymentId, exception);
        } catch (RestClientException exception) {
            log.error("PortOne payment lookup request failed. paymentId={}", paymentId, exception);
            throw new BusinessException(ErrorCode.PORTONE_API_ERROR);
        }
    }

    @Override
    public PortOneBillingKeyApiResponse getBillingKey(String billingKey) {
        try {
            PortOneBillingKeyApiResponse response = restClient.get()
                    .uri(BILLING_KEY_PATH, billingKey)
                    .retrieve()
                    .body(PortOneBillingKeyApiResponse.class);

            if (response == null) {
                throw new BusinessException(ErrorCode.PORTONE_API_ERROR, "PortOne 빌링키 조회 응답이 비어 있습니다.");
            }

            return response;
        } catch (RestClientResponseException exception) {
            throw mapPortOneException("billing key lookup", billingKey, exception);
        } catch (RestClientException exception) {
            log.error("PortOne billing key lookup request failed. billingKey={}", billingKey, exception);
            throw new BusinessException(ErrorCode.PORTONE_API_ERROR);
        }
    }

    @Override
    public void payWithBillingKey(String paymentId, PortOneBillingKeyPaymentRequest request) {
        try {
            restClient.post()
                    .uri(PAY_WITH_BILLING_KEY_PATH, paymentId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException exception) {
            throw mapPortOneException("billing key payment", paymentId, exception);
        } catch (RestClientException exception) {
            log.error("PortOne billing key payment request failed. paymentId={}", paymentId, exception);
            throw new BusinessException(ErrorCode.PORTONE_API_ERROR);
        }
    }

    private BusinessException mapPortOneException(
            String operation,
            String identifier,
            RestClientResponseException exception
    ) {
        int statusCode = exception.getStatusCode().value();
        String responseBody = exception.getResponseBodyAsString();

        log.error(
                "PortOne {} failed. id={}, status={}, body={}",
                operation,
                identifier,
                statusCode,
                responseBody
        );

        if (statusCode == 401 || statusCode == 403) {
            return new BusinessException(
                    ErrorCode.PORTONE_API_ERROR,
                    "PortOne 인증에 실패했습니다. 관리자 콘솔의 V2 API Secret을 PORTONE_API_SECRET에 설정했는지 확인하세요. (PG사 Secret Key는 사용할 수 없습니다)"
            );
        }

        if (statusCode == 404) {
            return new BusinessException(
                    ErrorCode.PORTONE_API_ERROR,
                    "PortOne 리소스를 찾을 수 없습니다. (" + operation + ")"
            );
        }

        if (statusCode == 400) {
            return new BusinessException(
                    ErrorCode.PORTONE_API_ERROR,
                    "PortOne 요청이 거부되었습니다. (HTTP 400) " + summarizePortOneError(responseBody)
            );
        }

        return new BusinessException(
                ErrorCode.PORTONE_API_ERROR,
                "PortOne API 호출에 실패했습니다. (HTTP " + statusCode + ")"
        );
    }

    private String summarizePortOneError(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "";
        }
        // 프론트에 너무 긴 JSON을 그대로 노출하지 않고, 원인 파악용으로 짧게 전달한다.
        String trimmed = responseBody.replaceAll("\\s+", " ").trim();
        int maxLength = 300;
        if (trimmed.length() > maxLength) {
            return trimmed.substring(0, maxLength) + "...";
        }
        return trimmed;
    }
}
