package cn.gofree.lingxi.eventcenter.enums;

import cn.gofree.lingxi.eventcenter.exception.EventBridgeException;

import static cn.gofree.lingxi.eventcenter.enums.EventBridgeErrorCode.EventSourceTypeInvalid;

public enum EventSourceTypeEnum {
    OFFICIAL_SERVICE(1),

    PARTNER_SAAS(2),

    USER_DEFINED(3);

    private int code;

    EventSourceTypeEnum(int code) {
        this.code = code;
    }

    public int getCode() {
        return this.code;
    }

    public static EventSourceTypeEnum parseFromCode(int code) {
        for (EventSourceTypeEnum sourceType : EventSourceTypeEnum.values()) {
            if (sourceType.code == code) {
                return sourceType;
            }
        }
        throw new EventBridgeException(EventSourceTypeInvalid, code);
    }

    public static EventSourceTypeEnum parseFromName(String name) {
        for (EventSourceTypeEnum sourceType : EventSourceTypeEnum.values()) {
            if (sourceType.toString()
                    .equals(name)) {
                return sourceType;
            }
        }
        throw new EventBridgeException(EventSourceTypeInvalid, name);
    }
}
