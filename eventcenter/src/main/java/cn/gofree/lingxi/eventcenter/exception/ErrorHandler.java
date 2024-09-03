package cn.gofree.lingxi.eventcenter.exception;


import cn.gofree.lingxi.eventcenter.common.OffsetManager;
import cn.gofree.lingxi.eventcenter.config.TargetRunnerConfig;
import cn.gofree.lingxi.eventcenter.constant.RuntimeConfigDefine;
import cn.gofree.lingxi.eventcenter.context.EventContext;
import cn.gofree.lingxi.eventcenter.enums.PushRetryStrategyEnum;
import cn.gofree.lingxi.eventcenter.listener.EventSubscriber;
import com.google.common.base.Strings;
import io.openmessaging.connector.api.data.ConnectRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class ErrorHandler {

    @Autowired
    private EventSubscriber eventSubscriber;

    @Autowired
    private EventContext circulatorContext;

    @Autowired
    private OffsetManager offsetManager;

    public void handle(ConnectRecord connectRecord, Throwable t) {
        String eventRunnerName = connectRecord.getExtension(RuntimeConfigDefine.RUNNER_NAME);
        TargetRunnerConfig targetRunnerConfig = circulatorContext.getRunnerConfig(eventRunnerName);
        String eventBusName = targetRunnerConfig.getEventBusName();
        PushRetryStrategyEnum pushRetryStrategyEnum = targetRunnerConfig.getRunOptions().getRetryStrategy().getPushRetryStrategyEnum();

        int retryTimes = parseRetryTimes(connectRecord);
        int delaySec = calcDelaySec(retryTimes, pushRetryStrategyEnum);
        if (delaySec > 0) {
            eventSubscriber.put(eventBusName, connectRecord, delaySec);
        }
        offsetManager.commit(connectRecord);
    }

    private int parseRetryTimes(ConnectRecord connectRecord) {
        int retryTimes = 0;
        String retryTag = connectRecord.getExtension(RuntimeConfigDefine.CONNECT_RECORDS_KEY);
        if (Strings.isNullOrEmpty(retryTag)) {
            return retryTimes;
        }
        try {
            retryTimes = Integer.parseInt(retryTag);
        } catch (Throwable e) {
            log.warn("parse retry times failed. retryTag={}", retryTag);
        }
        return retryTimes;
    }

    /**
     * Return right time or -1 (already done)
     *
     * @param retryTimes
     * @param pushRetryStrategyEnum
     * @return
     */
    private int calcDelaySec(int retryTimes, PushRetryStrategyEnum pushRetryStrategyEnum) {
        switch (pushRetryStrategyEnum) {
            case BACKOFF_RETRY:
                if (retryTimes >= pushRetryStrategyEnum.getRetryTimes()) {
                    return -1;
                }
                return 10;
            case EXPONENTIAL_DECAY_RETRY:
                if (retryTimes >= pushRetryStrategyEnum.getRetryTimes()) {
                    return -1;
                }
                int pow = (int) Math.pow(2, 3 + retryTimes);
                return pow > 512 ? 512 : pow;
            default:
                return -1;
        }
    }

}
