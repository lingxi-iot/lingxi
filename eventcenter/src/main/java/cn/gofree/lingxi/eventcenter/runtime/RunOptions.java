package cn.gofree.lingxi.eventcenter.runtime;

import cn.gofree.lingxi.eventcenter.enums.ErrorToleranceEnum;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RunOptions {
    private ErrorToleranceEnum errorTolerance;
    private RetryStrategy retryStrategy;
    private DeadLetterQueue deadLetterQueue;
}
