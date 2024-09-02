package cn.gofree.lingxi.eventbridge.exception;

public interface BaseErrorCode {
    int getHttpCode();

    String getCode();

    String getMsg();
}
