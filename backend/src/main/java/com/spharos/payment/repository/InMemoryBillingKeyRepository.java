package com.spharos.payment.repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.spharos.payment.domain.entity.RegisteredBillingKey;

/**
 * 테스트용 인메모리 빌링키 저장소.
 * 서버 재시작 시 목록이 초기되며, 운영에서는 JPA 구현체로 교체하면 된다.
 */
@Repository
public class InMemoryBillingKeyRepository implements BillingKeyRepository {

    private final Map<String, RegisteredBillingKey> store = new ConcurrentHashMap<>();

    @Override
    public RegisteredBillingKey save(RegisteredBillingKey billingKey) {
        store.put(billingKey.getBillingKey(), billingKey);
        return billingKey;
    }

    @Override
    public Optional<RegisteredBillingKey> findByBillingKey(String billingKey) {
        return Optional.ofNullable(store.get(billingKey));
    }

    @Override
    public Optional<RegisteredBillingKey> findDefault() {
        return store.values().stream()
                .filter(RegisteredBillingKey::isDefaultMethod)
                .findFirst();
    }

    @Override
    public List<RegisteredBillingKey> findAll() {
        return store.values().stream()
                .sorted(Comparator
                        .comparing(RegisteredBillingKey::isDefaultMethod).reversed()
                        .thenComparing(RegisteredBillingKey::getRegisteredAt))
                .toList();
    }

    @Override
    public boolean existsByBillingKey(String billingKey) {
        return store.containsKey(billingKey);
    }

    @Override
    public long count() {
        return store.size();
    }
}
