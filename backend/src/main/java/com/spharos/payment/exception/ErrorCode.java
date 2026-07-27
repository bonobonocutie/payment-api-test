package com.spharos.payment.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "잘못된 요청입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "요청한 API를 찾을 수 없습니다."),
    INVALID_STORE_ID(HttpStatus.BAD_REQUEST, "INVALID_STORE_ID", "PortOne V2 Store ID 형식이 올바르지 않습니다. store- 로 시작하는 값을 사용하세요."),
    PAYMENT_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "PAYMENT_SESSION_NOT_FOUND", "결제 세션을 찾을 수 없습니다."),
    PAYMENT_ALREADY_VERIFIED(HttpStatus.CONFLICT, "PAYMENT_ALREADY_VERIFIED", "이미 검증된 결제입니다."),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_AMOUNT_MISMATCH", "결제 금액이 일치하지 않습니다."),
    PAYMENT_ORDER_NAME_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_ORDER_NAME_MISMATCH", "주문명이 일치하지 않습니다."),
    PAYMENT_CURRENCY_MISMATCH(HttpStatus.BAD_REQUEST, "PAYMENT_CURRENCY_MISMATCH", "통화 정보가 일치하지 않습니다."),
    PAYMENT_NOT_PAID(HttpStatus.BAD_REQUEST, "PAYMENT_NOT_PAID", "결제가 완료되지 않았습니다."),
    PAYMENT_CHANNEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PAYMENT_CHANNEL_NOT_ALLOWED", "허용되지 않은 결제 채널입니다."),
    BILLING_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "BILLING_KEY_NOT_FOUND", "등록된 결제수단을 찾을 수 없습니다."),
    BILLING_KEY_ALREADY_REGISTERED(HttpStatus.CONFLICT, "BILLING_KEY_ALREADY_REGISTERED", "이미 등록된 결제수단입니다."),
    BILLING_KEY_INVALID(HttpStatus.BAD_REQUEST, "BILLING_KEY_INVALID", "유효하지 않은 빌링키입니다."),
    BILLING_KEY_DELETED(HttpStatus.BAD_REQUEST, "BILLING_KEY_DELETED", "삭제된 빌링키입니다."),
    DEFAULT_BILLING_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "DEFAULT_BILLING_KEY_NOT_FOUND", "기본 결제수단이 설정되어 있지 않습니다."),
    INVALID_BILLING_CHANNEL_KEY(HttpStatus.BAD_REQUEST, "INVALID_BILLING_CHANNEL_KEY", "빌링 채널 키 형식이 올바르지 않습니다."),
    PORTONE_API_ERROR(HttpStatus.BAD_GATEWAY, "PORTONE_API_ERROR", "PortOne API 호출에 실패했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
