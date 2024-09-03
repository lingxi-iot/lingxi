package cn.gofree.lingxi.eventcenter.entity;

import com.google.common.collect.Maps;
import io.cloudevents.SpecVersion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class EventBridgeEvent {
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

    public static Set<String> getAttributeKeys() {
        return SpecVersion.V1.getAllAttributes();
    }

    public Object getExtension(String extensionKey) {
        if (extensions == null || extensions.isEmpty()) {
            return null;
        } else {
            return extensions.get(extensionKey);
        }
    }

    public void addExtension(String extensionKey, Object extensionValue) {
        if (extensions == null) {
            extensions = Maps.newHashMap();
        }
        extensions.put(extensionKey, extensionValue);
    }
}
