package cn.gofree.lingxi.eventcenter.entity;


import java.util.Date;
import java.util.Map;
import cn.gofree.lingxi.eventcenter.runtime.RunOptions;
import cn.gofree.lingxi.eventcenter.enums.EventTargetStatusEnum;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EventTarget {
    private String accountId;
    private String eventBusName;
    private String eventRuleName;
    private String name;
    private String className;
    private Map<String, Object> config;
    private RunOptions runOptions;
    private String runContext;
    private EventTargetStatusEnum status;
    private Date gmtCreate;
    private Date gmtModify;
}
