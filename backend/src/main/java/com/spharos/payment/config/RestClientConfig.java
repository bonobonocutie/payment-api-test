package com.spharos.payment.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * PortOne REST 호출용 RestClient를 구성한다.
 * SDK 대신 REST를 쓰는 이유: Java 17에서 Kotlin suspend API 의존을 피하고 책임을 명확히 분리하기 위함.
 */
@Configuration
public class RestClientConfig {

    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
