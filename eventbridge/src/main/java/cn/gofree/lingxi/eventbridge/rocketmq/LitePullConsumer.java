package cn.gofree.lingxi.eventbridge.rocketmq;


import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;


import java.time.Duration;
import java.util.List;

public interface LitePullConsumer {
    void startup() throws MQClientException;

    void shutdown();

    void attachTopic(String topic, String tag);

    List<MessageExt> poll(int pullBatchSize, Duration timeout);

    void commit(List<String> messageIdList);

    void setSockProxyJson(String proxyJson);

    void subscribe(String topic);

    void unsubscribe(String topic);
}
