package cn.gofree.lingxi.eventcenter.listener;

import com.alibaba.fastjson.JSON;
import cn.gofree.lingxi.eventcenter.config.ConfigLoader;
import cn.gofree.lingxi.eventcenter.exception.EventBridgeException;
import cn.gofree.lingxi.eventcenter.rocketmq.ClientConfig;
import cn.gofree.lingxi.eventcenter.rocketmq.LitePullConsumer;
import cn.gofree.lingxi.eventcenter.constant.RuntimeConfigDefine;
import cn.gofree.lingxi.eventcenter.entity.SubscribeRunnerKeys;
import cn.gofree.lingxi.eventcenter.enums.RefreshTypeEnum;
import cn.gofree.lingxi.eventcenter.observer.TargetRunnerConfigObserver;
import cn.gofree.lingxi.eventcenter.respository.EventDataRepository;
import cn.gofree.lingxi.eventcenter.rocketmq.LitePullConsumerImpl;
import cn.gofree.lingxi.eventcenter.runtime.ServiceThread;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import io.openmessaging.KeyValue;
import io.openmessaging.connector.api.data.ConnectRecord;
import io.openmessaging.connector.api.data.RecordOffset;
import io.openmessaging.connector.api.data.RecordPartition;
import io.openmessaging.connector.api.data.Schema;
import io.openmessaging.internal.DefaultKeyValue;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.acl.common.AclClientRPCHook;
import org.apache.rocketmq.acl.common.SessionCredentials;
import org.apache.rocketmq.client.AccessChannel;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.RPCHook;
import org.apache.rocketmq.remoting.proxy.SocksProxyConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


// 定义 RocketMQEventSubscriber 类，继承自 EventSubscriber
@Component
public class RocketMQEventSubscriber extends EventSubscriber {

