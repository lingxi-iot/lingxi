package cn.gofree.lingxi.eventcenter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class EventData {
    private String id;
    private URI source;
    private String type;
    private String datacontenttype;
    private URI dataschema;
    private String subject;
    private OffsetDateTime time;
    private byte[] data;
    private String specversion;
    private Map<String, Object> extensions;
}
