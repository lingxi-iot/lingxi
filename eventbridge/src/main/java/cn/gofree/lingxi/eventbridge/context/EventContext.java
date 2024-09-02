package cn.gofree.lingxi.eventbridge.context;

import cn.gofree.lingxi.eventbridge.config.TargetRunnerConfig;
import cn.gofree.lingxi.eventbridge.constant.RuntimeConfigDefine;
import cn.gofree.lingxi.eventbridge.entity.TargetKeyValue;
import cn.gofree.lingxi.eventbridge.enums.RefreshTypeEnum;
import cn.gofree.lingxi.eventbridge.plugin.PluginLoader;
import cn.gofree.lingxi.eventbridge.plugin.PluginManager;
import cn.gofree.lingxi.eventbridge.listener.TargetRunnerListener;
import cn.gofree.lingxi.eventbridge.transfer.TransformEngine;
import cn.gofree.lingxi.eventbridge.util.ShutdownUtils;
import cn.gofree.lingxi.eventbridge.util.ThreadUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import io.openmessaging.connector.api.component.task.sink.SinkTask;
import io.openmessaging.connector.api.data.ConnectRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

@Component
public class EventContext implements TargetRunnerListener {

    private final static Logger log= LoggerFactory.getLogger(EventContext.class);
    private static final Integer QUEUE_CAPACITY = 50000;
    @Autowired
    private PluginManager plugin;

    /**
     * BlockingQueue 阻塞队列
     */
    private final BlockingQueue<ConnectRecord> eventQueue = new LinkedBlockingQueue<>(50000);
    private final BlockingQueue<ConnectRecord> targetQueue = new LinkedBlockingQueue<>(50000);
    private final Map<String/*RunnerName*/, TargetRunnerConfig> runnerConfigMap = new ConcurrentHashMap<>(30);
    private final Map<String/*RunnerName*/, TransformEngine<ConnectRecord>> taskTransformMap = new ConcurrentHashMap<>(20);
    private final Map<String/*RunnerName*/, BlockingQueue<ConnectRecord>> eventQueueMap = new ConcurrentHashMap<>(30);
    private final Map<String/*RunnerName*/, BlockingQueue<ConnectRecord>> targetQueueMap = new ConcurrentHashMap<>(30);
    private final Map<String/*RunnerName*/, SinkTask> pusherTaskMap = new ConcurrentHashMap<>(20);
    private final Map<String/*RunnerName*/, ExecutorService> pusherExecutorMap = new ConcurrentHashMap<>(10);

    public void initEventContext(Set<TargetRunnerConfig> targetRunnerConfigs){
        if (CollectionUtils.isEmpty(targetRunnerConfigs)){
            return;
        }
        for (TargetRunnerConfig targetRunnerConfig:targetRunnerConfigs)
        {
            onAddTargetRunner(targetRunnerConfig);
        }
    }
    /**
     * take batch target records
     *
     * @param batchSize
     * @return
     */
    public Map<String, List<ConnectRecord>> takeTargetRecords(Integer batchSize) {
        if (targetQueue.isEmpty()) {
            return null;
        }
        List<ConnectRecord> targetRecords = Lists.newArrayList();
        targetQueue.drainTo(targetRecords, batchSize);
        return buildWithRunnerNameKeyMap(targetRecords);
    }
    /**
     * get specific thread pool by push name
     *
     * @param runnerName
     * @return
     */
    public ExecutorService getExecutorService(String runnerName) {
        return pusherExecutorMap.get(runnerName);
    }
    @Override
    public void onAddTargetRunner(TargetRunnerConfig targetRunnerConfig) {
            refreshRunnerContext(targetRunnerConfig,RefreshTypeEnum.ADD);
    }

    @Override
    public void onUpdateTargetRunner(TargetRunnerConfig targetRunnerConfig) {
        refreshRunnerContext(targetRunnerConfig,RefreshTypeEnum.UPDATE);
    }

