package cn.gofree.lingxi.eventcenter.enums;


import cn.gofree.lingxi.eventcenter.exception.BaseErrorCode;

public enum DefaultErrorCode implements BaseErrorCode {
    //Default
    Success(200, "Success", "success"),
    InternalError(500, "InternalError", "InternalError"),
    LoginFailed(409, "LoginFailed", "Login failed."),
    ;

    private final int httpCode;
    private final String code;
    private final String msg;

    DefaultErrorCode(int httpCode, String code, String s) {
        this.httpCode = httpCode;
        this.code = code;
        this.msg = s;
    }

    @Override
    public int getHttpCode() {
        return httpCode;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