    // 日志记录器实例
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketMQEventSubscriber.class);

    // 事件数据仓库实例，用于存储和检索事件数据
    //@Autowired
    private EventDataRepository eventDataRepository;

    // 目标运行器配置观察者实例，用于观察运行器配置的变化
    @Autowired
    private TargetRunnerConfigObserver runnerConfigObserver;

    // 配置加载器实例，用于加载应用程序配置
    @Autowired
    private ConfigLoader configLoader;

    // 消息缓冲队列，用于暂存从 RocketMQ 接收的消息
    private final BlockingQueue<MessageExt> messageBuffer = new LinkedBlockingQueue<>(50000);

    // RocketMQ 拉取超时时间
    private Integer pullTimeOut;

    // RocketMQ 拉取批处理大小
    private Integer pullBatchSize;

    // RocketMQ 客户端配置对象
    private ClientConfig clientConfig;

    // RocketMQ 会话凭证，用于身份验证
    private SessionCredentials sessionCredentials;

    // RocketMQ socks 代理配置字符串
    private String socksProxy;

    // 消费者工作线程映射，键为运行器名称，值为 ConsumeWorker 实例
    private Map<String, ConsumeWorker> consumeWorkerMap = new ConcurrentHashMap<>();

    // 分隔符常量定义
    private static final String SEMICOLON = ";";

    // 默认消费者组前缀
    private static final String DEFAULT_GROUP_PREFIX = "event-bridge-group";

    // RocketMQ 消息 ID 属性名
    public static final String MSG_ID = "msgId";

    // RocketMQ 队列偏移量属性名
    public static final String QUEUE_OFFSET = "queueOffset";

    // 初始化方法，在 Spring 容器中创建实例后调用
    @PostConstruct
    public void initRocketMQEventSubscriber() {
        // 初始化 MQ 属性配置
        this.initMqProperties();

        // 初始化消费工作线程
        this.initConsumeWorkers();
    }

    // 刷新订阅者方法，根据不同的刷新类型执行添加、更新或删除操作
    @Override
    public void refresh(SubscribeRunnerKeys subscribeRunnerKeys, RefreshTypeEnum refreshTypeEnum) {
        // 根据刷新类型执行相应操作
        switch (refreshTypeEnum) {
            case ADD:
            case UPDATE:
                putConsumeWorker(subscribeRunnerKeys);
                break;
            case DELETE:
                removeConsumeWorker(subscribeRunnerKeys);
                break;
            default:
                break;
        }
    }

    // 拉取方法，用于从消息缓冲队列中拉取消息并转换为 ConnectRecord 列表
    @Override
    public List<ConnectRecord> pull() {
        // 创建消息列表，从消息缓冲队列中拉取指定数量的消息
        ArrayList<MessageExt> messages = new ArrayList<>();
        messageBuffer.drainTo(messages, pullBatchSize);

        // 如果消息列表为空，则返回 null
        if (CollectionUtils.isEmpty(messages)) {
            LOGGER.trace("consumer poll message empty.");
            return null;
        }

        // 创建 ConnectRecord 列表
        List<ConnectRecord> connectRecords = new CopyOnWriteArrayList<>();

        // 创建 CompletableFuture 列表，用于处理异步转换操作
        List<CompletableFuture<Void>> completableFutures = Lists.newArrayList();

        // 遍历消息列表，将每条消息转换为 ConnectRecord 并添加到 CompletableFuture 列表中
        messages.forEach(item -> {
            CompletableFuture<Void> recordCompletableFuture = CompletableFuture.supplyAsync(() -> convertToSinkRecord(item))
                    .exceptionally((exception) -> {
                        LOGGER.error("execute completable job failed", exception);
                        return null;
                    })
                    .thenAccept(connectRecords::add);
            completableFutures.add(recordCompletableFuture);
        });

        // 等待所有 CompletableFuture 完成
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[messages.size()])).join();

        // 返回 ConnectRecord 列表
        return connectRecords;
    }

    // 提交方法，用于批量提交 ConnectRecord 到指定的运行器
    @Override
    public void commit(List<ConnectRecord> connectRecordList) {
        // 如果 ConnectRecord 列表为空，则警告并返回
        if (CollectionUtils.isEmpty(connectRecordList)) {
            LOGGER.warn("commit event record data empty!");
            return;
        }

        // 获取第一个 ConnectRecord 中的运行器名称
        String runnerName = connectRecordList.iterator().next().getExtension(RuntimeConfigDefine.RUNNER_NAME);

        // 从 ConnectRecord 列表中提取消息 ID 列表
        List<String> msgIds = connectRecordList.stream().map(item -> item.getPosition()
                .getPartition().getPartition().get(MSG_ID).toString()).collect(Collectors.toList());

        // 调用 ConsumeWorker 的 commit 方法提交消息 ID 列表
        consumeWorkerMap.get(runnerName).commit(msgIds);
    }

    // 关闭方法，用于关闭所有的 ConsumeWorker 实例
    @Override
    public void close() {
        // 遍历 consumeWorkerMap 中的所有 ConsumeWorker 实例，调用其 shutdown 方法
        for (Map.Entry<String, ConsumeWorker> item : consumeWorkerMap.entrySet()) {
            ConsumeWorker consumeWorker = item.getValue();
            consumeWorker.shutdown();
        }
    }

    // 初始化 MQ 属性配置的方法
    private void initMqProperties() {
        try {
            // 创建并初始化 ClientConfig 对象
            ClientConfig clientConfig = new ClientConfig();

            // 设置 RocketMQ 名称服务器地址
            String namesrvAddr = configLoader.getString("rocketmq.namesrvAddr");

            // 设置拉取超时时间
            pullTimeOut = Integer.valueOf(configLoader.getString("rocketmq.consumer.pullTimeOut"));

            // 设置拉取批处理大小
            pullBatchSize = Integer.valueOf(configLoader.getString("rocketmq.consumer.pullBatchSize"));

            // 设置访问通道类型
            String accessChannel = configLoader.getString("rocketmq.accessChannel");

            // 设置命名空间
            String namespace = configLoader.getString("rocketmq.namespace");

            // 设置访问密钥
            String accessKey = configLoader.getString("rocketmq.consumer.accessKey");

            // 设置秘密密钥
            String secretKey = configLoader.getString("rocketmq.consumer.secretKey");

            // 设置 socks5 用户名
            String socks5UserName = configLoader.getString("rocketmq.consumer.socks5UserName");

            // 设置 socks5 密码
            String socks5Password = configLoader.getString("rocketmq.consumer.socks5Password");

            // 设置 socks5 终端点
            String socks5Endpoint = configLoader.getString("rocketmq.consumer.socks5Endpoint");

            // 设置 ClientConfig 对象的属性
            clientConfig.setNameSrvAddr(namesrvAddr);
            clientConfig.setAccessChannel(AccessChannel.CLOUD.name().equals(accessChannel) ?
                    AccessChannel.CLOUD : AccessChannel.LOCAL);
            clientConfig.setNamespace(namespace);
            this.clientConfig = clientConfig;

            // 如果访问密钥和秘密密钥不为空，则设置会话凭证
            if (StringUtils.isNotBlank(accessKey) && StringUtils.isNotBlank(secretKey)) {
                this.sessionCredentials = new SessionCredentials(accessKey, secretKey);
            }

            // 如果 socks5 配置不为空，则设置 socks 代理配置
            if (StringUtils.isNotBlank(socks5UserName) && StringUtils.isNotBlank(socks5Password)
                    && StringUtils.isNotBlank(socks5Endpoint)) {
                SocksProxyConfig proxyConfig = new SocksProxyConfig();
                proxyConfig.setUsername(socks5UserName);
                proxyConfig.setPassword(socks5Password);
                proxyConfig.setAddr(socks5Endpoint);
                Map<String, SocksProxyConfig> proxyConfigMap = Maps.newHashMap();
                proxyConfigMap.put("0.0.0.0/0", proxyConfig);
                this.socksProxy = new Gson().toJson(proxyConfigMap);
            }

        } catch (Exception exception) {
            // 记录初始化 RocketMQ 属性配置异常
            LOGGER.error("init rocket mq property exception, stack trace-", exception);
        }
    }

    // 初始化消费工作线程的方法
    private void initConsumeWorkers() {
        // 获取订阅运行器键集合
        Set<SubscribeRunnerKeys> subscribeRunnerKeysSet = runnerConfigObserver.getSubscribeRunnerKeys();

        // 如果集合为空，则直接返回
        if (subscribeRunnerKeysSet == null || subscribeRunnerKeysSet.isEmpty()) {
            return;
        }

        // 遍历订阅运行器键集合，初始化 LitePullConsumer 和 ConsumeWorker
        for (SubscribeRunnerKeys subscribeRunnerKeys : subscribeRunnerKeysSet) {
            LitePullConsumer litePullConsumer = initLitePullConsumer(subscribeRunnerKeys);
            ConsumeWorker consumeWorker = new ConsumeWorker(litePullConsumer, subscribeRunnerKeys.getRunnerName());
            consumeWorkerMap.put(subscribeRunnerKeys.getRunnerName(), consumeWorker);
            consumeWorker.start();
        }
    }

    // 初始化 LitePullConsumer 的方法
    public LitePullConsumer initLitePullConsumer(SubscribeRunnerKeys subscribeRunnerKeys) {
        // 获取主题名称
        String topic = getTopicName(subscribeRunnerKeys);

        // 创建 RPC 钩子，如果会话凭证不为空
        RPCHook rpcHook = this.sessionCredentials != null ? new AclClientRPCHook(this.sessionCredentials) : null;

        // 克隆客户端配置
        ClientConfig consumerConfig = ClientConfig.cloneConfig(this.clientConfig);

        // 创建消费者组名
        String groupName = createGroupName(subscribeRunnerKeys);

        // 设置消费者组名
        consumerConfig.setConsumerGroup(groupName);

        // 创建 LitePullConsumer 实例
        LitePullConsumerImpl pullConsumer = new LitePullConsumerImpl(consumerConfig, rpcHook);

        // 如果 socks 代理配置不为空，则设置 socks 代理 JSON 字符串
        if (StringUtils.isNotBlank(this.socksProxy)) {
            pullConsumer.setSockProxyJson(this.socksProxy);
        }

        try {
            // 将主题绑定到消费者
            pullConsumer.attachTopic(topic, "*");

            // 启动消费者
            pullConsumer.startup();
        } catch (Exception exception) {
            // 记录初始化默认消费者异常
            LOGGER.error("init default pull consumer exception, topic -" + topic + "-stackTrace-", exception);

            // 抛出事件桥异常
            throw new EventBridgeException(" init rocketmq consumer failed");
        }

        // 返回 LitePullConsumer 实例
        return pullConsumer;
    }

    // 获取主题名称的方法
    private String getTopicName(SubscribeRunnerKeys subscribeRunnerKeys) {
        // 返回通配符主题名称，注释中提到此处需要修改
        String topicName="eventbridge%" + subscribeRunnerKeys.getAccountId() + "%" + subscribeRunnerKeys.getEventBusName() + "_" + "*";
        //TODO get Topic from Topic cache , topicName format should be "eventbridge%accountId%eventBusName_0" .
        //String topicName=eventDataRepository.getTopicNameWithOutCache(subscribeRunnerKeys.getAccountId(), subscribeRunnerKeys.getEventBusName());
        System.out.println(topicName);
        return topicName;

    }
    public String buildTopicName(String accountId, String eventBusName) {
        return "eventbridge%" + accountId + "%" + eventBusName + "_" + System.currentTimeMillis();
    }
    // 创建消费者组名的方法
    private String createGroupName(SubscribeRunnerKeys subscribeRunnerKeys) {
        // 构建消费者组名字符串
        StringBuilder sb = new StringBuilder();
        sb.append(DEFAULT_GROUP_PREFIX).append("-");
        sb.append(subscribeRunnerKeys.getAccountId()).append("-");
        sb.append(subscribeRunnerKeys.getRunnerName());

        // 返回构建的消费者组名
        return sb.toString().replace(".", "-");
    }

    // 将 MessageExt 转换为 ConnectRecord 的方法
    private ConnectRecord convertToSinkRecord(MessageExt messageExt) {
        // 获取消息属性映射
        Map<String, String> properties = messageExt.getProperties();

        // 创建模式对象
        Schema schema;

        // 创建时间戳对象
        Long timestamp;

        // 创建 ConnectRecord 对象
        ConnectRecord sinkRecord;

        // 获取 Connect 时间戳属性
        String connectTimestamp = properties.get(RuntimeConfigDefine.CONNECT_TIMESTAMP);

        // 设置时间戳
        timestamp = StringUtils.isNotEmpty(connectTimestamp) ? Long.valueOf(connectTimestamp) : null;

        // 获取 Connect 模式属性
        String connectSchema = properties.get(RuntimeConfigDefine.CONNECT_SCHEMA);

        // 设置模式
        schema = StringUtils.isNotEmpty(connectSchema) ? JSON.parseObject(connectSchema, Schema.class) : null;

        // 获取消息体字节数组
        byte[] body = messageExt.getBody();

        // 创建 RecordPartition 对象
        RecordPartition recordPartition = convertToRecordPartition(messageExt.getTopic(), messageExt.getBrokerName(), messageExt.getQueueId(), messageExt.getMsgId());

        // 创建 RecordOffset 对象
        RecordOffset recordOffset = convertToRecordOffset(messageExt.getQueueOffset());

        // 将消息体字节数组转换为 UTF-8 编码的字符串
        String bodyStr = new String(body, StandardCharsets.UTF_8);

        // 创建 ConnectRecord 对象
        sinkRecord = new ConnectRecord(recordPartition, recordOffset, timestamp, schema, bodyStr);

        // 创建 KeyValue 对象
        KeyValue keyValue = new DefaultKeyValue();

        // 如果消息属性映射不为空，则遍历映射并将键值对添加到 KeyValue 对象中
        if (MapUtils.isNotEmpty(properties)) {
            for (Map.Entry<String, String> entry : properties.entrySet()) {
                keyValue.put(entry.getKey(), entry.getValue());
            }
        }

        // 将 KeyValue 对象添加到 ConnectRecord 的扩展属性中
        sinkRecord.addExtension(keyValue);

        // 返回 ConnectRecord 对象
        return sinkRecord;
    }

    // 将消息属性转换为 RecordPartition 对象的方法
    private RecordPartition convertToRecordPartition(String topic, String brokerName, int queueId, String msgId) {
        // 创建 RecordPartition 属性映射
        Map<String, String> map = new HashMap<>();

        // 设置主题属性
        map.put("topic", topic);

        // 设置 Broker 名称属性
        map.put("brokerName", brokerName);

        // 设置队列 ID 属性
        map.put("queueId", queueId + "");

        // 设置消息 ID 属性
        map.put(MSG_ID, msgId);

        // 创建并返回 RecordPartition 对象
        RecordPartition recordPartition = new RecordPartition(map);

        return recordPartition;
    }

    // 将消息属性转换为 RecordOffset 对象的方法
    private RecordOffset convertToRecordOffset(Long offset) {
        // 创建 RecordOffset 属性映射
        Map<String, String> offsetMap = new HashMap<>();

        // 设置队列偏移量属性
        offsetMap.put(QUEUE_OFFSET, offset + "");

        // 创建并返回 RecordOffset 对象
        RecordOffset recordOffset = new RecordOffset(offsetMap);

        return recordOffset;
    }

    // 添加 ConsumeWorker 的方法
    private void putConsumeWorker(SubscribeRunnerKeys subscribeRunnerKeys) {
        // 获取 ConsumeWorker 实例
        ConsumeWorker consumeWorker = consumeWorkerMap.get(subscribeRunnerKeys.getRunnerName());

        // 如果 ConsumeWorker 实例不为空，则关闭现有实例
        if (!Objects.isNull(consumeWorker)) {
            consumeWorker.shutdown();
        }

        // 初始化 LitePullConsumer 实例
        LitePullConsumer litePullConsumer = initLitePullConsumer(subscribeRunnerKeys);

        // 创建新的 ConsumeWorker 实例
        ConsumeWorker newWorker = new ConsumeWorker(litePullConsumer, subscribeRunnerKeys.getRunnerName());

        // 将新的 ConsumeWorker 实例添加到映射中
        consumeWorkerMap.put(subscribeRunnerKeys.getRunnerName(), newWorker);

        // 启动新的 ConsumeWorker 实例
        newWorker.start();
    }

    // 删除 ConsumeWorker 的方法
    private void removeConsumeWorker(SubscribeRunnerKeys subscribeRunnerKeys) {
        // 从映射中移除 ConsumeWorker 实例
        ConsumeWorker consumeWorker = consumeWorkerMap.remove(subscribeRunnerKeys.getRunnerName());

        // 如果 ConsumeWorker 实例不为空，则关闭实例
        if (!Objects.isNull(consumeWorker)) {
            consumeWorker.shutdown();
        }
    }

    // ConsumeWorker 内部类定义
    class ConsumeWorker extends ServiceThread {

        // LitePullConsumer 实例
        private final LitePullConsumer pullConsumer;

        // 运行器名称
        private final String runnerName;

        // 构造函数
        public ConsumeWorker(LitePullConsumer pullConsumer, String runnerName) {
            this.pullConsumer = pullConsumer;
            this.runnerName = runnerName;
        }

        // 获取服务名称的方法
        @Override
        public String getServiceName() {
            return ConsumeWorker.class.getSimpleName();
        }

        // 主循环方法
        @Override
        public void run() {

            // 当未停止时持续运行
            while (!stopped) {
                try {
                    // 从 LitePullConsumer 拉取消息
                    List<MessageExt> messages = pullConsumer.poll(pullBatchSize, Duration.ofMillis(pullTimeOut));

                    // 遍历消息列表，添加运行器名称属性并放入消息缓冲队列
                    for (MessageExt message : messages) {
                        message.putUserProperty(RuntimeConfigDefine.RUNNER_NAME, runnerName);
                        messageBuffer.put(message);
                    }
                } catch (Exception exception) {
                    // 记录异常日志
                    LOGGER.error(getServiceName() + " - RocketMQEventSubscriber pull record exception, stackTrace - ", exception);
                }
            }
        }

        // 提交消息 ID 列表的方法
        public void commit(List<String> messageIds) {
            this.pullConsumer.commit(messageIds);
        }

        // 关闭方法
        @Override
        public void shutdown() {
            // 关闭 LitePullConsumer 实例
            pullConsumer.shutdown();

            // 调用父类的 shutdown 方法
            super.shutdown();
        }
    }
}