    @Override
    public void onDeleteTargetRunner(TargetRunnerConfig targetRunnerConfig) {
        refreshRunnerContext(targetRunnerConfig,RefreshTypeEnum.DELETE);
    }
    public TargetRunnerConfig getRunnerConfig(String runnerName){
        return runnerConfigMap.get(runnerName);
    }
    private void refreshRunnerContext(TargetRunnerConfig targetRunnerConfig, RefreshTypeEnum refreshTypeEnum){
        String runnerName = targetRunnerConfig.getName();
        switch (refreshTypeEnum) {
            case ADD:
            case UPDATE:
                runnerConfigMap.put(runnerName,targetRunnerConfig);
                TransformEngine<ConnectRecord> transformChain =new TransformEngine<>(targetRunnerConfig.getComponents(),plugin);
                taskTransformMap.put(runnerName,transformChain);

                int endIndex=targetRunnerConfig.getComponents().size() -1;
                TargetKeyValue targetKeyValue=new TargetKeyValue(targetRunnerConfig.getComponents().get(endIndex));
                SinkTask sinkTask=initTargetSinkTask(targetKeyValue);
            case DELETE:
            default:
                break;
        }
    }
    /**
     * init target sink task
     *
     * @param targetKeyValue
     * @return
     */
    private SinkTask initTargetSinkTask(TargetKeyValue targetKeyValue) {
        String taskClass = targetKeyValue.getString(RuntimeConfigDefine.RUNNER_CLASS);
        ClassLoader loader = plugin.getPluginClassLoader(taskClass);
        Class taskClazz;
        boolean isolationFlag = false;
        try {
            if (loader instanceof PluginLoader) {
                taskClazz = ((PluginLoader) loader).loadClass(taskClass, false);
                isolationFlag = true;
            } else {
                taskClazz = Class.forName(taskClass);
            }
            SinkTask sinkTask = (SinkTask) taskClazz.getDeclaredConstructor().newInstance();
            sinkTask.init(targetKeyValue);
            TriggerTaskContext sinkTaskContext = new TriggerTaskContext(targetKeyValue);
            sinkTask.start(sinkTaskContext);
            if (isolationFlag) {
                PluginManager.compareAndSwapLoaders(loader);
            }
            return sinkTask;
        } catch (Exception exception) {
            log.error("task class -" + taskClass + "- init its sinkTask failed, ex- ", exception);
        }
        return null;
    }
    /**
     * offer event records
     *
     * @param connectRecords
     */
    public boolean offerEventRecords(List<ConnectRecord> connectRecords) {
        Map<String, List<ConnectRecord>> recordMap = buildWithRunnerNameKeyMap(connectRecords);
        updateRecordQueueMap(recordMap, eventQueueMap);
        return eventQueue.addAll(connectRecords);
    }
    /**
     * update record queue map
     *
     * @param recordMap
     * @param eventQueueMap
     */
    private boolean updateRecordQueueMap(Map<String, List<ConnectRecord>> recordMap,
                                         Map<String, BlockingQueue<ConnectRecord>> eventQueueMap) {
        try {
            for (String runnerName : recordMap.keySet()) {
                BlockingQueue<ConnectRecord> recordQueue = eventQueueMap.get(runnerName);
                if (CollectionUtils.isEmpty(recordQueue)) {
                    recordQueue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
                }
                recordQueue.addAll(recordMap.get(runnerName));
                eventQueueMap.put(runnerName, recordQueue);
            }
            return true;
        } catch (Exception exception) {
            return false;
        }
    }
    /**
     * user runner-name as key
     *
     * @param eventRecords
     * @return
     */
    private Map<String, List<ConnectRecord>> buildWithRunnerNameKeyMap(List<ConnectRecord> eventRecords) {
        Map<String, List<ConnectRecord>> eventRecordMap = Maps.newHashMap();
        for (ConnectRecord connectRecord : eventRecords) {
            String runnerName = connectRecord.getExtension(RuntimeConfigDefine.RUNNER_NAME);
            List<ConnectRecord> curEventRecords = eventRecordMap.get(runnerName);
            if (CollectionUtils.isEmpty(curEventRecords)) {
                curEventRecords = Lists.newArrayList();
            }
            curEventRecords.add(connectRecord);
            eventRecordMap.put(runnerName, curEventRecords);
        }
        return eventRecordMap;
    }
    /**
     * take event records
     *
     * @return
     */
    public Map<String, List<ConnectRecord>> takeEventRecords(int batchSize) {
        if (eventQueue.isEmpty()) {
            return null;
        }
        List<ConnectRecord> eventRecords = Lists.newArrayList();
        eventQueue.drainTo(eventRecords, batchSize);
        return buildWithRunnerNameKeyMap(eventRecords);
    }

    public Map<String, TransformEngine<ConnectRecord>> getTaskTransformMap() {
        return taskTransformMap;
    }

    public Map<String, SinkTask> getPusherTaskMap() {
        return pusherTaskMap;
    }
    public boolean offerTargetTaskQueue(List<ConnectRecord> connectRecords) {
        Map<String, List<ConnectRecord>> recordMap = buildWithRunnerNameKeyMap(connectRecords);
        updateRecordQueueMap(recordMap, targetQueueMap);
        return targetQueue.addAll(connectRecords);
    }

    /**
     * init default thread poll param, support auto config
     *
     * @param threadPollName
     * @return
     */
    private ExecutorService initDefaultThreadPoolExecutor(String threadPollName) {
        return new ThreadPoolExecutor(200, 300, 1, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(300), ThreadUtils.newThreadFactory(threadPollName, false));
    }
    public void releaseTaskTransform() throws Exception {
        for (Map.Entry<String, TransformEngine<ConnectRecord>> taskTransform : taskTransformMap.entrySet()) {
            String runnerName = taskTransform.getKey();
            TransformEngine<ConnectRecord> transformEngine = taskTransform.getValue();
            transformEngine.close();
            taskTransformMap.remove(runnerName);
        }
    }

    public void releaseTriggerTask() {
        for (Map.Entry<String, SinkTask> triggerTask : pusherTaskMap.entrySet()) {
            SinkTask sinkTask = triggerTask.getValue();
            String runnerName = triggerTask.getKey();
            sinkTask.stop();
            pusherTaskMap.remove(runnerName);
        }
    }

    public void releaseExecutorService() throws Exception {
        for (Map.Entry<String, ExecutorService> pusherExecutor : pusherExecutorMap.entrySet()) {
            ExecutorService pusher = pusherExecutor.getValue();
            ShutdownUtils.shutdownThreadPool(pusher);
        }
    }
}
