package cn.gofree.lingxi.eventbridge.common;

import cn.gofree.lingxi.eventbridge.listener.EventSubscriber;
import com.google.common.collect.Lists;
import io.openmessaging.connector.api.data.ConnectRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Manage the offset of event
 */
@Component
public class OffsetManager {

    @Autowired
    private EventSubscriber eventSubscriber;

    public void commit(final List<ConnectRecord> connectRecordList) {
        this.eventSubscriber.commit(connectRecordList);
    }

    public void commit(final ConnectRecord connectRecord) {
        this.eventSubscriber.commit(Lists.newArrayList(connectRecord));
    }

}
