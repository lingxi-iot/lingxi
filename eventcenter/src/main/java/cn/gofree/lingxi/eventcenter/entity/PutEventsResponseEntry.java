package cn.gofree.lingxi.eventcenter.entity;

import lombok.Data;

@Data
public class PutEventsResponseEntry {
    private String eventId;

    private String errorCode;

    private String errorMessage;
}
