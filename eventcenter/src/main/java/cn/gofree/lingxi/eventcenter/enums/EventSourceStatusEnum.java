package cn.gofree.lingxi.eventcenter.enums;

import cn.gofree.lingxi.eventcenter.exception.EventBridgeException;

import static cn.gofree.lingxi.eventcenter.enums.EventBridgeErrorCode.EventSourceStatusInvalid;

public enum EventSourceStatusEnum {
    ACTIVATED(1),
    FROZEN(0);

    private Integer code;

    EventSourceStatusEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public static EventSourceStatusEnum parseFromCode(int code) {
        for (EventSourceStatusEnum sourceStatusType : EventSourceStatusEnum.values()) {
            if (sourceStatusType.code == code) {
                return sourceStatusType;
            }
        }
        throw new EventBridgeException(EventSourceStatusInvalid, code);
    }
}
