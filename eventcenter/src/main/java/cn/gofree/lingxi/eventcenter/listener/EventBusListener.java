package cn.gofree.lingxi.eventcenter.listener;

import cn.gofree.lingxi.eventcenter.context.EventContext;
import cn.gofree.lingxi.eventcenter.exception.ErrorHandler;
import cn.gofree.lingxi.eventcenter.runtime.ServiceThread;
import com.google.common.collect.Lists;
import io.openmessaging.connector.api.data.ConnectRecord;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class EventBusListener  extends ServiceThread {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventBusListener.class);

    private final EventContext circulatorContext;
    private final EventSubscriber eventSubscriber;
    private final ErrorHandler errorHandler;

    public EventBusListener(EventContext circulatorContext, EventSubscriber eventSubscriber,
                            ErrorHandler errorHandler) {
        this.circulatorContext = circulatorContext;
        this.eventSubscriber = eventSubscriber;
        this.errorHandler = errorHandler;
    }

    @Override
    public void run() {
        // 当 stopped 变量为 false 时，循环将继续执行
        while (!stopped) {
            // 创建一个新的 List 集合用于存储从事件订阅者中拉取的连接记录
            List<ConnectRecord> pullRecordList = Lists.newArrayList();

            try {
                // 从事件订阅者中尝试拉取最新的连接记录列表
                pullRecordList = eventSubscriber.pull();

                // 如果拉取到的连接记录列表为空，则等待 1000 毫秒后继续下一次循环
                if (CollectionUtils.isEmpty(pullRecordList)) {
                    this.waitForRunning(1000);
                    continue;
                }

                // 将非空的连接记录列表放入循环上下文中，供后续处理
                circulatorContext.offerEventRecords(pullRecordList);
            } catch (Exception exception) {
                // 如果在拉取记录过程中出现异常，记录错误日志
                LOGGER.error(getServiceName() + " - event bus pull record exception, stackTrace - ", exception);

                // 遍历异常情况下获取的连接记录列表，对每个记录进行错误处理
                pullRecordList.forEach(pullRecord -> errorHandler.handle(pullRecord, exception));
            }
        }
    }

    @Override
    public String getServiceName() {
        return EventBusListener.class.getSimpleName();
    }

    @Override
    public void shutdown() {
        eventSubscriber.close();
    }
}