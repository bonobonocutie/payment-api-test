package com.spharos.payment.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * PortOne 빌링키 단건 조회 응답 중 등록/표시에 필요한 필드만 매핑한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PortOneBillingKeyApiResponse(
        String billingKey,
        String status,
        String issuedAt,
        String deletedAt,
        List<Method> methods
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Method(
            String type,
            Card card
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Card(
            String name,
            String number,
            String brand,
            String bin
    ) {
    }

    public boolean isDeleted() {
        return deletedAt != null && !deletedAt.isBlank();
    }
}
