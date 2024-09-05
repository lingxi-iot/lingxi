package cn.gofree.lingxi.eventcenter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class EventBus {
    private String accountId;
    private String eventBusName;
    private String description;
    private String topicName;
    private Date gmtCreate;
    private Date gmtModify;
}
