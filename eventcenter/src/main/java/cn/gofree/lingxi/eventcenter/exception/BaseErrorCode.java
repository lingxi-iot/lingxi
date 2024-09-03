package cn.gofree.lingxi.eventcenter.exception;

public interface BaseErrorCode {
    int getHttpCode();

    String getCode();

    String getMsg();
}
