package cn.gofree.lingxi.eventbridge.runtime;

import cn.gofree.lingxi.eventbridge.enums.PushRetryStrategyEnum;
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
