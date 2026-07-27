package com.spharos.payment.client;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

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
            int statusCode = exception.getStatusCode().value();
            String responseBody = exception.getResponseBodyAsString();

            log.error(
                    "PortOne payment lookup failed. paymentId={}, status={}, body={}",
                    paymentId,
                    statusCode,
                    responseBody
            );

            if (statusCode == 401 || statusCode == 403) {
                // PG사 Secret Key(test_sk_...)를 넣으면 401이 난다. PortOne V2 API Secret이 필요하다.
                throw new BusinessException(
                        ErrorCode.PORTONE_API_ERROR,
                        "PortOne 인증에 실패했습니다. 관리자 콘솔의 V2 API Secret을 PORTONE_API_SECRET에 설정했는지 확인하세요. (PG사 Secret Key는 사용할 수 없습니다)"
                );
            }

            throw new BusinessException(
                    ErrorCode.PORTONE_API_ERROR,
                    "PortOne API 호출에 실패했습니다. (HTTP " + statusCode + ")"
            );
        } catch (RestClientException exception) {
            log.error("PortOne payment lookup request failed. paymentId={}", paymentId, exception);
            throw new BusinessException(ErrorCode.PORTONE_API_ERROR);
        }
    }
}
