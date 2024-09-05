package cn.gofree.lingxi.eventcenter.entity;

import cn.gofree.lingxi.eventcenter.enums.EventSourceStatusEnum;
import cn.gofree.lingxi.eventcenter.enums.EventSourceTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.Map;
@Builder
@Data
public class EventSource {
    private String accountId;
    private String eventBusName;
    private String name;
    private String description;
    private EventSourceTypeEnum type;
    private String className;
    private Map<String, Object> config;
    private String runContext;
    private EventSourceStatusEnum status;
    private Date gmtCreate;
    private Date gmtModify;
}
