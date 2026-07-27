package com.spharos.payment.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 시크릿이 코드/이미지에 포함되지 않고 환경변수로만 주입됐는지 기동 시점에 검증한다.
 * 운영에서는 개발자가 키를 직접 보지 않고 AWS/Docker/GitHub Secrets 등으로만 주입하는 전제를 강제한다.
 */
@Slf4j
@Component
public class SecretEnvironmentValidator implements ApplicationRunner {

    private static final String PLACEHOLDER_API_SECRET = "your_portone_api_secret";
    private static final String PLACEHOLDER_PREFIX = "여기에_";

    private final Environment environment;
    private final PortOneProperties portOneProperties;

    public SecretEnvironmentValidator(Environment environment, PortOneProperties portOneProperties) {
        this.environment = environment;
        this.portOneProperties = portOneProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        validateApiSecret(portOneProperties.apiSecret());
        validateStoreId(portOneProperties.storeId());
        validateChannelKey(portOneProperties.channelKey());

        if (isProdProfile()) {
            log.info("Production profile active. Secrets must come from external environment injection.");
        } else {
            log.info("Non-prod profile active. Local .env is allowed for developer machines only.");
        }
    }

    private void validateApiSecret(String apiSecret) {
        if (isBlank(apiSecret)
                || PLACEHOLDER_API_SECRET.equals(apiSecret)
                || apiSecret.startsWith(PLACEHOLDER_PREFIX)
                || apiSecret.startsWith("test_sk_")) {
            throw new IllegalStateException(
                    "PORTONE_API_SECRET이 올바르지 않습니다. PortOne V2 API Secret을 환경변수로 주입하세요."
            );
        }
    }

    private void validateStoreId(String storeId) {
        if (isBlank(storeId) || !storeId.startsWith("store-") || storeId.contains("xxxxxxxx")) {
            throw new IllegalStateException(
                    "PORTONE_STORE_ID가 올바르지 않습니다. 환경변수로 실제 store- 값을 주입하세요."
            );
        }
    }

    private void validateChannelKey(String channelKey) {
        if (isBlank(channelKey) || !channelKey.startsWith("channel-key-") || channelKey.contains("xxxxxxxx")) {
            throw new IllegalStateException(
                    "PORTONE_CHANNEL_KEY가 올바르지 않습니다. 환경변수로 실제 channel-key 값을 주입하세요."
            );
        }
    }

    private boolean isProdProfile() {
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
