package cn.gofree.lingxi.eventcenter.listener;


import cn.gofree.lingxi.eventcenter.common.OffsetManager;
import cn.gofree.lingxi.eventcenter.context.EventContext;
import cn.gofree.lingxi.eventcenter.exception.ErrorHandler;
import cn.gofree.lingxi.eventcenter.runtime.ServiceThread;
import cn.gofree.lingxi.eventcenter.util.ExceptionUtil;
import com.alibaba.fastjson.JSON;
import io.openmessaging.connector.api.component.task.sink.SinkTask;
import io.openmessaging.connector.api.data.ConnectRecord;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * event target push to sink task
 */
public class EventTargetTrigger extends ServiceThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventTargetTrigger.class);

    private final EventContext circulatorContext;
    private final OffsetManager offsetManager;
    private final ErrorHandler errorHandler;
    private volatile Integer batchSize = 100;

    public EventTargetTrigger(EventContext circulatorContext, OffsetManager offsetManager,
                              ErrorHandler errorHandler) {
        this.circulatorContext = circulatorContext;
        this.offsetManager = offsetManager;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run() {
        while (!stopped) {
            Map<String, List<ConnectRecord>> targetRecordMap = circulatorContext.takeTargetRecords(batchSize);
            if (MapUtils.isEmpty(targetRecordMap)) {
                LOGGER.trace("current target pusher is empty");
                this.waitForRunning(1000);
                continue;
            }
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("start push content by pusher - {}", JSON.toJSONString(targetRecordMap));
            }

            for (String runnerName : targetRecordMap.keySet()) {
                ExecutorService executorService = circulatorContext.getExecutorService(runnerName);
                executorService.execute(() -> {
                    SinkTask sinkTask = circulatorContext.getPusherTaskMap().get(runnerName);
                    List<ConnectRecord> triggerRecords = targetRecordMap.get(runnerName);
                    try {
                        sinkTask.put(triggerRecords);
                        offsetManager.commit(triggerRecords);
                    } catch (Exception exception) {
                        LOGGER.error(getServiceName() + " push target exception, stackTrace-", exception);
                        triggerRecords.forEach(triggerRecord -> errorHandler.handle(triggerRecord, exception));
                    }
                });
            }
        }
    }

    @Override
    public String getServiceName() {
        return EventTargetTrigger.class.getSimpleName();
    }

    @Override
    public void start() {
        thread.start();
    }

    @Override
    public void shutdown() {
        try {
            circulatorContext.releaseExecutorService();
            circulatorContext.releaseTriggerTask();
        } catch (Exception e) {
            LOGGER.error(String.format("current thread: %s, error Track: %s ", getServiceName(), ExceptionUtil.getErrorMessage(e)));
        }
    }
}

