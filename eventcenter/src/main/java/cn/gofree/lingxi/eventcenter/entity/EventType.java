package cn.gofree.lingxi.eventcenter.entity;

import lombok.Data;

import java.util.Date;

@Data
public class EventType {
    private String accountId;
    private String eventBusName;
    private String eventSourceName;
    private String name;
    private String description;
    private Date gmtCreate;
    private Date gmtModify;
}
