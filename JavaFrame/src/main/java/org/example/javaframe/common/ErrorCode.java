package org.example.javaframe.common;

public enum ErrorCode {

    BAD_REQUEST(40000, "请求参数不正确"),
    UNAUTHORIZED(40100, "账号或密码错误"),
    REFRESH_INVALID(40101, "刷新票无效或已使用"),
    NOT_FOUND(40400, "路径不存在"),
METHOD_NOT_ALLOWED(40500, "方法不允许"),
    SYSTEM_ERROR(50000, "系统繁忙");
    

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
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