package cn.gofree.lingxi.eventcenter.comand;

import cn.gofree.lingxi.eventcenter.entity.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
public class EventBusCmd {
    public void createEventBus(String accountId,String eventBusName,String description){
        log.debug(String.format("创建事件总线:accountId=%s,EventBusName=%s,description=%s",accountId,eventBusName,description));
        String topicName = BuildTopicName(accountId, eventBusName);
        EventBus eventBus = EventBus.builder()
                .accountId(accountId)
                .eventBusName(eventBusName)
                .description(description)
                .topicName(topicName)
                .build();
        //TODO 存储到持久层

    }
    public EventBus getEventBus(String accountId, String EventBusName) {
        log.debug(String.format("获取事件总线:accountId=%s,EventBusName=%s", accountId, EventBusName));
        return new EventBus();
    }
    public void removeEventBus(String accountId, String EventBusName){
        log.debug(String.format("删除事件总线:accountId=%s,EventBusName=%s",accountId,EventBusName));
        //TODO 先验证依赖项是否可删除，如果可删除则删除
    }
    public List<EventBus> listEventBuses(String accountId){
        log.debug(String.format("列出事件总线:accountId=%s",accountId));
        return null;
    }
    private String BuildTopicName(String accountId, String eventBusName){
        return "eventbridge%" + accountId + "%" + eventBusName + "_" + System.currentTimeMillis();
    }
}
