package cn.gofree.lingxi.eventcenter.entity;


import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Setter
@Getter
public class EventRuleDetail extends EventRule {
    private List<EventTarget> eventTargets;

    public EventRuleDetail(String accountId, String eventBusName, String name, String description,
                           String filterPattern,
                           String status, Date gmtCreate, Date gmtModify) {
        super(accountId, eventBusName, name, description, filterPattern, status, gmtCreate, gmtModify);
    }
}
