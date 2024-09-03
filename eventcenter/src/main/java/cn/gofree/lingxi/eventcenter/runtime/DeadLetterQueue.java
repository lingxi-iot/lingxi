package cn.gofree.lingxi.eventcenter.runtime;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class DeadLetterQueue {
    private String type;
    private Map<String,String> config;
}
