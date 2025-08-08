package com.example.api;

import java.util.UUID;

/**
 * 统一API响应模型
 */
public class ApiResponse<T> {
    private String code;     // OK / VALIDATION_ERROR / VERSION_MISMATCH / NOT_FOUND / INTERNAL_ERROR
    private String message;
    private String traceId;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = "OK";
        r.message = "";
        r.traceId = UUID.randomUUID().toString();
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.code = code;
        r.message = message;
        r.traceId = UUID.randomUUID().toString();
        r.data = null;
        return r;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}

