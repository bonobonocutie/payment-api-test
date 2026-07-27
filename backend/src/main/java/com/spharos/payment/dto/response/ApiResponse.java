package com.spharos.payment.dto.response;

/**
 * 모든 API의 공통 응답 래퍼.
 * 프론트엔드가 성공/실패를 동일한 계약으로 처리할 수 있게 한다.
 */
public record ApiResponse<T>(
        boolean success,
        String code,
        String message,
        T data
) {
    private static final String SUCCESS_CODE = "OK";
    private static final String SUCCESS_MESSAGE = "요청이 성공적으로 처리되었습니다.";

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, SUCCESS_CODE, SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, SUCCESS_CODE, message, data);
    }

    public static <T> ApiResponse<T> failure(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
