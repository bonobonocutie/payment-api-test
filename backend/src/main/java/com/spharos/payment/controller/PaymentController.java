package com.spharos.payment.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spharos.payment.dto.request.PaymentVerifyRequest;
import com.spharos.payment.dto.response.ApiResponse;
import com.spharos.payment.dto.response.PaymentReadyResponse;
import com.spharos.payment.dto.response.PaymentVerifyResponse;
import com.spharos.payment.service.PaymentService;

import jakarta.validation.Valid;

/**
 * 결제 API의 요청/응답만 담당한다.
 * 비즈니스 규칙은 Service로 위임한다.
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/ready")
    public ApiResponse<PaymentReadyResponse> ready() {
        return ApiResponse.success(paymentService.ready());
    }

    @PostMapping("/verify")
    public ApiResponse<PaymentVerifyResponse> verify(@Valid @RequestBody PaymentVerifyRequest request) {
        return ApiResponse.success(paymentService.verify(request));
    }
}
