package cn.gofree.lingxi.eventbridge.respository;

import cn.gofree.lingxi.eventbridge.data.PutEventCallback;
import cn.gofree.lingxi.eventbridge.entity.EventBridgeEvent;

public interface EventDataRepository {

    /**
     * @param accountId
     * @param eventBusName
     * @return
     */
    boolean createEventBusPersistence(String accountId, String eventBusName);

    /**
     * @param accountId
     * @param eventBusName
     * @return
     */
    boolean deleteEventBusPersistence(String accountId, String eventBusName);

    /**
     * @param accountId
     * @param eventBusName
     * @param event
     * @param putEventCallback
     * @return
     */
    boolean putEvent(String accountId, String eventBusName, EventBridgeEvent event, PutEventCallback putEventCallback);

    /**
     * @param accountId
     * @param eventBusName
     * @return
     */
    String getEventBusPersistentContext(String accountId, String eventBusName);

    /**
     * @param accountId
     * @param eventBusName
     * @return
     */
    String getTopicName(String accountId, String eventBusName);

    /**
     * @param accountId
     * @param eventBusName
     * @return
     */
    String getTopicNameWithOutCache(String accountId, String eventBusName);
}
