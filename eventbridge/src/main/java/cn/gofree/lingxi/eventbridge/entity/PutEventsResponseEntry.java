package cn.gofree.lingxi.eventbridge.entity;

import lombok.Data;

@Data
public class PutEventsResponseEntry {
    private String eventId;

    private String errorCode;

    private String errorMessage;
}
