package com.spharos.payment.repository;

import java.util.List;
import java.util.Optional;

import com.spharos.payment.domain.entity.RegisteredBillingKey;

public interface BillingKeyRepository {

    RegisteredBillingKey save(RegisteredBillingKey billingKey);

    Optional<RegisteredBillingKey> findByBillingKey(String billingKey);

    Optional<RegisteredBillingKey> findDefault();

    List<RegisteredBillingKey> findAll();

    boolean existsByBillingKey(String billingKey);

    long count();
}
