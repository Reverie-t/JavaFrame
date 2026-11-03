package org.example.javaframe.common;

public record Result<T>(int code, String message, T data) {

    public static <T> Result<T> success(T data) {
        return new Result<>(0, "success", data);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null);
    }
    public static <T> Result<T> fail(int code, String message, T data) {
        return new Result<>(code, message, data);
    }
}