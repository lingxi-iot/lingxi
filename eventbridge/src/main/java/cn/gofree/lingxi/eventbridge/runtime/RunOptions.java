package cn.gofree.lingxi.eventbridge.runtime;

import cn.gofree.lingxi.eventbridge.enums.ErrorToleranceEnum;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RunOptions {
    private ErrorToleranceEnum errorTolerance;
    private RetryStrategy retryStrategy;
    private DeadLetterQueue deadLetterQueue;
}
