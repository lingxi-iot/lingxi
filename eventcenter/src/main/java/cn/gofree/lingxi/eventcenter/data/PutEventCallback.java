package cn.gofree.lingxi.eventcenter.data;

import cn.gofree.lingxi.eventcenter.entity.PutEventsResponseEntry;

public interface PutEventCallback {
    void endProcess(PutEventsResponseEntry entry);
}
