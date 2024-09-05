package cn.gofree.lingxi.eventcenter.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@EqualsAndHashCode(of = {"accountId", "eventBusName", "name"})
@Setter
@Getter
@Builder
@Data
public class EventRule {
    private String accountId;
    private String eventBusName;
    private String name;
    private String description;
    private String filterPattern;
    private String status;
    private Date gmtCreate;
    private Date gmtModify;
}
