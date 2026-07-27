package com.spharos.payment.service;

import java.util.List;

import com.spharos.payment.dto.request.BillingKeyDefaultRequest;
import com.spharos.payment.dto.request.BillingKeyRegisterRequest;
import com.spharos.payment.dto.response.BillingKeyReadyResponse;
import com.spharos.payment.dto.response.BillingKeyResponse;

public interface BillingKeyService {

    BillingKeyReadyResponse ready();

    BillingKeyResponse register(BillingKeyRegisterRequest request);

    List<BillingKeyResponse> list();

    BillingKeyResponse setDefault(BillingKeyDefaultRequest request);

    BillingKeyResponse getDefault();
}
