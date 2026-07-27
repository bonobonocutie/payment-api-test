package com.spharos.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PortOne 연동에 필요한 설정을 환경변수(.env)로부터 바인딩한다.
 * 시크릿과 공개 식별값을 한 곳에서 관리해 하드코딩을 방지한다.
 */
@ConfigurationProperties(prefix = "portone")
public record PortOneProperties(
        String apiSecret,
        String storeId,
        String channelKey,
        // 빌링키(결제수단 등록)용 채널. 일반결제 채널과 분리한다.
        String billingChannelKey,
        String apiBaseUrl,
        String allowedChannelType
) {
}
