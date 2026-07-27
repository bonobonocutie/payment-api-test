package com.spharos.payment.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * PortOne 결제 단건 조회 API 응답 중 검증에 필요한 필드만 매핑한다.
 * 불필요한 필드는 무시해 API 스펙 변경에 대한 결합도를 낮춘다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOnePaymentApiResponse(
        String id,
        String status,
        String orderName,
        Amount amount,
        // REST 응답의 currency는 "KRW" 문자열로 내려온다.
        String currency,
        Channel channel
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Amount(Long total) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Channel(String type) {
    }
}
