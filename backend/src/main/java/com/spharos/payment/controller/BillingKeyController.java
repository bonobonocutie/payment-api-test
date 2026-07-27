package com.spharos.payment.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spharos.payment.dto.request.BillingKeyDefaultRequest;
import com.spharos.payment.dto.request.BillingKeyRegisterRequest;
import com.spharos.payment.dto.response.ApiResponse;
import com.spharos.payment.dto.response.BillingKeyReadyResponse;
import com.spharos.payment.dto.response.BillingKeyResponse;
import com.spharos.payment.service.BillingKeyService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/billing-keys")
public class BillingKeyController {

    private final BillingKeyService billingKeyService;

    public BillingKeyController(BillingKeyService billingKeyService) {
        this.billingKeyService = billingKeyService;
    }

    @GetMapping("/ready")
    public ApiResponse<BillingKeyReadyResponse> ready() {
        return ApiResponse.success(billingKeyService.ready());
    }

    @PostMapping
    public ApiResponse<BillingKeyResponse> register(@Valid @RequestBody BillingKeyRegisterRequest request) {
        return ApiResponse.success(billingKeyService.register(request));
    }

    @GetMapping
    public ApiResponse<List<BillingKeyResponse>> list() {
        return ApiResponse.success(billingKeyService.list());
    }

    @GetMapping("/default")
    public ApiResponse<BillingKeyResponse> getDefault() {
        return ApiResponse.success(billingKeyService.getDefault());
    }

    @PostMapping("/default")
    public ApiResponse<BillingKeyResponse> setDefault(@Valid @RequestBody BillingKeyDefaultRequest request) {
        return ApiResponse.success(billingKeyService.setDefault(request));
    }
}
