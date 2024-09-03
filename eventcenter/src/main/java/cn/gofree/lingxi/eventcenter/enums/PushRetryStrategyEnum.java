package cn.gofree.lingxi.eventcenter.enums;

import com.google.common.base.Strings;

public enum PushRetryStrategyEnum {
    /**
     * 3 times: every 10s~20s
     */
    BACKOFF_RETRY(1, 3),
    /**
     * 176 times: 1, 2, 4, 8, 16, 36, 64, 128, 256, 512, 512...512s(176)
     */
    EXPONENTIAL_DECAY_RETRY(2, 176);

    private int code;
    private int retryTimes;
    PushRetryStrategyEnum(int code, int retryTimes) {
        this.code = code;
        this.retryTimes = retryTimes;
    }

    public static PushRetryStrategyEnum parse(String pushRetryStrategy) {
        if (Strings.isNullOrEmpty(pushRetryStrategy)) {
            return BACKOFF_RETRY;
        }
        for (PushRetryStrategyEnum pushRetryStrategyEnum : PushRetryStrategyEnum.values()) {
            if (pushRetryStrategyEnum.name().equals(pushRetryStrategy)) {
                return pushRetryStrategyEnum;
            }
        }
        return BACKOFF_RETRY;
    }

    public int getCode() {
        return code;
    }

    public int getRetryTimes() {
        return retryTimes;
    }
}
