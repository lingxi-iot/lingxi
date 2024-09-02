package cn.gofree.lingxi.eventbridge.data;

import cn.gofree.lingxi.eventbridge.entity.PutEventsResponseEntry;

public interface PutEventCallback {
    void endProcess(PutEventsResponseEntry entry);
}
