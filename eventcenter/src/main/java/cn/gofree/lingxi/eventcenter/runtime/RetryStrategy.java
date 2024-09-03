package cn.gofree.lingxi.eventcenter.runtime;

import cn.gofree.lingxi.eventcenter.enums.PushRetryStrategyEnum;
import lombok.Builder;
import lombok.Data;

/**
 * 重试策略
 */
@Builder
@Data
public class RetryStrategy {

    private PushRetryStrategyEnum pushRetryStrategyEnum;

    private int maximumEventAgeInSeconds;

    private int maximumRetryAttempts;
}
