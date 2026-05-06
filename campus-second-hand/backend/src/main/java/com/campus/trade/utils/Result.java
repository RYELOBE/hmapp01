package com.campus.trade.utils;

import lombok.Data;

@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    private static final int SUCCESS_CODE = 200;
    private static final int ERROR_CODE = 500;

    private Result() {
    }

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(SUCCESS_CODE, "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(SUCCESS_CODE, "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(SUCCESS_CODE, message, data);
    }

    public static <T> Result<T> success(String message) {
        return new Result<>(SUCCESS_CODE, message, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>(ERROR_CODE, message, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    public boolean isSuccess() {
        return this.code == SUCCESS_CODE;
    }

    public enum ResultCode {
        SUCCESS(200, "操作成功"),
        BAD_REQUEST(400, "请求参数错误"),
        UNAUTHORIZED(401, "未授权"),
        FORBIDDEN(403, "禁止访问"),
        NOT_FOUND(404, "资源不存在"),
        INTERNAL_ERROR(500, "服务器内部错误");

        private final int code;
        private final String message;

        ResultCode(int code, String message) {
            this.code = code;
            this.message = message;
        }

        public int getCode() {
            return code;
        }

        public String getMessage() {
            return message;
        }
    }
}
