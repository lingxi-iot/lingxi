package cn.gofree.lingxi.eventcenter.context;

import cn.gofree.lingxi.eventcenter.entity.MessageQueue;
import cn.gofree.lingxi.eventcenter.entity.TargetKeyValue;
import cn.gofree.lingxi.eventcenter.enums.QueueState;
import io.openmessaging.connector.api.component.task.sink.SinkTaskContext;
import io.openmessaging.connector.api.data.RecordOffset;
import io.openmessaging.connector.api.data.RecordPartition;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TriggerTaskContext implements SinkTaskContext {
    private static Logger log= LoggerFactory.getLogger(TriggerTaskContext.class);

    private final TargetKeyValue taskConfig;
    private final Map<MessageQueue, Long> messageQueuesOffsetMap = new ConcurrentHashMap<>(64);
    private final Map<MessageQueue, QueueState> messageQueuesStateMap = new ConcurrentHashMap<>(64);

    public static final String BROKER_NAME = "brokerName";
    public static final String QUEUE_ID = "queueId";
    public static final String TOPIC = "topic";
    public static final String QUEUE_OFFSET = "queueOffset";

    public TriggerTaskContext(TargetKeyValue taskConfig) {
        this.taskConfig = taskConfig;
    }

    @Override
    public String getConnectorName() {
        return null;
    }

    @Override
    public String getTaskName() {
        return null;
    }

    @Override
    public void resetOffset(RecordPartition recordPartition, RecordOffset recordOffset) {
        //校验是否为空
      if (null == recordPartition || null == recordPartition.getPartition() || null == recordOffset || null == recordOffset.getOffset()) {
        log.warn("recordPartition {} info is null or recordOffset {} info is null", recordPartition, recordOffset);
        return;
      }
      String brokerName = (String) recordPartition.getPartition().get(BROKER_NAME);
      String topic = (String) recordPartition.getPartition().get(TOPIC);
      Integer queueId = Integer.valueOf((String) recordPartition.getPartition().get(QUEUE_ID));
      if (StringUtils.isEmpty(brokerName) || StringUtils.isEmpty(topic) || null == queueId) {
        log.warn("brokerName is null or queueId is null or queueName is null, brokerName {}, queueId {} queueId {}", brokerName, queueId, topic);
        return;
      }

      MessageQueue messageQueue = new MessageQueue(topic, brokerName, queueId);
      Long offset = Long.valueOf((String) recordOffset.getOffset().get(QUEUE_OFFSET));
      if (null == offset) {
        log.warn("resetOffset, offset is null");
        return;
      }
      messageQueuesOffsetMap.put(messageQueue, offset);
    }

    @Override
    public void resetOffset(Map<RecordPartition, RecordOffset> offsets) {
        if (MapUtils.isEmpty(offsets)) {
            log.warn("resetOffset, offsets {} is null", offsets);
            return;
        }
        for (Map.Entry<RecordPartition, RecordOffset> entry : offsets.entrySet()) {
            if (null == entry || null == entry.getKey() || null == entry.getKey().getPartition() || null == entry.getValue() || null == entry.getValue().getOffset()) {
                log.warn("recordPartition {} info is null or recordOffset {} info is null, entry {}", entry);
                continue;
            }
            RecordPartition recordPartition = entry.getKey();
            String brokerName = (String) recordPartition.getPartition().get(BROKER_NAME);
            String topic = (String) recordPartition.getPartition().get(TOPIC);
            Integer queueId = Integer.valueOf((String) recordPartition.getPartition().get(QUEUE_ID));
            if (StringUtils.isEmpty(brokerName) || StringUtils.isEmpty(topic) || null == queueId) {
                log.warn("brokerName is null or queueId is null or queueName is null, brokerName {}, queueId {} queueId {}", brokerName, queueId, topic);
                continue;
            }
            MessageQueue messageQueue = new MessageQueue(topic, brokerName, queueId);
            RecordOffset recordOffset = entry.getValue();
            Long offset = Long.valueOf((String) recordOffset.getOffset().get(QUEUE_OFFSET));
            if (null == offset) {
                log.warn("resetOffset, offset is null");
                continue;
            }
            messageQueuesOffsetMap.put(messageQueue, offset);
        }
    }

    @Override
    public void pause(List<RecordPartition> list) {

    }

    @Override
    public void resume(List<RecordPartition> list) {

    }

    @Override
    public Set<RecordPartition> assignment() {
        return null;
    }
}
