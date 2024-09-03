package cn.gofree.lingxi.eventbridge.exception;

import cn.gofree.lingxi.eventbridge.enums.DefaultErrorCode;

import java.text.MessageFormat;

public class EventBridgeException extends RuntimeException{

    private int httpCode;
    private String code;

    public EventBridgeException(String msg) {
        super(MessageFormat.format(DefaultErrorCode.InternalError.getMsg(), msg));
        this.code = DefaultErrorCode.InternalError.getCode();
    }

    public EventBridgeException(String code, String msg) {
        super(msg);
        this.code = code;
    }

    public EventBridgeException(String msg, Throwable throwable) {
        super(MessageFormat.format(DefaultErrorCode.InternalError.getMsg(), msg), throwable);
        this.code = DefaultErrorCode.InternalError.getCode();
    }

    public EventBridgeException(Throwable throwable) {
        super(throwable);
        this.code = DefaultErrorCode.InternalError.getCode();
    }

    public EventBridgeException(BaseErrorCode errorCode, Throwable throwable, Object... args) {
        super(MessageFormat.format(errorCode.getMsg(), args), throwable);
        this.code = errorCode.getCode();
        this.httpCode = errorCode.getHttpCode();
    }

    public EventBridgeException(BaseErrorCode errorCode, Object... args) {
        super(MessageFormat.format(errorCode.getMsg(), args));
        this.code = errorCode.getCode();
        this.httpCode = errorCode.getHttpCode();
    }

    public String getCode() {
        return code;
    }

    public int getHttpCode() {
        return httpCode;
    }
}
