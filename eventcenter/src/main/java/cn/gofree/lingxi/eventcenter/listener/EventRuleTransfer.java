package cn.gofree.lingxi.eventcenter.listener;

import cn.gofree.lingxi.eventcenter.common.OffsetManager;
import cn.gofree.lingxi.eventcenter.context.EventContext;
import cn.gofree.lingxi.eventcenter.exception.ErrorHandler;
import cn.gofree.lingxi.eventcenter.runtime.ServiceThread;
import cn.gofree.lingxi.eventcenter.transfer.TransformEngine;
import cn.gofree.lingxi.eventcenter.util.ExceptionUtil;
import com.google.common.collect.Lists;
import io.openmessaging.connector.api.data.ConnectRecord;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventRuleTransfer extends ServiceThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventRuleTransfer.class);

    private volatile Integer batchSize = 100;

    private final EventContext circulatorContext;
    private final OffsetManager offsetManager;
    private final ErrorHandler errorHandler;

    public EventRuleTransfer(EventContext circulatorContext, OffsetManager offsetManager,
                             ErrorHandler errorHandler) {
        this.circulatorContext = circulatorContext;
        this.offsetManager = offsetManager;
        this.errorHandler = errorHandler;
    }

    @Override
    public String getServiceName() {
        return this.getClass().getSimpleName();
    }

    @PostConstruct
    public void init() {
        super.start();
    }

    @Override
    public void run() {
        List<ConnectRecord> afterTransformConnect = new CopyOnWriteArrayList<>();
        while (!stopped) {
            try {
                Map<String, List<ConnectRecord>> eventRecordMap = circulatorContext.takeEventRecords(batchSize);
                if (MapUtils.isEmpty(eventRecordMap)) {
                    LOGGER.trace("listen eventRecords is empty, continue by curTime - {}", System.currentTimeMillis());
                    this.waitForRunning(1000);
                    continue;
                }
                Map<String, TransformEngine<ConnectRecord>> latestTransformMap = circulatorContext.getTaskTransformMap();
                if (MapUtils.isEmpty(latestTransformMap)) {
                    LOGGER.warn("latest transform engine is empty, continue by curTime - {}", System.currentTimeMillis());
                    this.waitForRunning(3000);
                    continue;
                }

                afterTransformConnect.clear();
                List<CompletableFuture<Void>> completableFutures = Lists.newArrayList();
                for (String runnerName : eventRecordMap.keySet()) {
                    TransformEngine<ConnectRecord> curTransformEngine = latestTransformMap.get(runnerName);
                    List<ConnectRecord> curEventRecords = eventRecordMap.get(runnerName);
                    curEventRecords.forEach(pullRecord -> {
                        CompletableFuture<Void> transformFuture = CompletableFuture.supplyAsync(() -> curTransformEngine.doTransforms(pullRecord))
                                .exceptionally((exception) -> {
                                    LOGGER.error("transfer do transform event record failed, stackTrace-", exception);
                                    errorHandler.handle(pullRecord, exception);
                                    return null;
                                })
                                .thenAccept(pushRecord -> {
                                    if (Objects.nonNull(pushRecord)) {
                                        afterTransformConnect.add(pushRecord);
                                    } else {
                                        offsetManager.commit(pullRecord);
                                    }
                                });
                        completableFutures.add(transformFuture);
                    });
                }
                CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[eventRecordMap.values().size()])).get();
                circulatorContext.offerTargetTaskQueue(afterTransformConnect);
                LOGGER.info("offer target task queues succeed, transforms.size={}", afterTransformConnect.size());
            } catch (Exception exception) {
                LOGGER.error("transfer event record failed, stackTrace-", exception);
                afterTransformConnect.forEach(transferRecord -> errorHandler.handle(transferRecord, exception));
            }

        }
    }

    @Override
    public void start() {
        thread.start();
    }

    @Override
    public void shutdown() {
        try {
            circulatorContext.releaseTaskTransform();
        } catch (Exception e) {
            LOGGER.error(String.format("current thread: %s, error Track: %s ", getServiceName(), ExceptionUtil.getErrorMessage(e)));
        }
    }
}
